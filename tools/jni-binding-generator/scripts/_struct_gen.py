"""
Struct/data-class marshalling code generator.

For each Kotlin ``data class`` (or eligible ``class``) whose primary constructor
uses only supported leaf types, this module generates:

  * A ``struct JNI_<Name>`` C++ value type mirroring the Kotlin fields.
  * An ``inline JNI_<Name> extract_<Name>(JNIEnv*, jobject)`` accessor that
    reads every field from a live Java object into the C++ struct.
  * An ``inline jobject make_<Name>(JNIEnv*, const JNI_<Name>&)`` mutator
    that constructs a new Java object from C++ values.

The generated helpers are emitted at the top of each ``.gen.cpp`` file that
references the struct type, before the JNI function bodies.  Helpers for
dependency structs (fields whose type is itself a struct) are emitted first
(topological order).
"""

from __future__ import annotations

import re

from _models import KotlinField, KotlinStruct, ParsedFile
from _types import resolve_typealias

# ---------------------------------------------------------------------------
# Field-level type tables
# ---------------------------------------------------------------------------

# Kotlin scalar type → (JNI field descriptor, C++ struct field type, GetXField suffix)
_PRIM: dict[str, tuple[str, str, str]] = {
    "Int": ("I", "int32_t", "Int"),
    "Long": ("J", "int64_t", "Long"),
    "Float": ("F", "float", "Float"),
    "Double": ("D", "double", "Double"),
    "Short": ("S", "int16_t", "Short"),
    "Byte": ("B", "int8_t", "Byte"),
    "Boolean": ("Z", "bool", "Boolean"),
}

# Kotlin primitive-array type → (JNI descriptor, C++ vector type, NewXArray suffix, elem jni type)
_PRIM_ARR: dict[str, tuple[str, str, str, str]] = {
    "IntArray": ("[I", "std::vector<int32_t>", "Int", "jint"),
    "LongArray": ("[J", "std::vector<int64_t>", "Long", "jlong"),
    "FloatArray": ("[F", "std::vector<float>", "Float", "jfloat"),
    "DoubleArray": ("[D", "std::vector<double>", "Double", "jdouble"),
    "ShortArray": ("[S", "std::vector<int16_t>", "Short", "jshort"),
    "ByteArray": ("[B", "std::vector<int8_t>", "Byte", "jbyte"),
    # BooleanArray handled specially below (std::vector<bool> is not a contiguous buffer)
    "BooleanArray": ("[Z", "std::vector<bool>", "Boolean", "jboolean"),
}

# JNI descriptor part for each Kotlin type (used when building constructor descriptor).
_CTOR_DESC: dict[str, str] = {
    "Int": "I",
    "Long": "J",
    "Float": "F",
    "Double": "D",
    "Short": "S",
    "Byte": "B",
    "Boolean": "Z",
    "String": "Ljava/lang/String;",
    "IntArray": "[I",
    "LongArray": "[J",
    "FloatArray": "[F",
    "DoubleArray": "[D",
    "ShortArray": "[S",
    "ByteArray": "[B",
    "BooleanArray": "[Z",
}


# ---------------------------------------------------------------------------
# Dependency ordering
# ---------------------------------------------------------------------------


def collect_needed_structs(parsed: ParsedFile) -> set[str]:
    """Return the set of struct type names transitively referenced by *parsed*'s functions."""
    needed: set[str] = set()
    queue: list[str] = []

    for func in parsed.functions:
        for p in func.params:
            base = p.kotlin_type.rstrip("?").strip()
            if base in parsed.struct_types:
                queue.append(base)
        if func.return_type:
            base = func.return_type.rstrip("?").strip()
            if base in parsed.struct_types:
                queue.append(base)

    while queue:
        name = queue.pop()
        if name in needed or name not in parsed.struct_types:
            continue
        needed.add(name)
        for f in parsed.struct_types[name].fields:
            ft = f.kotlin_type
            if ft in parsed.struct_types and ft not in needed:
                queue.append(ft)
                continue
            elem = _array_element_struct(ft, parsed.struct_types)
            if elem is not None and elem not in needed:
                queue.append(elem)

    return needed


def topo_sort_structs(needed: set[str], all_structs: dict[str, KotlinStruct]) -> list[str]:
    """Return struct names in topological order (dependencies before dependents)."""
    visited: set[str] = set()
    result: list[str] = []

    def visit(name: str) -> None:
        if name in visited or name not in all_structs:
            return
        visited.add(name)
        for f in all_structs[name].fields:
            if f.kotlin_type in all_structs:
                visit(f.kotlin_type)
                continue
            elem = _array_element_struct(f.kotlin_type, all_structs)
            if elem is not None:
                visit(elem)
        result.append(name)

    for name in sorted(needed):  # sorted for deterministic output
        visit(name)
    return result


# ---------------------------------------------------------------------------
# C++ type derivation for struct fields
# ---------------------------------------------------------------------------

_ARRAY_OF_RE = re.compile(r"^Array<(.+)>$")


def _array_element_struct(kt: str, all_structs: dict[str, KotlinStruct]) -> str | None:
    """If *kt* is ``Array<X>`` and X is a known struct, return X; else None."""
    m = _ARRAY_OF_RE.match(kt)
    if not m:
        return None
    elem = m.group(1).strip()
    return elem if elem in all_structs else None


def _jvm_path(name: str, all_structs: dict[str, KotlinStruct]) -> str:
    """Return the internal JNI class-path descriptor body for a known struct, e.g. 'pkg/Name'."""
    pkg = all_structs[name].package.replace(".", "/")
    sep = "/" if pkg else ""
    return f"{pkg}{sep}{name}"


def _enum_jvm_path(kt: str, enum_packages: dict[str, str], fallback_package: str = "") -> str:
    """Internal JNI class-path for an enum-typed field, e.g. 'pkg/Kind' (no 'L'/';' wrapper).

    Uses the enum's own *declaring* package from *enum_packages* (populated by the
    driver pre-pass) — an enum very often lives in a different package than the
    struct/function that references it (e.g. an `enums` sub-package vs. a `models`
    sub-package), so assuming "same package as the referencer" is wrong in the common
    case, not just an edge case. Falls back to *fallback_package* only if the enum's
    package wasn't recorded (should not normally happen for a confirmed enum type).
    """
    pkg = enum_packages.get(kt, fallback_package).replace(".", "/")
    sep = "/" if pkg else ""
    return f"{pkg}{sep}{kt}"


def _enum_field_descriptor(
    kt: str, enum_packages: dict[str, str], fallback_package: str = ""
) -> str:
    """Internal JNI field descriptor for an enum-typed field, e.g. 'Lpkg/Kind;'."""
    return f"L{_enum_jvm_path(kt, enum_packages, fallback_package)};"


def _cpp_field_type(
    f: KotlinField,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    typealiases: dict[str, str] | None = None,
) -> str:
    kt = resolve_typealias(f.kotlin_type, typealiases)
    if kt in _PRIM:
        return _PRIM[kt][1]
    if kt in _PRIM_ARR:
        return _PRIM_ARR[kt][1]
    if kt == "String":
        return "std::string"
    if kt in enum_names:
        return "int32_t"  # marshalled as ordinal, same convention as enum function params
    if kt in all_structs:
        if f.nullable or f.optional_pointer:
            return f"std::optional<JNI_{kt}>"
        return f"JNI_{kt}"
    elem_struct = _array_element_struct(kt, all_structs)
    if elem_struct is not None:
        return f"std::vector<JNI_{elem_struct}>"
    # Unknown/unsupported field type — placeholder
    return f"void* /* TODO: unsupported field type '{kt}' */"


def _field_descriptor(
    f: KotlinField,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    package: str = "",
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> str:
    kt = resolve_typealias(f.kotlin_type, typealiases)
    if kt in _PRIM:
        return _PRIM[kt][0]
    if kt in _PRIM_ARR:
        return _PRIM_ARR[kt][0]
    if kt == "String":
        return "Ljava/lang/String;"
    if kt in enum_names:
        return _enum_field_descriptor(kt, enum_packages or {}, package)
    if kt in all_structs:
        return f"L{_jvm_path(kt, all_structs)};"
    elem_struct = _array_element_struct(kt, all_structs)
    if elem_struct is not None:
        return f"[L{_jvm_path(elem_struct, all_structs)};"
    return f"Ljava/lang/Object;  /* TODO: unknown type '{kt}' */"


def _ctor_descriptor(
    fields: list[KotlinField],
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    package: str = "",
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> str:
    parts: list[str] = []
    for f in fields:
        kt = resolve_typealias(f.kotlin_type, typealiases)
        if kt in _CTOR_DESC:
            parts.append(_CTOR_DESC[kt])
        elif kt in enum_names:
            parts.append(_enum_field_descriptor(kt, enum_packages or {}, package))
        elif kt in all_structs:
            parts.append(f"L{_jvm_path(kt, all_structs)};")
        else:
            elem_struct = _array_element_struct(kt, all_structs)
            if elem_struct is not None:
                parts.append(f"[L{_jvm_path(elem_struct, all_structs)};")
            else:
                parts.append("Ljava/lang/Object;")
    return "(" + "".join(parts) + ")V"


# ---------------------------------------------------------------------------
# extract_* generation
# ---------------------------------------------------------------------------


def _extract_field_lines(
    f: KotlinField,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    typealiases: dict[str, str] | None = None,
) -> list[str]:
    kt = resolve_typealias(f.kotlin_type, typealiases)
    n = f.name
    lines: list[str] = []

    if kt in enum_names:
        lines.append("    {")
        lines.append(f"        jobject _jenum = env->GetObjectField(obj, fid_{n});")
        lines.append(f"        out.{n} = enum_ordinal(env, _jenum);")
        lines.append("        if (_jenum) env->DeleteLocalRef(_jenum);")
        lines.append("    }")
        return lines

    if kt in _PRIM:
        _, cpp_t, get_sfx = _PRIM[kt]
        if kt == "Boolean":
            lines.append(f"    out.{n} = (env->GetBooleanField(obj, fid_{n}) == JNI_TRUE);")
        else:
            lines.append(
                f"    out.{n} = static_cast<{cpp_t}>(env->Get{get_sfx}Field(obj, fid_{n}));"
            )
        return lines

    if kt in _PRIM_ARR:
        _, cpp_t, arr_sfx, elem_jni = _PRIM_ARR[kt]
        jni_arr_t = f"j{arr_sfx.lower()}Array"
        lines.append("    {")
        lines.append(
            f"        {jni_arr_t} _arr = static_cast<{jni_arr_t}>("
            f"env->GetObjectField(obj, fid_{n}));"
        )
        lines.append("        if (_arr) {")
        lines.append("            jsize _len = env->GetArrayLength(_arr);")
        if kt == "BooleanArray":
            lines.append(f"            std::vector<{elem_jni}> _tmp(static_cast<size_t>(_len));")
            lines.append("            env->GetBooleanArrayRegion(_arr, 0, _len, _tmp.data());")
            lines.append(f"            out.{n}.assign(_tmp.begin(), _tmp.end());")
        else:
            lines.append(f"            out.{n}.resize(static_cast<size_t>(_len));")
            lines.append(
                f"            env->Get{arr_sfx}ArrayRegion(_arr, 0, _len, "
                f"reinterpret_cast<{elem_jni}*>(out.{n}.data()));"
            )
        lines.append("            env->DeleteLocalRef(_arr);")
        lines.append("        }")
        lines.append("    }")
        if f.count_field:
            lines.append(
                f"    // NOTE: {n}.size() should equal {f.count_field} per --struct-config"
            )
        return lines

    if kt == "String":
        lines.append("    {")
        lines.append(
            f"        jstring _jstr = static_cast<jstring>(env->GetObjectField(obj, fid_{n}));"
        )
        lines.append("        if (_jstr) {")
        lines.append("            const char* _cs = env->GetStringUTFChars(_jstr, nullptr);")
        lines.append(f'            out.{n} = _cs ? _cs : "";')
        lines.append("            if (_cs) env->ReleaseStringUTFChars(_jstr, _cs);")
        lines.append("            env->DeleteLocalRef(_jstr);")
        lines.append("        }")
        lines.append("    }")
        return lines

    if kt in all_structs:
        lines.append("    {")
        lines.append(f"        jobject _jobj = env->GetObjectField(obj, fid_{n});")
        if f.nullable or f.optional_pointer:
            lines.append("        if (_jobj) {")
            lines.append(f"            out.{n} = extract_{kt}(env, _jobj);")
            lines.append("            env->DeleteLocalRef(_jobj);")
            lines.append("        }")
        else:
            lines.append("        if (_jobj) {")
            lines.append(f"            out.{n} = extract_{kt}(env, _jobj);")
            lines.append("            env->DeleteLocalRef(_jobj);")
            lines.append("        }")
        lines.append("    }")
        return lines

    elem_struct = _array_element_struct(kt, all_structs)
    if elem_struct is not None:
        lines.append("    {")
        lines.append(
            f"        jobjectArray _arr = static_cast<jobjectArray>("
            f"env->GetObjectField(obj, fid_{n}));"
        )
        lines.append("        if (_arr) {")
        lines.append("            jsize _len = env->GetArrayLength(_arr);")
        lines.append(f"            out.{n}.reserve(static_cast<size_t>(_len));")
        lines.append("            for (jsize _i = 0; _i < _len; ++_i) {")
        lines.append("                jobject _elem = env->GetObjectArrayElement(_arr, _i);")
        lines.append(f"                out.{n}.push_back(extract_{elem_struct}(env, _elem));")
        lines.append("                if (_elem) env->DeleteLocalRef(_elem);")
        lines.append("            }")
        lines.append("            env->DeleteLocalRef(_arr);")
        lines.append("        }")
        lines.append("    }")
        if f.count_field:
            lines.append(
                f"    // NOTE: {n}.size() should equal {f.count_field} per --struct-config"
            )
        return lines

    # Unsupported field type — emit a stub
    lines.append(f"    // TODO: unsupported field type '{kt}' for field '{n}' — skipped")
    return lines


def _generate_extract_fn(
    struct: KotlinStruct,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> list[str]:
    name = struct.name
    lines: list[str] = []
    lines.append(f"inline JNI_{name} extract_{name}(JNIEnv* env, jobject obj) {{")
    lines.append(f"    JNI_{name} out{{}};")
    lines.append("    if (!obj) return out;")
    lines.append("    jclass cls = env->GetObjectClass(obj);")
    for f in struct.fields:
        desc = _field_descriptor(
            f, all_structs, enum_names, struct.package, enum_packages, typealiases
        )
        lines.append(f'    jfieldID fid_{f.name} = env->GetFieldID(cls, "{f.name}", "{desc}");')
    lines.append("    env->DeleteLocalRef(cls);")
    for f in struct.fields:
        lines.extend(_extract_field_lines(f, all_structs, enum_names, typealiases))
    lines.append("    return out;")
    lines.append("}")
    return lines


# ---------------------------------------------------------------------------
# make_* generation
# ---------------------------------------------------------------------------


def _make_pre_args(
    fields: list[KotlinField],
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    package: str = "",
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> tuple[list[str], list[str], list[str]]:
    """Return (pre_lines, cleanup_lines, arg_exprs) for building a NewObject call.

    *pre_lines*: statements creating temporary JNI objects (jstring, jarray, …).
    *cleanup_lines*: DeleteLocalRef calls for temporaries, in reverse creation order.
    *arg_exprs*: per-parameter expressions to pass to NewObject.
    """
    pre: list[str] = []
    cleanup: list[str] = []
    args: list[str] = []

    for f in fields:
        kt = resolve_typealias(f.kotlin_type, typealiases)
        n = f.name

        if kt in enum_names:
            tmp = f"_jenum_{n}"
            jvm_path = _enum_jvm_path(kt, enum_packages or {}, package)
            pre.append(f'    jobject {tmp} = enum_from_ordinal(env, "{jvm_path}", val.{n});')
            pre.append(f"    if (!{tmp}) {{ {_cleanup_expr(cleanup)} return nullptr; }}")
            cleanup.insert(0, f"env->DeleteLocalRef({tmp});")
            args.append(tmp)
            continue

        if kt in _PRIM:
            _, cpp_t, _ = _PRIM[kt]
            if kt == "Boolean":
                args.append(f"static_cast<jboolean>(val.{n} ? JNI_TRUE : JNI_FALSE)")
            elif kt in ("Byte", "Short"):
                args.append(f"static_cast<j{kt.lower()}>(val.{n})")
            else:
                args.append(f"val.{n}")
            continue

        if kt == "String":
            tmp = f"_jstr_{n}"
            pre.append(f"    jstring {tmp} = env->NewStringUTF(val.{n}.c_str());")
            pre.append(f"    if (!{tmp}) {{ {_cleanup_expr(cleanup)} return nullptr; }}")
            cleanup.insert(0, f"env->DeleteLocalRef({tmp});")
            args.append(tmp)
            continue

        if kt in _PRIM_ARR:
            _, _, arr_sfx, elem_jni = _PRIM_ARR[kt]
            jni_arr_t = f"j{arr_sfx.lower()}Array"
            tmp = f"_jarr_{n}"
            pre.append(
                f"    {jni_arr_t} {tmp} = env->New{arr_sfx}Array("
                f"static_cast<jsize>(val.{n}.size()));"
            )
            pre.append(f"    if (!{tmp}) {{ {_cleanup_expr(cleanup)} return nullptr; }}")
            if kt == "BooleanArray":
                pre.append(
                    f"    {{ std::vector<jboolean> _tmp_{n}(val.{n}.begin(), val.{n}.end());"
                )
                pre.append(
                    f"      env->SetBooleanArrayRegion({tmp}, 0, "
                    f"static_cast<jsize>(val.{n}.size()), _tmp_{n}.data()); }}"
                )
            else:
                pre.append(
                    f"    env->Set{arr_sfx}ArrayRegion({tmp}, 0, "
                    f"static_cast<jsize>(val.{n}.size()), "
                    f"reinterpret_cast<const {elem_jni}*>(val.{n}.data()));"
                )
            cleanup.insert(0, f"env->DeleteLocalRef({tmp});")
            args.append(tmp)
            continue

        if kt in all_structs:
            tmp = f"_jobj_{n}"
            if f.nullable or f.optional_pointer:
                pre.append(
                    f"    jobject {tmp} = val.{n}.has_value() ? make_{kt}(env, *val.{n}) : nullptr;"
                )
                pre.append(
                    f"    if (!{tmp} && val.{n}.has_value()) "
                    f"{{ {_cleanup_expr(cleanup)} return nullptr; }}"
                )
            else:
                # val.{n} is a plain (non-optional) JNI_<Struct> here — no .has_value() to call.
                pre.append(f"    jobject {tmp} = make_{kt}(env, val.{n});")
                pre.append(f"    if (!{tmp}) {{ {_cleanup_expr(cleanup)} return nullptr; }}")
            cleanup.insert(0, f"if ({tmp}) env->DeleteLocalRef({tmp});")
            args.append(tmp)
            continue

        elem_struct = _array_element_struct(kt, all_structs)
        if elem_struct is not None:
            tmp = f"_jarr_{n}"
            elem_cls = f"_elemcls_{n}"
            pre.append(
                f'    jclass {elem_cls} = env->FindClass("{_jvm_path(elem_struct, all_structs)}");'
            )
            pre.append(f"    if (!{elem_cls}) {{ {_cleanup_expr(cleanup)} return nullptr; }}")
            pre.append(
                f"    jobjectArray {tmp} = env->NewObjectArray("
                f"static_cast<jsize>(val.{n}.size()), {elem_cls}, nullptr);"
            )
            pre.append(
                f"    env->DeleteLocalRef({elem_cls});"
                f" if (!{tmp}) {{ {_cleanup_expr(cleanup)} return nullptr; }}"
            )
            pre.append(f"    for (size_t _i = 0; _i < val.{n}.size(); ++_i) {{")
            pre.append(f"        jobject _elem = make_{elem_struct}(env, val.{n}[_i]);")
            pre.append(
                f"        if (!_elem) {{ env->DeleteLocalRef({tmp}); "
                f"{_cleanup_expr(cleanup)} return nullptr; }}"
            )
            pre.append(f"        env->SetObjectArrayElement({tmp}, static_cast<jsize>(_i), _elem);")
            pre.append("        env->DeleteLocalRef(_elem);")
            pre.append("    }")
            cleanup.insert(0, f"env->DeleteLocalRef({tmp});")
            args.append(tmp)
            continue

        # Unsupported — pass nullptr
        args.append("nullptr /* TODO: unsupported field */")

    return pre, cleanup, args


def _cleanup_expr(cleanup: list[str]) -> str:
    return " ".join(cleanup) if cleanup else ""


def _generate_make_fn(
    struct: KotlinStruct,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> list[str]:
    name = struct.name
    pkg_path = struct.package.replace(".", "/")
    class_path = f"{pkg_path}/{name}" if pkg_path else name
    ctor_desc = _ctor_descriptor(
        struct.fields, all_structs, enum_names, struct.package, enum_packages, typealiases
    )

    lines: list[str] = []
    lines.append(f"inline jobject make_{name}(JNIEnv* env, const JNI_{name}& val) {{")
    lines.append(f'    jclass cls = env->FindClass("{class_path}");')
    lines.append("    if (!cls) return nullptr;")
    lines.append(f'    jmethodID ctor = env->GetMethodID(cls, "<init>", "{ctor_desc}");')
    lines.append("    if (!ctor) { env->DeleteLocalRef(cls); return nullptr; }")

    pre, cleanup, args = _make_pre_args(
        struct.fields, all_structs, enum_names, struct.package, enum_packages, typealiases
    )
    for line in pre:
        lines.append(line)

    arg_str = ", ".join(["cls, ctor"] + args)
    lines.append(f"    jobject result = env->NewObject({arg_str});")

    # Clean up temporaries (reverse order already established in cleanup list)
    for c in cleanup:
        lines.append(f"    {c}")
    lines.append("    if (!result) { env->DeleteLocalRef(cls); return nullptr; }")
    lines.append("    env->DeleteLocalRef(cls);")
    lines.append("    return result;")
    lines.append("}")
    return lines


# ---------------------------------------------------------------------------
# Top-level block generator
# ---------------------------------------------------------------------------


def generate_struct_block(
    struct: KotlinStruct,
    all_structs: dict[str, KotlinStruct],
    enum_names: frozenset[str] = frozenset(),
    enum_packages: dict[str, str] | None = None,
    typealiases: dict[str, str] | None = None,
) -> str:
    """Return the complete C++ helper block for *struct*."""
    name = struct.name
    lines: list[str] = []
    lines.append(f"// --- Struct marshalling: {name} ---")

    # C++ struct definition
    lines.append(f"struct JNI_{name} {{")
    for f in struct.fields:
        cpp_t = _cpp_field_type(f, all_structs, enum_names, typealiases)
        comment = ""
        if f.count_field:
            comment = f"  // size == {f.count_field}"
        if f.optional_pointer:
            comment = "  // optional pointer (nullable)"
        lines.append(f"    {cpp_t} {f.name};{comment}")
    lines.append("};")
    lines.append("")

    lines.extend(_generate_extract_fn(struct, all_structs, enum_names, enum_packages, typealiases))
    lines.append("")
    lines.extend(_generate_make_fn(struct, all_structs, enum_names, enum_packages, typealiases))

    return "\n".join(lines)


def generate_all_struct_blocks(parsed: ParsedFile) -> str:
    """Return the concatenated struct helper blocks needed for *parsed*."""
    needed = collect_needed_structs(parsed)
    if not needed:
        return ""
    ordered = topo_sort_structs(needed, parsed.struct_types)
    blocks: list[str] = []
    for name in ordered:
        blocks.append(
            generate_struct_block(
                parsed.struct_types[name],
                parsed.struct_types,
                parsed.enum_names,
                parsed.enum_packages,
                parsed.typealiases,
            )
        )
    return "\n\n".join(blocks) + "\n"
