#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Extracts real shadcn/ui base-color OKLCH tokens (new-york-v4 style) from a pinned
third_party/shadcn-ui-ref/ checkout (see tools/fetch_shadcn_reference.sh) and writes a
generated, test-only Kotlin ground-truth object:

    awake/engine/ui/ui-designsystem/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/
    designsystem/ShadcnReferenceTokens.kt

Canonical source: apps/v4/registry/themes.ts's `THEMES` array, one entry per base-color theme
name -- every name in THEME_NAMES below, one for each of Awake's shipped `ShadcnBaseColor`
values (`neutral`, `stone`, `zinc`, `mauve`, `olive`, `mist`, `taupe`). That TS object literal's
`cssVars.light`/`cssVars.dark` are shadcn's own theme-registry source of truth (what actually
generates the real :root/.dark CSS custom properties), not a hand-transcribed copy of rendered
CSS. Stdlib-only regex/brace-matching parse, since the file is TypeScript object-literal syntax,
not valid JSON (unquoted keys, trailing commas).

Deterministic: parsing is a straight top-to-bottom scan over a fixed THEME_NAMES order, so
re-running against an unchanged pinned checkout produces byte-identical output. If a name in
THEME_NAMES is missing from themes.ts (upstream rename/removal), extraction fails loudly instead
of silently emitting a partial file -- a silently dropped base color is exactly the failure mode
this script exists to prevent.
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
# Every base-color theme name Awake ships (ShadcnTheme.kt's ShadcnBaseColor enum), lowercased to
# match themes.ts's own `name:` field verbatim. Order is the file's own THEMES array order and is
# what BY_BASE_COLOR's map is emitted in -- fixed and deterministic, not a set/dict iteration.
THEME_NAMES = ["neutral", "stone", "zinc", "mauve", "olive", "mist", "taupe"]

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
        sys.exit(
            f'error: no theme named "{name}" found in {THEMES_TS} -- upstream registry format '
            "or theme name likely changed; fix THEME_NAMES/extraction rather than silently "
            "skipping this base color"
        )
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


def kotlin_map(colors: "dict[str, tuple[float, float, float, float]]", indent: int) -> str:
    pad = " " * indent
    lines = [f'{pad}"{key}" to {kotlin_oklch(value)},' for key, value in colors.items()]
    return "mapOf(\n" + "\n".join(lines) + f"\n{' ' * (indent - 4)})"


def render_kotlin(sha: str, per_theme: "list[tuple[str, dict, dict, dict[str, float]]]") -> str:
    # All 7 base-color themes in the pinned registry share the same --radius (0.625rem) --
    # verified below rather than assumed, since a future upstream change could diverge them.
    radius_values = {sizes["radius"] for _, _, _, sizes in per_theme if "radius" in sizes}
    if not radius_values:
        sys.exit('error: no "radius" size var found in any extracted theme block')
    if len(radius_values) > 1:
        sys.exit(f"error: base-color themes disagree on --radius: {sorted(radius_values)}")
    radius_rem = next(iter(radius_values))

    by_base_color_entries = []
    for name, light, dark, _sizes in per_theme:
        by_base_color_entries.append(
            f'        "{name}" to ShadcnReferenceBaseColorTokens(\n'
            f"            light = {kotlin_map(light, 16)},\n"
            f"            dark = {kotlin_map(dark, 16)},\n"
            f"        ),"
        )
    by_base_color_kotlin = "mapOf(\n" + "\n".join(by_base_color_entries) + "\n    )"

    return f"""\
// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
//
// GENERATED FILE -- do not hand-edit.
// Generated by tools/extract_shadcn_tokens.py from shadcn-ui/ui @ {sha}
// (apps/v4/registry/themes.ts, new-york-v4 style, one entry per Awake ShadcnBaseColor value:
// {", ".join(name for name, _, _, _ in per_theme)}). Re-run tools/fetch_shadcn_reference.sh to
// move the pinned checkout, then this script to refresh. See
// docs/reference/shadcn-reference-pipeline.md.
package io.github.ronjunevaldoz.awake.ui.designsystem

/** One real shadcn OKLCH CSS custom property value: `oklch(lightness chroma hueDegrees [/ alpha%])`. */
data class ShadcnReferenceOklch(
    val lightness: Float,
    val chroma: Float,
    val hueDegrees: Float,
    val alpha: Float = 1f,
)

/** Real shadcn/ui ground truth for one base-color theme name's light/dark CSS custom properties.
 * Map keys are the CSS custom property name verbatim (`"card-foreground"`, not `cardForeground`)
 * so they stay directly greppable against shadcn's own `--card-foreground` var. */
data class ShadcnReferenceBaseColorTokens(
    val light: Map<String, ShadcnReferenceOklch>,
    val dark: Map<String, ShadcnReferenceOklch>,
)

/**
 * Real shadcn/ui base-color ground truth -- see file header for provenance. [BY_BASE_COLOR] is
 * keyed by the theme name verbatim (`"neutral"`, `"stone"`, ...), matching every value of
 * Awake's `ShadcnBaseColor` enum (lowercased). [light]/[dark] stay as a top-level convenience
 * for `"neutral"` -- Awake's `ShadcnBaseColor.Neutral` / `ShadcnTheme`'s own default.
 */
object ShadcnReferenceTokens {{
    const val PINNED_SHA: String = "{sha}"
    const val RADIUS_REM: Float = {radius_rem}f

    val BY_BASE_COLOR: Map<String, ShadcnReferenceBaseColorTokens> = {by_base_color_kotlin}

    val light: Map<String, ShadcnReferenceOklch> = BY_BASE_COLOR.getValue("neutral").light
    val dark: Map<String, ShadcnReferenceOklch> = BY_BASE_COLOR.getValue("neutral").dark
}}
"""


def main() -> None:
    sha = pinned_sha()
    if not THEMES_TS.is_file():
        sys.exit(f"error: {THEMES_TS} missing -- run tools/fetch_shadcn_reference.sh first")
    source = THEMES_TS.read_text(encoding="utf-8")

    per_theme: "list[tuple[str, dict, dict, dict[str, float]]]" = []
    total_light = total_dark = total_sizes = 0
    for theme_name in THEME_NAMES:
        theme_block = find_theme_block(source, theme_name)
        light_block = find_sub_block(theme_block, "light")
        dark_block = find_sub_block(theme_block, "dark")

        light = parse_mode_colors(light_block)
        dark = parse_mode_colors(dark_block)
        sizes_rem = parse_sizes_rem(theme_block)

        if not light or not dark:
            sys.exit(
                f'error: parsed zero color tokens for theme "{theme_name}" -- registry format '
                "likely changed upstream"
            )

        per_theme.append((theme_name, light, dark, sizes_rem))
        total_light += len(light)
        total_dark += len(dark)
        total_sizes += len(sizes_rem)

    OUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    OUT_FILE.write_text(render_kotlin(sha, per_theme), encoding="utf-8")
    total = total_light + total_dark + total_sizes
    print(f"wrote {OUT_FILE.relative_to(REPO_ROOT)}: {len(per_theme)} base colors, "
          f"{total_light} light + {total_dark} dark color tokens + {total_sizes} size token(s) "
          f"({total} total) @ {sha}")


if __name__ == "__main__":
    main()
