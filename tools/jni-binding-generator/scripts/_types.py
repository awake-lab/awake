from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class TypeInfo:
    """How a Kotlin type maps onto the JNI and C++ worlds."""

    jni_type: str  # type used in the JNI signature, e.g. "jstring"
    cpp_type: str  # local C++ variable type, e.g. "std::string"
    # marshalling expression; ``{env}`` and ``{var}`` are substituted.
    convert: str | None  # None means "no local var needed" (e.g. void)
    is_handle: bool = False  # Long handles get a null-check by default
    is_string: bool = False  # strings get an empty-check helper available
    is_enum: bool = False  # True when type is a confirmed Kotlin enum class


def resolve_typealias(base: str, typealiases: dict[str, str] | None = None) -> str:
    """Follow a chain of ``typealias`` declarations to the ultimate underlying type.

    ``typealiases`` maps each alias to its immediate underlying type (one hop); this
    walks the chain (e.g. ``VkBufferCreateFlags -> VkFlags -> Int``) until it reaches
    a name that isn't itself an alias, or detects a cycle (returned as-is — a cycle
    means the source set is malformed, not something this function should paper over).
    """
    if not typealiases:
        return base
    seen: set[str] = set()
    while base in typealiases and base not in seen:
        seen.add(base)
        base = typealiases[base]
    return base


class UnknownTypeError(ValueError):
    """Raised when a Kotlin type has no mapping. The message is actionable."""


# Kotlin (nullability stripped) -> TypeInfo.
#
# ``Long`` is treated as an opaque native handle (the dominant JNI convention
# for passing a ``void*`` across the boundary). A plain numeric long is rare in
# these signatures; callers who need one can post-process the int64_t value.
TYPE_MAP: dict[str, TypeInfo] = {
    "Int": TypeInfo("jint", "int32_t", "static_cast<int32_t>({var})"),
    "Long": TypeInfo("jlong", "void*", "reinterpret_cast<void*>({var})", is_handle=True),
    "Float": TypeInfo("jfloat", "float", "static_cast<float>({var})"),
    "Double": TypeInfo("jdouble", "double", "static_cast<double>({var})"),
    "Boolean": TypeInfo("jboolean", "bool", "({var} == JNI_TRUE)"),
    "Short": TypeInfo("jshort", "int16_t", "static_cast<int16_t>({var})"),
    "Byte": TypeInfo("jbyte", "int8_t", "static_cast<int8_t>({var})"),
    "String": TypeInfo("jstring", "std::string", "jstring2string({env}, {var})", is_string=True),
    "ByteArray": TypeInfo("jbyteArray", "std::vector<uint8_t>", "extract_byte_array({env}, {var})"),
    "FloatArray": TypeInfo(
        "jfloatArray", "std::vector<float>", "extract_float_array({env}, {var})"
    ),
    "IntArray": TypeInfo("jintArray", "std::vector<int32_t>", "extract_int_array({env}, {var})"),
    "LongArray": TypeInfo("jlongArray", "std::vector<int64_t>", "extract_long_array({env}, {var})"),
    "ShortArray": TypeInfo(
        "jshortArray", "std::vector<int16_t>", "extract_short_array({env}, {var})"
    ),
    "DoubleArray": TypeInfo(
        "jdoubleArray", "std::vector<double>", "extract_double_array({env}, {var})"
    ),
    "BooleanArray": TypeInfo(
        "jbooleanArray", "std::vector<bool>", "extract_bool_array({env}, {var})"
    ),
    "Array<String>": TypeInfo(
        "jobjectArray", "std::vector<std::string>", "extract_string_array({env}, {var})"
    ),
    # java.util.List variants — arrive as jobject, unboxed to std::vector
    "List<String>": TypeInfo(
        "jobject", "std::vector<std::string>", "extract_list_string({env}, {var})"
    ),
    "List<Int>": TypeInfo("jobject", "std::vector<int32_t>", "extract_list_int({env}, {var})"),
    "List<Long>": TypeInfo("jobject", "std::vector<int64_t>", "extract_list_long({env}, {var})"),
    "List<Float>": TypeInfo("jobject", "std::vector<float>", "extract_list_float({env}, {var})"),
    "List<Double>": TypeInfo("jobject", "std::vector<double>", "extract_list_double({env}, {var})"),
    "List<Boolean>": TypeInfo("jobject", "std::vector<bool>", "extract_list_bool({env}, {var})"),
    "List<Byte>": TypeInfo("jobject", "std::vector<int8_t>", "extract_list_byte({env}, {var})"),
    # Kotlin Array<T> of boxed types — jobjectArray, unboxed to std::vector
    "Array<Int>": TypeInfo(
        "jobjectArray", "std::vector<int32_t>", "extract_boxed_int_array({env}, {var})"
    ),
    "Array<Long>": TypeInfo(
        "jobjectArray", "std::vector<int64_t>", "extract_boxed_long_array({env}, {var})"
    ),
    "Array<Float>": TypeInfo(
        "jobjectArray", "std::vector<float>", "extract_boxed_float_array({env}, {var})"
    ),
    "Array<Double>": TypeInfo(
        "jobjectArray", "std::vector<double>", "extract_boxed_double_array({env}, {var})"
    ),
    # Nested collections
    "List<List<String>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<std::string>>",
        "extract_list_list_string({env}, {var})",
    ),
    # java.util.Set variants
    "Set<String>": TypeInfo(
        "jobject", "std::unordered_set<std::string>", "extract_set_string({env}, {var})"
    ),
    "Set<Int>": TypeInfo("jobject", "std::unordered_set<int32_t>", "extract_set_int({env}, {var})"),
    "Set<Long>": TypeInfo(
        "jobject", "std::unordered_set<int64_t>", "extract_set_long({env}, {var})"
    ),
    "Set<Float>": TypeInfo(
        "jobject", "std::unordered_set<float>", "extract_set_float({env}, {var})"
    ),
    "Set<Boolean>": TypeInfo(
        "jobject", "std::unordered_set<bool>", "extract_set_bool({env}, {var})"
    ),
    "Set<Double>": TypeInfo(
        "jobject", "std::unordered_set<double>", "extract_set_double({env}, {var})"
    ),
    # java.util.List — Short variant (completes the primitive family)
    "List<Short>": TypeInfo("jobject", "std::vector<int16_t>", "extract_list_short({env}, {var})"),
    # Nested List<List<T>> variants
    "List<List<Int>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<int32_t>>",
        "extract_list_list_int({env}, {var})",
    ),
    "List<List<Float>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<float>>",
        "extract_list_list_float({env}, {var})",
    ),
    "List<List<Long>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<int64_t>>",
        "extract_list_list_long({env}, {var})",
    ),
    "List<List<Double>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<double>>",
        "extract_list_list_double({env}, {var})",
    ),
    "List<List<Boolean>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<bool>>",
        "extract_list_list_bool({env}, {var})",
    ),
    "List<List<Short>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<int16_t>>",
        "extract_list_list_short({env}, {var})",
    ),
    "List<List<Byte>>": TypeInfo(
        "jobject",
        "std::vector<std::vector<int8_t>>",
        "extract_list_list_byte({env}, {var})",
    ),
    "Set<Byte>": TypeInfo(
        "jobject", "std::unordered_set<int8_t>", "extract_set_byte({env}, {var})"
    ),
    "Set<Short>": TypeInfo(
        "jobject", "std::unordered_set<int16_t>", "extract_set_short({env}, {var})"
    ),
    # Boxed Array<T> for remaining scalar types
    "Array<Byte>": TypeInfo(
        "jobjectArray", "std::vector<int8_t>", "extract_boxed_byte_array({env}, {var})"
    ),
    "Array<Boolean>": TypeInfo(
        "jobjectArray", "std::vector<bool>", "extract_boxed_bool_array({env}, {var})"
    ),
    "Array<Short>": TypeInfo(
        "jobjectArray", "std::vector<int16_t>", "extract_boxed_short_array({env}, {var})"
    ),
    # java.util.Map variants
    "Map<String, String>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, std::string>",
        "extract_map_string_string({env}, {var})",
    ),
    "Map<String, Int>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, int32_t>",
        "extract_map_string_int({env}, {var})",
    ),
    "Map<String, Long>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, int64_t>",
        "extract_map_string_long({env}, {var})",
    ),
    "Map<String, Float>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, float>",
        "extract_map_string_float({env}, {var})",
    ),
    "Map<String, Boolean>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, bool>",
        "extract_map_string_bool({env}, {var})",
    ),
    "Map<Int, String>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, std::string>",
        "extract_map_int_string({env}, {var})",
    ),
    "Map<Int, Int>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, int32_t>",
        "extract_map_int_int({env}, {var})",
    ),
    "Map<Int, Long>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, int64_t>",
        "extract_map_int_long({env}, {var})",
    ),
    "Map<Int, Float>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, float>",
        "extract_map_int_float({env}, {var})",
    ),
    "Map<Int, Boolean>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, bool>",
        "extract_map_int_bool({env}, {var})",
    ),
    "Map<String, Double>": TypeInfo(
        "jobject",
        "std::unordered_map<std::string, double>",
        "extract_map_string_double({env}, {var})",
    ),
    "Map<Int, Double>": TypeInfo(
        "jobject",
        "std::unordered_map<int32_t, double>",
        "extract_map_int_double({env}, {var})",
    ),
    "Map<Long, Int>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, int32_t>",
        "extract_map_long_int({env}, {var})",
    ),
    "Map<Long, Long>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, int64_t>",
        "extract_map_long_long({env}, {var})",
    ),
    "Map<Long, String>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, std::string>",
        "extract_map_long_string({env}, {var})",
    ),
    "Map<Long, Float>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, float>",
        "extract_map_long_float({env}, {var})",
    ),
    "Map<Long, Double>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, double>",
        "extract_map_long_double({env}, {var})",
    ),
    "Map<Long, Boolean>": TypeInfo(
        "jobject",
        "std::unordered_map<int64_t, bool>",
        "extract_map_long_bool({env}, {var})",
    ),
    "Unit": TypeInfo("void", "void", None),
}

# Return-type JNI mapping plus the "empty" value to return on an error path.
RETURN_MAP: dict[str, tuple[str, str]] = {
    "Int": ("jint", "0"),
    "Long": ("jlong", "0"),
    "Float": ("jfloat", "0.0f"),
    "Double": ("jdouble", "0.0"),
    "Boolean": ("jboolean", "JNI_FALSE"),
    "Short": ("jshort", "0"),
    "Byte": ("jbyte", "0"),
    "String": ("jstring", "nullptr"),
    "ByteArray": ("jbyteArray", "nullptr"),
    "FloatArray": ("jfloatArray", "nullptr"),
    "IntArray": ("jintArray", "nullptr"),
    "LongArray": ("jlongArray", "nullptr"),
    "ShortArray": ("jshortArray", "nullptr"),
    "DoubleArray": ("jdoubleArray", "nullptr"),
    "BooleanArray": ("jbooleanArray", "nullptr"),
    "Array<String>": ("jobjectArray", "nullptr"),
    "List<String>": ("jobject", "nullptr"),
    "List<Int>": ("jobject", "nullptr"),
    "List<Long>": ("jobject", "nullptr"),
    "List<Float>": ("jobject", "nullptr"),
    "List<Double>": ("jobject", "nullptr"),
    "List<Boolean>": ("jobject", "nullptr"),
    "List<Byte>": ("jobject", "nullptr"),
    "Array<Int>": ("jobjectArray", "nullptr"),
    "Array<Long>": ("jobjectArray", "nullptr"),
    "Array<Float>": ("jobjectArray", "nullptr"),
    "Array<Double>": ("jobjectArray", "nullptr"),
    "List<Short>": ("jobject", "nullptr"),
    "List<List<String>>": ("jobject", "nullptr"),
    "List<List<Int>>": ("jobject", "nullptr"),
    "List<List<Float>>": ("jobject", "nullptr"),
    "List<List<Long>>": ("jobject", "nullptr"),
    "List<List<Double>>": ("jobject", "nullptr"),
    "List<List<Boolean>>": ("jobject", "nullptr"),
    "List<List<Short>>": ("jobject", "nullptr"),
    "List<List<Byte>>": ("jobject", "nullptr"),
    "Set<Byte>": ("jobject", "nullptr"),
    "Set<Short>": ("jobject", "nullptr"),
    "Set<String>": ("jobject", "nullptr"),
    "Set<Int>": ("jobject", "nullptr"),
    "Set<Long>": ("jobject", "nullptr"),
    "Set<Float>": ("jobject", "nullptr"),
    "Set<Boolean>": ("jobject", "nullptr"),
    "Set<Double>": ("jobject", "nullptr"),
    "Array<Byte>": ("jobjectArray", "nullptr"),
    "Array<Boolean>": ("jobjectArray", "nullptr"),
    "Array<Short>": ("jobjectArray", "nullptr"),
    "Map<String, String>": ("jobject", "nullptr"),
    "Map<String, Int>": ("jobject", "nullptr"),
    "Map<String, Long>": ("jobject", "nullptr"),
    "Map<String, Float>": ("jobject", "nullptr"),
    "Map<String, Boolean>": ("jobject", "nullptr"),
    "Map<Int, String>": ("jobject", "nullptr"),
    "Map<Int, Int>": ("jobject", "nullptr"),
    "Map<Int, Long>": ("jobject", "nullptr"),
    "Map<Int, Float>": ("jobject", "nullptr"),
    "Map<Int, Boolean>": ("jobject", "nullptr"),
    "Map<String, Double>": ("jobject", "nullptr"),
    "Map<Int, Double>": ("jobject", "nullptr"),
    "Map<Long, Double>": ("jobject", "nullptr"),
    "Map<Long, Int>": ("jobject", "nullptr"),
    "Map<Long, Long>": ("jobject", "nullptr"),
    "Map<Long, String>": ("jobject", "nullptr"),
    "Map<Long, Float>": ("jobject", "nullptr"),
    "Map<Long, Boolean>": ("jobject", "nullptr"),
    "Unit": ("void", ""),
    None: ("void", ""),
}

# Kotlin return type -> (make_helper_fn, cpp_type) for collection returns.
# Module-level so both generate_function() and generate_test_file() share it.
_MAKE_HELPER_MAP: dict[str, tuple[str, str]] = {
    "List<String>": ("make_list_string", "std::vector<std::string>"),
    "List<Int>": ("make_list_int", "std::vector<int32_t>"),
    "List<Long>": ("make_list_long", "std::vector<int64_t>"),
    "List<Float>": ("make_list_float", "std::vector<float>"),
    "List<Double>": ("make_list_double", "std::vector<double>"),
    "List<Boolean>": ("make_list_bool", "std::vector<bool>"),
    "List<Byte>": ("make_list_byte", "std::vector<int8_t>"),
    "List<Short>": ("make_list_short", "std::vector<int16_t>"),
    "List<List<String>>": ("make_list_list_string", "std::vector<std::vector<std::string>>"),
    "Set<String>": ("make_set_string", "std::unordered_set<std::string>"),
    "Set<Int>": ("make_set_int", "std::unordered_set<int32_t>"),
    "Set<Long>": ("make_set_long", "std::unordered_set<int64_t>"),
    "Set<Float>": ("make_set_float", "std::unordered_set<float>"),
    "Set<Boolean>": ("make_set_bool", "std::unordered_set<bool>"),
    "Set<Double>": ("make_set_double", "std::unordered_set<double>"),
    "List<List<Int>>": ("make_list_list_int", "std::vector<std::vector<int32_t>>"),
    "List<List<Float>>": ("make_list_list_float", "std::vector<std::vector<float>>"),
    "List<List<Long>>": ("make_list_list_long", "std::vector<std::vector<int64_t>>"),
    "List<List<Double>>": ("make_list_list_double", "std::vector<std::vector<double>>"),
    "List<List<Boolean>>": ("make_list_list_bool", "std::vector<std::vector<bool>>"),
    "List<List<Short>>": ("make_list_list_short", "std::vector<std::vector<int16_t>>"),
    "List<List<Byte>>": ("make_list_list_byte", "std::vector<std::vector<int8_t>>"),
    "Set<Byte>": ("make_set_byte", "std::unordered_set<int8_t>"),
    "Set<Short>": ("make_set_short", "std::unordered_set<int16_t>"),
    "Map<Int, Int>": ("make_map_int_int", "std::unordered_map<int32_t, int32_t>"),
    "Map<Int, Long>": ("make_map_int_long", "std::unordered_map<int32_t, int64_t>"),
    "Map<Int, Float>": ("make_map_int_float", "std::unordered_map<int32_t, float>"),
    "Map<Int, Boolean>": ("make_map_int_bool", "std::unordered_map<int32_t, bool>"),
    "Map<String, String>": (
        "make_map_string_string",
        "std::unordered_map<std::string, std::string>",
    ),
    "Map<String, Int>": ("make_map_string_int", "std::unordered_map<std::string, int32_t>"),
    "Map<String, Long>": ("make_map_string_long", "std::unordered_map<std::string, int64_t>"),
    "Map<String, Float>": ("make_map_string_float", "std::unordered_map<std::string, float>"),
    "Map<String, Boolean>": ("make_map_string_bool", "std::unordered_map<std::string, bool>"),
    "Map<Int, String>": ("make_map_int_string", "std::unordered_map<int32_t, std::string>"),
    "Map<String, Double>": ("make_map_string_double", "std::unordered_map<std::string, double>"),
    "Map<Int, Double>": ("make_map_int_double", "std::unordered_map<int32_t, double>"),
    "Map<Long, Int>": ("make_map_long_int", "std::unordered_map<int64_t, int32_t>"),
    "Map<Long, Long>": ("make_map_long_long", "std::unordered_map<int64_t, int64_t>"),
    "Map<Long, String>": ("make_map_long_string", "std::unordered_map<int64_t, std::string>"),
    "Map<Long, Float>": ("make_map_long_float", "std::unordered_map<int64_t, float>"),
    "Map<Long, Double>": ("make_map_long_double", "std::unordered_map<int64_t, double>"),
    "Map<Long, Boolean>": ("make_map_long_bool", "std::unordered_map<int64_t, bool>"),
    # Boxed Array<T> return helpers (complement the existing extract_boxed_*_array params)
    "Array<String>": ("make_boxed_string_array", "std::vector<std::string>"),
    "Array<Byte>": ("make_boxed_byte_array", "std::vector<int8_t>"),
    "Array<Short>": ("make_boxed_short_array", "std::vector<int16_t>"),
    "Array<Int>": ("make_boxed_int_array", "std::vector<int32_t>"),
    "Array<Long>": ("make_boxed_long_array", "std::vector<int64_t>"),
    "Array<Float>": ("make_boxed_float_array", "std::vector<float>"),
    "Array<Double>": ("make_boxed_double_array", "std::vector<double>"),
    "Array<Boolean>": ("make_boxed_bool_array", "std::vector<bool>"),
}

_ENUM_RE = re.compile(r"^[A-Z][A-Za-z0-9_]*$")
_ENUM_TYPE = TypeInfo("jobject", "int32_t", "enum_ordinal({env}, {var})", is_enum=True)


def _looks_like_enum(base: str) -> bool:
    """True when *base* is a simple capitalized identifier (no generics)."""
    return bool(_ENUM_RE.match(base))


def _is_nullable(kotlin_type: str) -> bool:
    return kotlin_type.strip().endswith("?")


def _needs_exception_check(info: TypeInfo) -> bool:
    """True for TypeInfos whose conversion calls a JNI API that can pend an exception.

    Strings call GetStringUTFChars; arrays call Get*ArrayRegion / GetObjectArrayElement.
    Primitive casts are pure C and never pend exceptions.
    """
    return (
        info.is_string
        or info.cpp_type.startswith(("std::vector", "std::unordered_map", "std::unordered_set"))
        or info.is_enum
    )


def map_param_type(
    kotlin_type: str,
    known_enums: frozenset[str] = frozenset(),
    typealiases: dict[str, str] | None = None,
) -> TypeInfo:
    """Map a Kotlin parameter type to a TypeInfo.

    *known_enums* must be supplied for any source set that uses ``enum class``
    types as parameters.  An unknown capitalized identifier that is not in
    *known_enums* raises ``UnknownTypeError`` — the silent heuristic that
    previously guessed enums from any capitalised name has been removed.
    *typealiases* resolves ``typealias`` chains (e.g. ``VkDeviceSize -> Long``)
    to their underlying type before either lookup.
    """
    base = resolve_typealias(kotlin_type.rstrip("?").strip(), typealiases)
    if base in TYPE_MAP:
        return TYPE_MAP[base]
    if base in known_enums:
        return _ENUM_TYPE
    raise UnknownTypeError(
        f"unrecognized parameter type '{kotlin_type}'. "
        f"If '{base}' is a Kotlin enum class, ensure its declaration is in the source set. "
        f"If it is a data/struct class, declare it with a primary constructor so the generator "
        f"can detect it, or add a custom mapping via --type-map."
    )


def map_return_type(
    kotlin_type: str | None,
    known_enums: frozenset[str] = frozenset(),
    typealiases: dict[str, str] | None = None,
) -> tuple[str, str]:
    """Map a Kotlin return type to a (jni_type, empty_value) pair.

    Confirmed enum classes return ``jobject`` (the enum instance, retrieved
    via ``values()[ordinal]``).  Unknown capitalized types raise. *typealiases*
    resolves alias chains to their underlying type before either lookup.
    """
    base = resolve_typealias(kotlin_type.rstrip("?").strip(), typealiases) if kotlin_type else None
    if base in RETURN_MAP:
        return RETURN_MAP[base]
    if base and base in known_enums:
        return ("jobject", "nullptr")  # enum instance, not raw ordinal
    raise UnknownTypeError(
        f"unrecognized return type '{kotlin_type}'. "
        f"If '{base}' is a Kotlin enum class, ensure its declaration is in the source set. "
        f"If it is a data/struct class, declare it with a primary constructor so the generator "
        f"can detect it, or add a custom mapping via --type-map."
    )


def load_type_map(path: Path) -> None:
    """Merge custom Kotlin→JNI type mappings from a JSON file into the module-level maps.

    JSON schema (all sections optional):
    {
      "types": {
        "MyHandle": {
          "jni_type": "jlong",
          "cpp_type": "void*",
          "convert": "reinterpret_cast<void*>({var})",
          "is_handle": true
        }
      },
      "returns": {
        "MyHandle": ["jlong", "0"]
      },
      "make_helpers": {
        "MyList": ["make_my_list", "std::vector<MyItem>"]
      }
    }

    Custom types override built-ins if the same key is present in both.
    """
    import json

    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError(f"could not load type map '{path}': {exc}") from exc

    for kotlin_type, info in data.get("types", {}).items():
        TYPE_MAP[kotlin_type] = TypeInfo(
            jni_type=info["jni_type"],
            cpp_type=info["cpp_type"],
            convert=info.get("convert"),
            is_handle=bool(info.get("is_handle", False)),
            is_string=bool(info.get("is_string", False)),
        )

    for kotlin_type, pair in data.get("returns", {}).items():
        RETURN_MAP[kotlin_type] = (pair[0], pair[1])

    for kotlin_type, pair in data.get("make_helpers", {}).items():
        _MAKE_HELPER_MAP[kotlin_type] = (pair[0], pair[1])
