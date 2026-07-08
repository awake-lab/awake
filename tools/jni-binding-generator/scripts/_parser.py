from __future__ import annotations

import re
from pathlib import Path

from _models import ExternalFunction, KotlinField, KotlinStruct, Param, ParsedFile
from _types import UnknownTypeError

_PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)", re.MULTILINE)
# First top-level class or object declaration (captures nesting: "class Outer { class Inner").
_DECL_RE = re.compile(r"\b(class|object)\s+(\w+)")

# --- Enum class detection ---
_ENUM_CLASS_RE = re.compile(r"\benum\s+class\s+(\w+)")

# --- typealias detection ---
# Matches "typealias Name = Underlying" for simple (non-generic) aliases only —
# e.g. "typealias VkDeviceSize = Long" or "typealias VkBufferCreateFlags = VkFlags".
_TYPEALIAS_RE = re.compile(r"\btypealias\s+(\w+)\s*=\s*([\w.]+)\b")

# --- Struct/data class detection ---
# Matches "data class Foo(" or "class Foo(" at the start of a primary constructor.
# Skips generic type params on the class itself (e.g. "class Foo<T>(").
_STRUCT_CLASS_RE = re.compile(r"\b(data\s+class|class)\s+(\w+)\s*(?:<[^>]*>)?\s*\(")
# Kotlin file name for top-level funs: "foo/Bar.kt" → "BarKt"
_FILENAME_KT_RE = re.compile(r"([A-Za-z0-9_]+)\.kt$")
# @JvmName("altName") immediately before an external fun.
_JVM_NAME_RE = re.compile(r'@JvmName\s*\(\s*"(\w+)"\s*\)')
# Detect unsupported constructs that must be rejected before code-gen.
# Both "suspend external fun" and "external suspend fun" are valid Kotlin.
_SUSPEND_RE = re.compile(r"\b(?:suspend\s+external|external\s+suspend)\s+fun\b")
_EXTENSION_FUN_RE = re.compile(r"\bexternal\s+fun\s+\w+\.")
_VARARG_RE = re.compile(r"\bvararg\s+\w+\s*:")
_FN_TYPE_RE = re.compile(r":\s*\(")  # function-type param: "cb: (Int) -> String"
# Matches only "external fun name(" — the parameter list itself is extracted
# separately via paren-balancing (see _find_matching_close), not by this regex.
# A naive ".*?\)" here would truncate at the *first* ")" in the source, which
# breaks the moment any annotation argument list — e.g. "@Foo(\"x\") p: Int" —
# appears before the parameter list's real closing paren.
_EXTERNAL_FUN_START_RE = re.compile(r"external\s+fun\s+(\w+)\s*\(")
_RETURN_TYPE_RE = re.compile(r"\A\s*:\s*([\w.]+(?:<(?:[^<>]|<[^<>]*>)*>)?\??)")

_BLOCK_COMMENT_RE = re.compile(r"/\*.*?\*/", re.DOTALL)
_LINE_COMMENT_RE = re.compile(r"//[^\n]*")

# One Kotlin annotation name, with an optional use-site target (@field:, @get:, @param:, …).
_ANNOTATION_NAME_RE = re.compile(r"@(?:[A-Za-z_]\w*:)?[A-Za-z_]\w*")


def _find_matching_close(
    source: str, open_pos: int, open_ch: str = "(", close_ch: str = ")"
) -> int:
    """Return the index of the char matching *open_ch* at *open_pos*, or -1.

    Balances nested *open_ch*/*close_ch* pairs starting at *open_pos* (which must
    itself be *open_ch*). Skips over double-quoted string literals (honouring
    backslash escapes) so a stray paren/bracket inside a string argument — e.g.
    an annotation argument like ``@Foo("a, b")`` — never desyncs the count.
    """
    depth = 0
    i = open_pos
    in_string = False
    while i < len(source):
        c = source[i]
        if in_string:
            if c == "\\":
                i += 1
            elif c == '"':
                in_string = False
        elif c == '"':
            in_string = True
        elif c == open_ch:
            depth += 1
        elif c == close_ch:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def _strip_leading_annotations(text: str) -> str:
    """Strip one or more leading Kotlin annotations from *text*.

    Handles ``@Foo``, ``@Foo(args)``, and ``@site:Foo(args)`` (use-site targets
    like ``field:``/``get:``/``param:``), with any whitespace/newlines between
    stacked annotations and the declaration that follows. Annotation argument
    lists are paren-balanced (via :func:`_find_matching_close`), so an argument
    containing nested parens or commas — e.g. ``@Foo("a", "b")`` — is skipped as
    a whole, never mistaken for the end of the annotation or split upstream.

    This is shared by both the struct/data-class property parser and the
    function-parameter parser so annotation handling can't drift between them.
    """
    text = text.lstrip()
    while text.startswith("@"):
        m = _ANNOTATION_NAME_RE.match(text)
        if not m:
            break  # malformed '@' — let the caller's own validation raise
        rest = text[m.end() :]
        stripped_rest = rest.lstrip()
        if stripped_rest.startswith("("):
            paren_pos = len(rest) - len(stripped_rest)
            close = _find_matching_close(rest, paren_pos)
            if close == -1:
                break  # unbalanced — let the caller's own validation raise
            rest = rest[close + 1 :]
        text = rest.lstrip()
    return text


def _split_top_level(raw: str) -> list[str]:
    """Split *raw* on top-level commas, honouring ``<>``, ``()``, and ``[]`` nesting.

    Shared by the function-parameter splitter and the struct-property splitter
    so both tolerate the same constructs: generics (``Array<String>``),
    annotation argument lists (``@Foo("a", "b")``), and array-literal default
    values (``= [1, 2]``-style constructs some annotations use).
    """
    chunks: list[str] = []
    depth = 0
    current = ""
    for ch in raw:
        if ch in "<([":
            depth += 1
            current += ch
        elif ch in ">)]":
            depth -= 1
            current += ch
        elif ch == "," and depth == 0:
            if current.strip():
                chunks.append(current)
            current = ""
        else:
            current += ch
    if current.strip():
        chunks.append(current)
    return chunks


def _split_params(raw: str) -> list[Param]:
    """Split a parameter list, tolerating generics, annotations, and trailing commas."""
    return [_parse_one_param(chunk) for chunk in _split_top_level(raw)]


def _parse_one_param(chunk: str) -> Param:
    chunk = _strip_leading_annotations(chunk.strip())
    if chunk.startswith("vararg "):
        raise UnknownTypeError(
            f"'vararg' parameters are not supported by the generator: '{chunk}'. "
            "Collect the items on the Kotlin side and pass an Array or List instead."
        )
    name, _, ktype = chunk.partition(":")
    name = name.strip()
    ktype = ktype.strip()
    # Drop a default value if present: "timeout: Int = 30"
    ktype = ktype.split("=")[0].strip()
    if not name or not ktype:
        raise ValueError(f"could not parse parameter '{chunk.strip()}'")
    if ktype.startswith("("):
        raise UnknownTypeError(
            f"function-type parameters are not supported: '{name}: {ktype}'. "
            "Use a plain interface or pass a callback handle (Long) instead."
        )
    return Param(name=name, kotlin_type=ktype)


def _strip_comments(source: str) -> str:
    """Blank out block and line comments so they can't be mistaken for code.

    Without this, prose like "the class itself" would be picked up by the
    class-declaration regex. Comments are replaced with whitespace that
    preserves the original newline count, so reported line numbers stay
    accurate.
    """

    def blank(match: re.Match[str]) -> str:
        return "\n" * match.group(0).count("\n")

    source = _BLOCK_COMMENT_RE.sub(blank, source)
    source = _LINE_COMMENT_RE.sub("", source)
    return source


def package_name_from_source(source: str) -> str:
    """Return the Kotlin package declaration without validating the whole file."""
    source = _strip_comments(source)
    pkg_match = _PACKAGE_RE.search(source)
    return pkg_match.group(1) if pkg_match else ""


def parse_kotlin_source(source: str, filename: str = "") -> ParsedFile:
    """Parse a single Kotlin source string into a ParsedFile."""
    source = _strip_comments(source)

    # MVP 1: reject suspend funs and extension funs up front with clear messages.
    if _SUSPEND_RE.search(source):
        raise UnknownTypeError(
            "'suspend external fun' is not supported. "
            "Expose a plain 'external fun' wrapper and call it from a coroutine dispatcher."
        )
    if _EXTENSION_FUN_RE.search(source):
        raise UnknownTypeError(
            "Extension 'external fun' (e.g. 'fun String.foo()') is not supported. "
            "Move the function into a class or object instead."
        )

    pkg_match = _PACKAGE_RE.search(source)
    package = pkg_match.group(1) if pkg_match else ""

    # MVP 2: nested class — collect all class/object declarations in order;
    # the JNI name for Outer.Inner is "Outer_00024Inner" ($ = _00024).
    decl_matches = list(_DECL_RE.finditer(source))
    if decl_matches:
        # The outermost declaration is the first match; subsequent ones inside
        # its body are nested. Walk all of them to build the full qualified name.
        kinds_names = [(m.group(1), m.group(2)) for m in decl_matches]
        # Only include class/object names, stop at the first companion object
        # (it doesn't appear in the JNI class name).
        parts = []
        is_static = False
        for kind, name in kinds_names:
            if kind == "object" and name == "Companion":
                is_static = True
                break
            if kind == "object":
                is_static = True
            parts.append(name)
        class_name = "$".join(parts) if len(parts) > 1 else (parts[0] if parts else "Native")
    else:
        # MVP 4: top-level external fun — use "<Filename>Kt" as class name,
        # matching what the Kotlin compiler emits for top-level declarations.
        fn_match = _FILENAME_KT_RE.search(filename)
        class_name = (fn_match.group(1) + "Kt") if fn_match else "Native"
        is_static = True  # top-level funs are static in the generated class

    # A companion object also makes the externals static even inside a class.
    if not is_static and re.search(r"companion\s+object", source):
        is_static = True

    functions: list[ExternalFunction] = []
    for m in _EXTERNAL_FUN_START_RE.finditer(source):
        # The regex only locates "external fun name(" — the "(" it ends on is the
        # start of the parameter list. Find its true matching ")" by paren-balancing
        # (honouring string literals) rather than a non-greedy regex, so annotation
        # argument lists inside the parameter list don't truncate the match early.
        open_paren = m.end() - 1
        close_paren = _find_matching_close(source, open_paren)
        if close_paren == -1:
            raise ValueError(
                f"unbalanced parentheses in parameter list for '{m.group(1)}' "
                "(unterminated '(' after 'external fun')"
            )
        params_raw = source[open_paren + 1 : close_paren]
        after = source[close_paren + 1 :]
        ret_match = _RETURN_TYPE_RE.match(after)
        ret = ret_match.group(1).strip() if ret_match else None

        # MVP 3: honour @JvmName if it appears in the 300 chars before "external fun".
        # Use the *last* @JvmName match in the window — earlier ones belong to prior
        # functions.  Discard even the last match if another "external fun" sits between
        # the annotation and the current position (meaning the annotation is for a prior
        # function that has no @JvmName of its own).
        lookahead = source[max(0, m.start() - 300) : m.start()]
        jvm_name_match = None
        for candidate in _JVM_NAME_RE.finditer(lookahead):
            if not re.search(r"\bexternal\s+fun\b", lookahead[candidate.end() :]):
                jvm_name_match = candidate
        name = jvm_name_match.group(1) if jvm_name_match else m.group(1)
        line = source.count("\n", 0, m.start()) + 1
        params = _split_params(params_raw)
        functions.append(ExternalFunction(name=name, params=params, return_type=ret, line=line))

    # Collect enum names, struct types, and typealiases visible in this single source
    # string. The driver pre-pass merges globals from the whole source set on top of these.
    enum_names = collect_enum_names(source)
    enum_packages = collect_enum_packages(source, package)
    struct_types = collect_struct_types(source, package)
    typealiases = collect_typealiases(source)

    return ParsedFile(
        package=package,
        class_name=class_name,
        is_static=is_static,
        functions=functions,
        enum_names=enum_names,
        enum_packages=enum_packages,
        struct_types=struct_types,
        typealiases=typealiases,
    )


def _find_top_level_class_ranges(stripped: str) -> list[tuple[int, int]]:
    """Return (start, end) character offsets of each top-level class/object block.

    'Top-level' means the class/object keyword appears at brace depth 0 in the
    stripped source (comments already removed).  Each range spans from the
    keyword to the character after the matching closing brace.
    """
    _kw_re = re.compile(r"\b(class|object)\b")
    ranges: list[tuple[int, int]] = []
    for m in _kw_re.finditer(stripped):
        prefix = stripped[: m.start()]
        depth = prefix.count("{") - prefix.count("}")
        if depth != 0:
            continue  # nested class — belongs to its parent
        after = stripped[m.end() :]
        brace_pos = after.find("{")
        if brace_pos == -1:
            continue  # no body (interface / abstract without body)
        body_start = m.end() + brace_pos + 1
        d = 1
        pos = body_start
        while pos < len(stripped) and d > 0:
            c = stripped[pos]
            if c == "{":
                d += 1
            elif c == "}":
                d -= 1
            pos += 1
        ranges.append((m.start(), pos))
    return ranges


def _remove_ranges_preserving_lines(source: str, ranges: list[tuple[int, int]]) -> str:
    chars = list(source)
    for start, end in ranges:
        for idx in range(start, min(end, len(chars))):
            if chars[idx] != "\n":
                chars[idx] = " "
    return "".join(chars)


def parse_kotlin_source_multi(source: str, filename: str = "") -> list[ParsedFile]:
    """Parse a Kotlin source file that may contain multiple top-level classes.

    Returns one ParsedFile per top-level class/object that contains at least one
    external fun.  If the file has only one class (the common case) this is
    equivalent to [parse_kotlin_source(source, filename)].
    """
    stripped = _strip_comments(source)
    ranges = _find_top_level_class_ranges(stripped)

    if not ranges:
        return [parse_kotlin_source(source, filename)]

    pkg_match = _PACKAGE_RE.search(stripped)
    pkg_prefix = f"package {pkg_match.group(1)}\n\n" if pkg_match else ""

    results: list[ParsedFile] = []
    for start, end in ranges:
        segment = pkg_prefix + stripped[start:end]
        parsed = parse_kotlin_source(segment, filename)
        if parsed.functions:
            line_delta = stripped.count("\n", 0, start) - pkg_prefix.count("\n")
            for fn in parsed.functions:
                fn.line += line_delta
            results.append(parsed)

    top_level_source = _remove_ranges_preserving_lines(stripped, ranges)
    if _EXTERNAL_FUN_START_RE.search(top_level_source):
        parsed = parse_kotlin_source(top_level_source, filename)
        if parsed.functions:
            results.append(parsed)

    return results if results else [parse_kotlin_source(source, filename)]


def parse_kotlin_file(path: Path) -> list[ParsedFile]:
    return parse_kotlin_source_multi(path.read_text(encoding="utf-8"), filename=path.name)


def mangle(segment: str) -> str:
    """Apply JNI short-name mangling to a single identifier segment."""
    out = []
    for ch in segment:
        if ch == "_":
            out.append("_1")
        elif ch == ".":
            out.append("_")
        elif ch == "$":
            out.append("_00024")
        else:
            out.append(ch)
    return "".join(out)


def collect_enum_names(source: str) -> frozenset[str]:
    """Return the names of all ``enum class`` declarations in *source*."""
    stripped = _strip_comments(source)
    return frozenset(_ENUM_CLASS_RE.findall(stripped))


def collect_enum_packages(source: str, package: str = "") -> dict[str, str]:
    """Return a mapping of enum name -> its *declaring* package.

    An enum's marshalled JNI class path must use where it is actually declared, not
    the package of whatever struct/function happens to reference it — those are
    frequently different (e.g. enums grouped under an `enums` sub-package, structs
    under a `models` sub-package).
    """
    stripped = _strip_comments(source)
    return {name: package for name in _ENUM_CLASS_RE.findall(stripped)}


def collect_typealiases(source: str) -> dict[str, str]:
    """Return a mapping of ``typealias Name = Underlying`` declarations in *source*.

    Only simple (non-generic) aliases are recognised — the common case for semantic
    scalar aliases like ``typealias VkDeviceSize = Long``. Chains (an alias of an
    alias) are stored as one hop each; follow them with ``resolve_typealias``.
    """
    stripped = _strip_comments(source)
    return dict(_TYPEALIAS_RE.findall(stripped))


def _extract_ctor_body(source: str, open_paren: int) -> str:
    """Return the content between the ``(`` at *open_paren* and its matching ``)``."""
    close = _find_matching_close(source, open_paren)
    if close == -1:
        return ""
    return source[open_paren + 1 : close]


def _parse_struct_props(raw: str) -> list[KotlinField]:
    """Split a primary-constructor param list and return only ``val``/``var`` properties."""
    props: list[KotlinField] = []
    for chunk in _split_top_level(raw):
        p = _try_parse_prop(chunk.strip())
        if p:
            props.append(p)
    return props


def _try_parse_prop(chunk: str) -> KotlinField | None:
    """Parse ``[@Annotation...] val name: Type [= default]`` → KotlinField.

    Returns None if *chunk* is not a property declaration at all (e.g. a plain
    constructor parameter with no ``val``/``var``). Leading annotations — same-line
    or on their own line, with or without argument lists — are stripped first, so
    an annotated property is never mistaken for "not a property" and silently
    dropped.
    """
    chunk = _strip_leading_annotations(chunk)
    if not (chunk.startswith("val ") or chunk.startswith("var ")):
        return None
    chunk = chunk[4:].strip()
    colon = chunk.find(":")
    if colon == -1:
        return None
    name = chunk[:colon].strip()
    rest = chunk[colon + 1 :].strip()
    # Strip default value, respecting angle brackets.
    depth = 0
    eq_pos = -1
    for i, c in enumerate(rest):
        if c in "<([":
            depth += 1
        elif c in ">)]":
            depth -= 1
        elif c == "=" and depth == 0:
            eq_pos = i
            break
    if eq_pos != -1:
        rest = rest[:eq_pos].strip()
    kt = rest.strip()
    nullable = kt.endswith("?")
    base = kt.rstrip("?").strip()
    if not name or not base:
        return None
    return KotlinField(name=name, kotlin_type=base, nullable=nullable)


def collect_struct_types(source: str, package: str = "") -> dict[str, KotlinStruct]:
    """Return a mapping of class-name → KotlinStruct for all marshallable classes in *source*.

    A class is considered marshallable when its primary constructor consists
    entirely of ``val``/``var`` property declarations (i.e. no plain constructor
    parameters without a ``val``/``var`` keyword).  Empty-constructor classes and
    classes whose primary constructor has no property declarations at all are
    excluded.
    """
    stripped = _strip_comments(source)
    structs: dict[str, KotlinStruct] = {}
    for m in _STRUCT_CLASS_RE.finditer(stripped):
        kind = m.group(1)
        name = m.group(2)
        ctor_body = _extract_ctor_body(stripped, m.end() - 1)
        if not ctor_body.strip():
            continue
        props = _parse_struct_props(ctor_body)
        if not props:
            continue
        structs[name] = KotlinStruct(
            name=name,
            package=package,
            fields=props,
            is_data_class=kind.startswith("data"),
        )
    return structs


def jni_function_name(package: str, class_name: str, method: str) -> str:
    pkg = mangle(package) if package else ""
    parts = ["Java", pkg, mangle(class_name), mangle(method)]
    return "_".join(p for p in parts if p)
