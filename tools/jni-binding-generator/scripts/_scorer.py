"""
Generation quality scorer.

Evaluates the generated C++ and Kotlin stubs against a rubric and prints a
scorecard. Designed to run via `--score` so developers and CI can track
quality over time without blocking builds.

Score dimensions (each 0–100, weighted average):

  type_coverage   — fraction of params/returns using non-TODO mapped types
  null_safety     — fraction of handle params that have a null-check guard
  string_safety   — fraction of String params that have an empty-string guard
  output_size     — ratio of generated lines to hand-written bodies (lower = leaner)
  strict_clean    — 1.0 if zero TODO-typed fields, 0.0 if any exist
"""

from __future__ import annotations

import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

_TODO_RE = re.compile(r"/\*\s*TODO:")
_HANDLE_NULL_CHECK_RE = re.compile(r"if\s*\(![\w_]+_ptr\)")
_STRING_GUARD_RE = re.compile(r'throw_illegal_argument\(env,\s*"[^"]+:\s*\w+ is required"')
_JNI_FN_RE = re.compile(r"extern\s+\"C\"\s+JNIEXPORT")
_VOID_PTR_PARAM_RE = re.compile(r"void\*\s+\w+_ptr\s*=\s*reinterpret_cast")
_JSTRING_PARAM_RE = re.compile(r"std::string\s+\w+_val\s*=\s*jstring2string")


@dataclass
class _FnScore:
    name: str
    has_handle: bool = False
    handle_guarded: bool = False
    string_params: int = 0
    string_guarded: int = 0
    todo_params: int = 0
    total_params: int = 0


@dataclass
class ScoreCard:
    type_coverage: float = 0.0
    null_safety: float = 0.0
    string_safety: float = 0.0
    strict_clean: float = 0.0
    total_functions: int = 0
    total_params: int = 0
    todo_params: int = 0
    files_scored: list[str] = field(default_factory=list)

    @property
    def overall(self) -> float:
        weights = {
            "type_coverage": 0.35,
            "null_safety": 0.30,
            "string_safety": 0.20,
            "strict_clean": 0.15,
        }
        return sum(getattr(self, k) * w for k, w in weights.items())


def _score_cpp_file(path: Path) -> list[_FnScore]:
    """Score each JNI function in a generated C++ file."""
    src = path.read_text(encoding="utf-8")
    # Split on function boundaries
    fn_blocks = re.split(r"(?=extern\s+\"C\"\s+JNIEXPORT)", src)
    scores: list[_FnScore] = []

    for block in fn_blocks:
        if "extern" not in block:
            continue
        # Function name from JNI symbol
        m = re.search(r"JNICALL\s+\n(\w+)\(", block)
        fn_name = m.group(1) if m else "unknown"
        fs = _FnScore(name=fn_name)

        # Count void* (handle) params
        handle_lines = _VOID_PTR_PARAM_RE.findall(block)
        fs.has_handle = bool(handle_lines)
        fs.handle_guarded = fs.has_handle and bool(_HANDLE_NULL_CHECK_RE.search(block))

        # Count string params
        string_lines = _JSTRING_PARAM_RE.findall(block)
        fs.string_params = len(string_lines)
        fs.string_guarded = len(_STRING_GUARD_RE.findall(block))

        # Count TODO placeholders in marshalling section
        marshalling_match = re.search(r"// --- Marshalling ---(.*?)// ---", block, re.DOTALL)
        if marshalling_match:
            marshalling = marshalling_match.group(1)
            fs.todo_params = len(_TODO_RE.findall(marshalling))

        # Total params = handle + string + others (count _val assignments)
        fs.total_params = len(re.findall(r"\w+_val\s*=", block))

        scores.append(fs)
    return scores


def _score_kotlin_file(path: Path) -> int:
    """Return count of TODO-typed fields in a generated Kotlin file."""
    src = path.read_text(encoding="utf-8")
    return len(_TODO_RE.findall(src))


def score(generated_dirs: list[Path], kotlin_dirs: list[Path] | None = None) -> ScoreCard:
    """Compute aggregate quality score across all generated files."""
    card = ScoreCard()
    fn_scores: list[_FnScore] = []

    for d in generated_dirs:
        cpp_files = (
            list(d.rglob("*.gen.cpp")) if d.is_dir() else ([d] if d.suffix == ".cpp" else [])
        )
        for f in cpp_files:
            card.files_scored.append(str(f))
            fn_scores.extend(_score_cpp_file(f))

    if kotlin_dirs:
        for d in kotlin_dirs:
            kt_files = list(d.rglob("*.kt")) if d.is_dir() else ([d] if d.suffix == ".kt" else [])
            for f in kt_files:
                todos = _score_kotlin_file(f)
                card.todo_params += todos
                card.files_scored.append(str(f))

    if not fn_scores:
        return card

    card.total_functions = len(fn_scores)
    card.total_params = sum(f.total_params for f in fn_scores)

    # null_safety: fraction of handle-bearing functions with a guard
    handle_fns = [f for f in fn_scores if f.has_handle]
    card.null_safety = (
        sum(1 for f in handle_fns if f.handle_guarded) / len(handle_fns) if handle_fns else 1.0
    )

    # string_safety: fraction of string params with a guard
    total_str = sum(f.string_params for f in fn_scores)
    total_str_guarded = sum(min(f.string_guarded, f.string_params) for f in fn_scores)
    card.string_safety = total_str_guarded / total_str if total_str else 1.0

    # type_coverage: fraction of params that are NOT TODO-typed
    cpp_todos = sum(f.todo_params for f in fn_scores)
    card.todo_params += cpp_todos
    card.type_coverage = 1.0 - (card.todo_params / card.total_params) if card.total_params else 1.0
    card.type_coverage = max(0.0, min(1.0, card.type_coverage))

    # strict_clean: 1.0 only if zero TODOs anywhere
    card.strict_clean = 1.0 if card.todo_params == 0 else 0.0

    return card


def _stdout_supports_unicode(out) -> bool:
    enc = (getattr(out, "encoding", None) or "ascii").lower().replace("-", "").replace("_", "")
    return enc.startswith(("utf8", "utf16", "utf32"))


def print_scorecard(card: ScoreCard, out=None) -> None:
    out = out or sys.stdout
    bar_width = 30
    uni = _stdout_supports_unicode(out)

    def bar(score: float) -> str:
        filled = round(score * bar_width)
        if uni:
            color = "\033[32m" if score >= 0.9 else ("\033[33m" if score >= 0.7 else "\033[31m")
            reset = "\033[0m"
            return f"{color}{'#' * filled}{'.' * (bar_width - filled)}{reset} {score * 100:5.1f}%"
        return f"{'#' * filled}{'.' * (bar_width - filled)} {score * 100:5.1f}%"

    top = "  +-----------------------------------------------------------+"
    hdr = "  |          JNI Binding Generator -- Quality Score            |"
    sep = "  +-----------------+-------------------------------------------+"
    bot = "  +-----------------------------------------------------------+"

    lines = [
        "",
        top,
        hdr,
        sep,
        f"  | type_coverage   | {bar(card.type_coverage)} |",
        f"  | null_safety     | {bar(card.null_safety)} |",
        f"  | string_safety   | {bar(card.string_safety)} |",
        f"  | strict_clean    | {bar(card.strict_clean)} |",
        sep,
        f"  | Overall score:  {card.overall * 100:5.1f} / 100" + " " * 27 + "|",
        sep,
        f"  | Functions:  {card.total_functions:<6}  Params: {card.total_params:<6}  TODOs: {card.todo_params:<6}  |",
        bot,
        "",
    ]
    for line in lines:
        print(line, file=out)

    if card.todo_params > 0:
        warn = "(!)" if not uni else "(!)"
        print(
            f"  {warn}  {card.todo_params} unmapped type(s) found -- run with --strict-types to fail on these.",
            file=out,
        )
        print("", file=out)
