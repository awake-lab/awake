from __future__ import annotations

from dataclasses import dataclass, field

# Exit codes
EXIT_OK = 0
EXIT_USAGE = 1  # no files / nothing to generate / bad path
EXIT_PARSE = 2  # unrecognized type or parse failure
EXIT_DRIFT = 3  # --check found out-of-date / missing output


@dataclass
class Param:
    name: str
    kotlin_type: str


@dataclass
class ExternalFunction:
    name: str
    params: list[Param]
    return_type: str | None
    line: int = 0  # 1-based line of the declaration in the source file


@dataclass
class KotlinField:
    """One primary-constructor property of a marshallable Kotlin class."""

    name: str
    kotlin_type: str  # base type, nullable marker stripped
    nullable: bool = False  # True when the field type ended with ?
    count_field: str | None = None  # --struct-config hint: field whose value == len(this array)
    optional_pointer: bool = False  # --struct-config hint: nullable single-element reference


@dataclass
class KotlinStruct:
    """A Kotlin data class (or eligible class) whose fields can be marshalled through JNI."""

    name: str
    package: str  # Kotlin package, e.g. "com.example.sample"
    fields: list[KotlinField]
    is_data_class: bool = False


@dataclass
class ParsedFile:
    package: str
    class_name: str
    is_static: bool  # object / companion -> static (jclass)
    functions: list[ExternalFunction] = field(default_factory=list)
    # Enum class names visible in the source set (populated by driver pre-pass).
    enum_names: frozenset[str] = field(default_factory=frozenset)
    # enum name -> declaring package (populated by driver pre-pass). Used instead of
    # assuming an enum lives in the same package as whatever references it.
    enum_packages: dict[str, str] = field(default_factory=dict)
    # Data/struct classes visible in the source set (populated by driver pre-pass).
    struct_types: dict[str, KotlinStruct] = field(default_factory=dict)
    # typealias name -> underlying type, one hop (populated by driver pre-pass).
    # Resolve chains via resolve_typealias() in _types.py.
    typealiases: dict[str, str] = field(default_factory=dict)
