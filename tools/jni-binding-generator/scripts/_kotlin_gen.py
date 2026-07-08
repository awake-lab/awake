"""
Reverse generator: C/C++ header → Kotlin external fun declarations.

Handles C-style APIs only (no templates, no virtual dispatch, no overloads).
Types not in the lookup table are mapped to Long with a TODO comment so the
output is always syntactically valid and immediately editable.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

# ─── C → Kotlin type tables ───────────────────────────────────────────────────

# Parameter direction: pointer arrays map to Kotlin *Array types
_C_PARAM_MAP: dict[str, str] = {
    "void*": "Long",
    "int": "Int",
    "int32_t": "Int",
    "int64_t": "Long",
    "unsigned int": "Int",
    "unsigned long": "Long",
    "long long": "Long",
    "long": "Long",
    "float": "Float",
    "double": "Double",
    "bool": "Boolean",
    "_Bool": "Boolean",
    "int16_t": "Short",
    "int8_t": "Byte",
    "uint8_t": "Byte",
    "uint16_t": "Int",
    "uint32_t": "Long",
    "size_t": "Long",
    "char*": "String",
    "float*": "FloatArray",
    "int32_t*": "IntArray",
    "uint8_t*": "ByteArray",
    "int8_t*": "ByteArray",
    "int64_t*": "LongArray",
    "double*": "DoubleArray",
    "int16_t*": "ShortArray",
    "short*": "ShortArray",
    "int*": "IntArray",
}

# Return direction: raw pointer returns are opaque handles (Long), not arrays
_C_RETURN_MAP: dict[str, str] = {
    **_C_PARAM_MAP,
    "void": "Unit",
    # Returned pointers are handles, not arrays
    "float*": "Long",
    "int32_t*": "Long",
    "uint8_t*": "Long",
    "int8_t*": "Long",
    "int64_t*": "Long",
    "double*": "Long",
    "int16_t*": "Long",
    "short*": "Long",
    "int*": "Long",
}

# ─── Source stripping ─────────────────────────────────────────────────────────

_BLOCK_COMMENT_RE = re.compile(r"/\*.*?\*/", re.DOTALL)
_LINE_COMMENT_RE = re.compile(r"//[^\n]*")
_PREPROCESSOR_RE = re.compile(r"^\s*#[^\n]*(?:\\\n[^\n]*)*", re.MULTILINE)
_STRUCT_BLOCK_RE = re.compile(
    r"(?:typedef\s+)?(?:struct|enum|union)\s+\w*\s*\{[^}]*\}\s*(?:\w+\s*)?;", re.DOTALL
)
_TYPEDEF_RE = re.compile(r"\btypedef\b[^;]+;", re.DOTALL)
_EXTERN_C_OPEN_RE = re.compile(r'extern\s+"C"\s*\{')
_EXTERN_C_CLOSE_RE = re.compile(r"^[ \t]*\}[ \t]*$", re.MULTILINE)
_ATTR_RE = re.compile(r"__attribute__\s*\(\s*\([^)]*\)\s*\)")
_DECLSPEC_RE = re.compile(r"__declspec\s*\([^)]*\)")


def _strip_c_source(source: str) -> str:
    """Remove comments, preprocessor, struct/typedef blocks, and compiler attrs."""
    source = _BLOCK_COMMENT_RE.sub("", source)
    source = _LINE_COMMENT_RE.sub("", source)
    source = _PREPROCESSOR_RE.sub("", source)
    source = _STRUCT_BLOCK_RE.sub("", source)
    source = _TYPEDEF_RE.sub("", source)
    # Strip extern "C" { ... } wrapper lines (keep the declarations inside)
    source = _EXTERN_C_OPEN_RE.sub("", source)
    source = _EXTERN_C_CLOSE_RE.sub("", source)
    source = _ATTR_RE.sub("", source)
    source = _DECLSPEC_RE.sub("", source)
    return source


# ─── Type normalization ───────────────────────────────────────────────────────


def _normalize_c_type(raw: str) -> str:
    """Strip qualifiers, collapse whitespace, normalize pointer spacing."""
    t = raw.strip()
    t = re.sub(r"\s+", " ", t)
    t = re.sub(r"\s*\*\s*", "*", t)  # "int *" → "int*", "char * " → "char*"
    t = re.sub(r"\bconst\b\s*", "", t)
    t = re.sub(r"\bvolatile\b\s*", "", t)
    t = re.sub(r"\brestrict\b\s*", "", t)
    t = t.strip()
    return t


# ─── Name helpers ─────────────────────────────────────────────────────────────

_SNAKE_RE = re.compile(r"_([a-z0-9])")


def _snake_to_camel(name: str) -> str:
    """Convert snake_case to camelCase."""
    return _SNAKE_RE.sub(lambda m: m.group(1).upper(), name)


def _header_to_object_name(path: Path) -> str:
    """Derive a Kotlin object name from a header filename.

    'my_engine.h'  → 'MyEngine'
    'jni-utils.h'  → 'JniUtils'
    'libfoo.h'     → 'Libfoo'
    """
    stem = path.stem.replace("-", "_")
    parts = [p for p in stem.split("_") if p]
    return "".join(p.capitalize() for p in parts) or "Native"


# ─── C function declaration parser ───────────────────────────────────────────


@dataclass
class KotlinParam:
    name: str  # camelCase Kotlin parameter name
    kotlin_type: str  # Kotlin type (may include "// TODO: ..." for unknowns)
    c_type: str  # original normalized C type


@dataclass
class KotlinFun:
    name: str  # camelCase Kotlin function name
    params: list[KotlinParam] = field(default_factory=list)
    return_type: str = "Unit"
    c_name: str = ""  # original C function name
    c_return: str = "void"  # original normalized C return type


# Matches a C function *declaration* (ends with ;, not {).
# The non-greedy return-type group ([\w \t*]+?) with backtracking naturally
# leaves the last identifier before ( as the function name.
_C_FUNC_RE = re.compile(
    r"^[ \t]*"
    r"([\w \t*]+?)"  # return type (no newlines, non-greedy)
    r"\s+(\*+)?\s*(\w+)\s*"  # optional return pointer attached to function name
    r"\(([^)]*)\)\s*;",  # param list + semicolon
    re.MULTILINE,
)

# C identifiers that can only be type keywords, never function names
_TYPE_ONLY = frozenset(
    "int long short char float double void bool unsigned signed "
    "const volatile static inline extern register".split()
)


def _parse_c_params(raw: str) -> list[KotlinParam]:
    """Parse a raw C parameter list string into KotlinParam entries."""
    raw = raw.strip()
    if not raw or raw == "void":
        return []

    params: list[KotlinParam] = []
    for idx, chunk in enumerate(raw.split(",")):
        chunk = chunk.strip()
        if not chunk:
            continue
        # Normalize array notation: "int buf[]" → "int*"
        chunk = re.sub(r"\[\s*\w*\s*\]", "*", chunk)

        tokens = chunk.split()
        if not tokens:
            continue

        # Determine if last token is a param name or a type token.
        last = tokens[-1].lstrip("*")
        if len(tokens) == 1 or not last or last.lower() in _TYPE_ONLY:
            raw_type = chunk
            param_name = f"param{idx}"
        else:
            # Leading * on the name belongs to the type
            ptr_prefix = "*" * (len(tokens[-1]) - len(tokens[-1].lstrip("*")))
            raw_type = " ".join(tokens[:-1]) + ptr_prefix
            param_name = last

        # Distinguish const char* (input string) from char* (mutable output buffer).
        # _normalize_c_type strips 'const', so we detect it before normalizing.
        is_mutable_char_ptr = (
            bool(re.search(r"(?<!\bconst\b)\s*char\s*\*", raw_type)) and "const" not in raw_type
        )
        norm_type = _normalize_c_type(raw_type)
        if norm_type == "char*" and is_mutable_char_ptr:
            mapped: str | None = "ByteArray"
        else:
            mapped = _C_PARAM_MAP.get(norm_type)
        kotlin_type = mapped if mapped is not None else f"Long /* TODO: {norm_type} */"

        params.append(
            KotlinParam(
                name=_snake_to_camel(param_name),
                kotlin_type=kotlin_type,
                c_type=norm_type,
            )
        )
    return params


def parse_c_header(source: str) -> list[KotlinFun]:
    """Parse a C/C++ header source string, returning one KotlinFun per declaration."""
    stripped = _strip_c_source(source)
    funs: list[KotlinFun] = []
    seen: set[str] = set()

    for m in _C_FUNC_RE.finditer(stripped):
        raw_ret = (m.group(1).strip() + (m.group(2) or "")).strip()
        c_name = m.group(3).strip()
        raw_params = m.group(4).strip()

        # Skip type keywords, operators, destructors, and duplicate names
        if c_name.lower() in _TYPE_ONLY:
            continue
        if c_name.startswith(("~", "operator")):
            continue
        if c_name in seen:
            continue
        seen.add(c_name)

        norm_ret = _normalize_c_type(raw_ret)
        # Strip leading storage-class specifiers from the return type
        for spec in ("static ", "inline ", "extern ", "__inline "):
            norm_ret = norm_ret.replace(spec, "")
        norm_ret = norm_ret.strip()

        mapped_ret = _C_RETURN_MAP.get(norm_ret)
        kotlin_ret = mapped_ret if mapped_ret is not None else f"Long /* TODO: {norm_ret} */"

        params = _parse_c_params(raw_params)
        funs.append(
            KotlinFun(
                name=_snake_to_camel(c_name),
                params=params,
                return_type=kotlin_ret,
                c_name=c_name,
                c_return=norm_ret,
            )
        )

    return funs


# ─── Kotlin stub file generator ──────────────────────────────────────────────

_KT_HEADER = """\
// AUTO-GENERATED by jni-binding-generator.py --kotlin-from-header — DO NOT EDIT.
// Source: {source}
//
// Review every signature before use:
//   - void* params are mapped to Long (opaque native handle convention)
//   - Pointer-array params (float*, int32_t* …) are mapped to *Array
//   - Params/returns marked TODO could not be mapped — fill them in manually
//
// Load the native library once (add to the companion object or Application):
//   System.loadLibrary("{lib_name}")

package {package}

object {object_name} {{
"""


def generate_kotlin_stubs(
    source: str,
    source_name: str,
    package: str = "",
    object_name: str = "",
    strict_types: bool = False,
) -> str:
    """Generate a .kt file content with external fun stubs for each C declaration."""
    funs = parse_c_header(source)
    if not funs:
        return ""

    if strict_types:
        unmapped: list[str] = []
        for fun in funs:
            for p in fun.params:
                if "/* TODO:" in p.kotlin_type:
                    unmapped.append(f"  {fun.c_name}(): param '{p.name}' → {p.c_type}")
            if "/* TODO:" in fun.return_type:
                unmapped.append(f"  {fun.c_name}(): return → {fun.c_return}")
        if unmapped:
            detail = "\n".join(unmapped)
            raise ValueError(
                f"--strict-types: {len(unmapped)} unmapped type(s) in {source_name}:\n{detail}"
            )

    pkg = package if package else "/* TODO: set your package */"
    lib = re.sub(r"[^a-zA-Z0-9_]", "", object_name.lower()) or "native"

    lines = [
        _KT_HEADER.format(source=source_name, package=pkg, object_name=object_name, lib_name=lib)
    ]

    for fun in funs:
        params_str = ", ".join(f"{p.name}: {p.kotlin_type}" for p in fun.params)
        if fun.return_type == "Unit":
            lines.append(f"    external fun {fun.name}({params_str})")
        else:
            lines.append(f"    external fun {fun.name}({params_str}): {fun.return_type}")

    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def parse_c_header_file(path: Path) -> list[KotlinFun]:
    return parse_c_header(path.read_text(encoding="utf-8"))


def generate_kotlin_from_header(
    header_path: Path,
    output_dir: Path,
    package: str = "",
) -> Path | None:
    """Parse *header_path* and write a .kt stub file to *output_dir*.

    Returns the output path, or None if no functions were found.
    """
    source = header_path.read_text(encoding="utf-8")
    obj_name = _header_to_object_name(header_path)
    content = generate_kotlin_stubs(source, header_path.name, package=package, object_name=obj_name)
    if not content:
        return None
    output_dir.mkdir(parents=True, exist_ok=True)
    out_path = output_dir / f"{obj_name}.kt"
    out_path.write_text(content, encoding="utf-8")
    return out_path
