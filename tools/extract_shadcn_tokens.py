#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Extracts real shadcn/ui "neutral" base-color OKLCH tokens (new-york-v4 style) from a pinned
third_party/shadcn-ui-ref/ checkout (see tools/fetch_shadcn_reference.sh) and writes a
generated, test-only Kotlin ground-truth object:

    awake/engine/ui/ui-designsystem/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/
    designsystem/ShadcnReferenceTokens.kt

Canonical source: apps/v4/registry/themes.ts's `THEMES` array, the entry named "neutral" --
that TS object literal's `cssVars.light`/`cssVars.dark` are shadcn's own theme-registry source
of truth (what actually generates the real :root/.dark CSS custom properties), not a
hand-transcribed copy of rendered CSS. Stdlib-only regex/brace-matching parse, since the file
is TypeScript object-literal syntax, not valid JSON (unquoted keys, trailing commas).

Deterministic: parsing is a straight top-to-bottom scan, so re-running against an unchanged
pinned checkout produces byte-identical output.
"""
from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
REF_CLONE = REPO_ROOT / "third_party" / "shadcn-ui-ref"
THEMES_TS = REF_CLONE / "apps" / "v4" / "registry" / "themes.ts"
OUT_FILE = (
    REPO_ROOT
    / "awake"
    / "engine"
    / "ui"
    / "ui-designsystem"
    / "src"
    / "commonTest"
    / "kotlin"
    / "io"
    / "github"
    / "ronjunevaldoz"
    / "awake"
    / "ui"
    / "designsystem"
    / "ShadcnReferenceTokens.kt"
)
THEME_NAME = "neutral"

OKLCH_RE = re.compile(
    r"^oklch\(\s*([\d.]+)\s+([\d.]+)\s+([\d.]+)(?:\s*/\s*([\d.]+)%)?\s*\)$"
)
REM_RE = re.compile(r"^([\d.]+)rem$")
ENTRY_RE = re.compile(r'(?:"([\w-]+)"|([A-Za-z_][\w]*))\s*:\s*"([^"]*)"')


def pinned_sha() -> str:
    if not (REF_CLONE / ".git").is_dir():
        sys.exit(
            f"error: {REF_CLONE} is not a git checkout -- run "
            "tools/fetch_shadcn_reference.sh first"
        )
    return subprocess.run(
        ["git", "-C", str(REF_CLONE), "rev-parse", "HEAD"],
        check=True,
        capture_output=True,
        text=True,
    ).stdout.strip()


def extract_balanced(text: str, open_brace_index: int) -> str:
    """Returns text[open_brace_index : matching_close+1], skipping braces inside "..." strings."""
    depth = 0
    in_string = False
    i = open_brace_index
    while i < len(text):
        ch = text[i]
        if in_string:
            if ch == "\\":
                i += 1  # skip escaped char
            elif ch == '"':
                in_string = False
        elif ch == '"':
            in_string = True
        elif ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return text[open_brace_index : i + 1]
        i += 1
    raise ValueError("unbalanced braces")


def find_theme_block(source: str, name: str) -> str:
    name_match = re.search(rf'name:\s*"{re.escape(name)}"', source)
    if not name_match:
        raise ValueError(f'no theme named "{name}" found in {THEMES_TS}')
    open_brace = source.rindex("{", 0, name_match.start())
    return extract_balanced(source, open_brace)


def find_sub_block(theme_block: str, key: str) -> str:
    key_match = re.search(rf"{key}:\s*\{{", theme_block)
    if not key_match:
        raise ValueError(f'no "{key}" block found in theme')
    open_brace = theme_block.index("{", key_match.start())
    return extract_balanced(theme_block, open_brace)


def parse_entries(block: str) -> "list[tuple[str, str]]":
    """Ordered (key, rawValue) pairs directly inside `block` -- callers pass an already
    brace-balanced light/dark (or whole-theme) substring, so nested sub-objects aren't a risk
    here (`light`/`dark` are flat key: "value" maps in this registry format)."""
    return [(quoted or bare, value) for quoted, bare, value in ENTRY_RE.findall(block)]


def parse_oklch(raw: str) -> "tuple[float, float, float, float] | None":
    m = OKLCH_RE.match(raw)
    if not m:
        return None
    lightness, chroma, hue, alpha_pct = m.groups()
    alpha = float(alpha_pct) / 100.0 if alpha_pct is not None else 1.0
    return float(lightness), float(chroma), float(hue), alpha


def parse_mode_colors(block: str) -> "dict[str, tuple[float, float, float, float]]":
    colors: "dict[str, tuple[float, float, float, float]]" = {}
    for key, raw in parse_entries(block):
        parsed = parse_oklch(raw)
        if parsed is not None:
            colors[key] = parsed
    return colors


def parse_sizes_rem(theme_block: str) -> "dict[str, float]":
    sizes: "dict[str, float]" = {}
    for key, raw in parse_entries(theme_block):
        m = REM_RE.match(raw)
        if m:
            sizes[key] = float(m.group(1))
    return sizes


def kotlin_oklch(value: "tuple[float, float, float, float]") -> str:
    lightness, chroma, hue, alpha = value
    args = [f"{lightness}f", f"{chroma}f", f"{hue}f"]
    if alpha != 1.0:
        args.append(f"{alpha}f")
    return f"ShadcnReferenceOklch({', '.join(args)})"


def kotlin_map(colors: "dict[str, tuple[float, float, float, float]]") -> str:
    lines = [f'        "{key}" to {kotlin_oklch(value)},' for key, value in colors.items()]
    return "mapOf(\n" + "\n".join(lines) + "\n    )"


def render_kotlin(sha: str, light: dict, dark: dict, sizes_rem: "dict[str, float]") -> str:
    radius_rem = sizes_rem.get("radius")
    if radius_rem is None:
        sys.exit('error: no "radius" size var found in the neutral theme block')
    other_sizes = {k: v for k, v in sizes_rem.items() if k != "radius"}
    other_sizes_kotlin = (
        "emptyMap()"
        if not other_sizes
        else "mapOf(\n"
        + "\n".join(f'        "{k}" to {v}f,' for k, v in other_sizes.items())
        + "\n    )"
    )
    return f"""\
// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
//
// GENERATED FILE -- do not hand-edit.
// Generated by tools/extract_shadcn_tokens.py from shadcn-ui/ui @ {sha}
// (apps/v4/registry/themes.ts, theme "{THEME_NAME}", new-york-v4 style -- Awake's
// ShadcnBaseColor.Neutral counterpart). Re-run tools/fetch_shadcn_reference.sh to move the
// pinned checkout, then this script to refresh. See docs/reference/shadcn-reference-pipeline.md.
package io.github.ronjunevaldoz.awake.ui.designsystem

/** One real shadcn OKLCH CSS custom property value: `oklch(lightness chroma hueDegrees [/ alpha%])`. */
data class ShadcnReferenceOklch(
    val lightness: Float,
    val chroma: Float,
    val hueDegrees: Float,
    val alpha: Float = 1f,
)

/**
 * Real shadcn/ui "{THEME_NAME}" base-color ground truth -- see file header for provenance.
 * Map keys are the CSS custom property name verbatim (`"card-foreground"`, not `cardForeground`)
 * so they stay directly greppable against shadcn's own `--card-foreground` var.
 */
object ShadcnReferenceTokens {{
    const val PINNED_SHA: String = "{sha}"
    const val RADIUS_REM: Float = {radius_rem}f
    val OTHER_SIZES_REM: Map<String, Float> = {other_sizes_kotlin}

    val light: Map<String, ShadcnReferenceOklch> = {kotlin_map(light)}

    val dark: Map<String, ShadcnReferenceOklch> = {kotlin_map(dark)}
}}
"""


def main() -> None:
    sha = pinned_sha()
    if not THEMES_TS.is_file():
        sys.exit(f"error: {THEMES_TS} missing -- run tools/fetch_shadcn_reference.sh first")
    source = THEMES_TS.read_text(encoding="utf-8")

    theme_block = find_theme_block(source, THEME_NAME)
    light_block = find_sub_block(theme_block, "light")
    dark_block = find_sub_block(theme_block, "dark")

    light = parse_mode_colors(light_block)
    dark = parse_mode_colors(dark_block)
    sizes_rem = parse_sizes_rem(theme_block)

    if not light or not dark:
        sys.exit("error: parsed zero color tokens -- registry format likely changed upstream")

    OUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUT_FILE.write_text(render_kotlin(sha, light, dark, sizes_rem), encoding="utf-8")
    total = len(light) + len(dark) + len(sizes_rem)
    print(f"wrote {OUT_FILE.relative_to(REPO_ROOT)}: {len(light)} light + {len(dark)} dark "
          f"color tokens + {len(sizes_rem)} size token(s) ({total} total) @ {sha}")


if __name__ == "__main__":
    main()
