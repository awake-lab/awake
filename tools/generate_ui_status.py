#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Generates docs/reference/ui-fidelity-status.md -- the UI fidelity status matrix.

One table answering "how close is Awake's UI to its target, per area, and what
is still missing". Every status is derived from a probe against the working
tree (a grep for the code marker, a file's existence, a count), never from a
hand-written claim, so a row cannot report DONE for something that was never
wired up. Where a fact is genuinely not machine-checkable the row says so.

Usage:
    python3 tools/generate_ui_status.py
"""

from __future__ import annotations

import json
import re
import subprocess
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
OUT = REPO / "docs/reference/ui-fidelity-status.md"
CORE = REPO / "awake/engine/ui/ui-core/src"
HEADLESS = REPO / "awake/engine/ui/ui-headless/src"
DS = REPO / "awake/engine/ui/ui-designsystem/src"
PREVIEWS = REPO / "docs/reference/shadcn-previews"
METRICS = REPO / "samples/ui-showcase/build/reports/shadcn-parity-metrics.json"

DONE, PARTIAL, MISSING = "done", "partial", "missing"
BADGE = {DONE: "done", PARTIAL: "partial", MISSING: "**missing**"}


def grep_count(root: Path, pattern: str, glob: str = "*.kt") -> int:
    """Occurrences of a regex across a subtree. Probe, not a linter."""
    if not root.exists():
        return 0
    rx = re.compile(pattern)
    total = 0
    for path in root.rglob(glob):
        # Strip line comments first: a comment explaining that a marker was REMOVED
        # otherwise counts as the marker still being present, which inverts the status.
        body = "\n".join(re.sub(r"//.*$", "", line) for line in path.read_text(errors="ignore").splitlines())
        total += len(rx.findall(body))
    return total


def exists(rel: str) -> bool:
    return (REPO / rel).exists()


def metrics() -> list[dict]:
    if not METRICS.exists():
        return []
    return json.loads(METRICS.read_text())


def crop_quality(entry: dict) -> str:
    awake = entry.get("awakeSize") or [0, 0]
    compared = entry.get("comparedSize") or [0, 0]
    if not all(awake) or not all(compared):
        return "unknown"
    ratio = (compared[0] * compared[1]) / (awake[0] * awake[1])
    return "good" if ratio >= 0.75 else ("partial" if ratio >= 0.35 else "poor")


def git_head() -> str:
    try:
        return subprocess.run(
            ["git", "rev-parse", "--short", "HEAD"], cwd=REPO,
            capture_output=True, text=True, check=True,
        ).stdout.strip()
    except Exception:
        return "unknown"


def build_rows() -> list[tuple[str, str, str, str, str]]:
    """(area, status, evidence, verified-by, gap)"""
    rows: list[tuple[str, str, str, str, str]] = []

    # --- Rendering -------------------------------------------------------
    scanline = grep_count(CORE, r"scanlineFillTriangulation")
    rows.append((
        "Path tessellation", DONE if scanline else MISSING,
        f"{scanline} reference(s) to the scanline filler",
        "`UiPathFillTessellationTest`",
        "Verified on the CPU rasterizer only; no real-backend pixel check.",
    ))

    fringe = grep_count(CORE, r"offsetPolygon")
    rows.append((
        "Antialiasing", DONE if fringe else MISSING,
        "fringe centred via polygon offset" if fringe else "outward-only fringe",
        "`UiPathFillTessellationTest`",
        "MSAA is off on every UI pipeline; AA is geometric only.",
    ))

    adaptive = grep_count(CORE, r"CURVE_FLATNESS_TOLERANCE_PX")
    rows.append((
        "Curve flattening", DONE if adaptive else MISSING,
        "tolerance-driven" if adaptive else "fixed step count",
        "`UiPathFillTessellationTest`", "",
    ))

    stroke_join_reads = grep_count(CORE, r"stroke\.join|UiStrokeJoin\.")
    rows.append((
        "Stroke rendering", MISSING if stroke_join_reads <= 1 else PARTIAL,
        "`UiStrokeJoin` declared but never read" if stroke_join_reads <= 1 else "joins read",
        "none",
        "Blocks any outline icon tier; corners would leave open notches.",
    ))

    # --- Assets ----------------------------------------------------------
    hand_written = grep_count(HEADLESS, r"transcribed|ported by hand")
    cubics = grep_count(HEADLESS, r"cubicTo\(")
    rows.append((
        "Icons", DONE if cubics and not hand_written else PARTIAL,
        f"{cubics} cubic segments, generated from SVG",
        "`tools/svg_to_ui_image_vector.py --self-test`",
        "Fill-only: no outline tier until stroke joins exist.",
    ))

    advance_test = exists(
        "awake/engine/ui/ui-core/src/commonTest/kotlin/io/github/ronjunevaldoz/"
        "awake/ui/font/PackedUiFontAdvanceTest.kt"
    )
    rows.append((
        "Text metrics", DONE if advance_test else MISSING,
        "advances pen-relative" if advance_test else "unverified",
        "`PackedUiFontAdvanceTest`",
        "Baked raster atlas, size-quantised; no subpixel positioning.",
    ))

    # --- Design tokens ---------------------------------------------------
    expanded = DS / "commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/ShadcnReferenceTokenExpandedTest.kt"
    drifted: list[str] = []
    if expanded.exists():
        block = re.search(r"KNOWN_DRIFTED[^(]*\(([^)]*)\)", expanded.read_text(), re.S)
        if block:
            drifted = re.findall(r'"([^"]+)"\s*to', block.group(1))
    rows.append((
        "Colour tokens", DONE if expanded.exists() and not drifted else PARTIAL,
        f"{len(drifted)} drifted" if drifted else "exact match, both modes",
        "`ShadcnReferenceTokenExpandedTest`",
        "Only the Vega preset on the Neutral base is checked.",
    ))

    ui_shape_pins = grep_count(DS, r"UiShape\.(sm|md|lg|xl)\b")
    rows.append((
        "Radius and spacing", DONE if ui_shape_pins == 0 else PARTIAL,
        "theme-driven" if ui_shape_pins == 0 else f"{ui_shape_pins} pinned to the ui-core global",
        "`Shadcn*FidelityTest`",
        "Seven non-default presets have no parity coverage.",
    ))

    ring = grep_count(CORE, r"\bringWidth\b|fun StyleScope\.ring\(")
    rows.append((
        "Focus ring", DONE if ring else MISSING,
        "ring in Style" if ring else "focused{} only recolours the border",
        "none",
        "" if ring else "shadcn draws a distinct 3px ring at 50% opacity outside the border.",
    ))

    shadow_calls = grep_count(DS, r"\.shadow\(")
    rows.append((
        "Shadows", DONE if shadow_calls else MISSING,
        f"{shadow_calls} call site(s)" if shadow_calls else "`Style.shadow` exists, never called",
        "`ShadcnCardVariantTest`",
        "" if shadow_calls else "Card shadow-sm, button/input shadow-xs, dialog shadow-lg all absent.",
    ))

    hardcoded = grep_count(HEADLESS, r"Dimension\.Fixed\(40f\.dp\)|40f\.dp")
    rows.append((
        "Component size slot", MISSING if hardcoded else DONE,
        f"{hardcoded} hardcoded 40dp fallback(s)" if hardcoded else "theme-sourced",
        "none",
        "shadcn's default control height is 36dp; sizes are not in the style contract."
        if hardcoded else "",
    ))

    # --- Behaviour -------------------------------------------------------
    keys = set(re.findall(r"Key\.(\w+)", (CORE / "commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/input/UiInputState.kt").read_text())) \
        if exists("awake/engine/ui/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/input/UiInputState.kt") else set()
    rows.append((
        "Keyboard and focus", MISSING if len(keys) <= 1 else PARTIAL,
        f"keys exposed: {', '.join(sorted(keys)) or 'none'}",
        "none",
        "No Tab traversal, Escape dismiss, arrow navigation or Enter activation. "
        "One gap blocking roughly ten Radix behaviours.",
    ))

    disabled = grep_count(HEADLESS, r"withGraphicsLayerAlpha")
    rows.append((
        "Disabled states", DONE if disabled >= 5 else PARTIAL,
        f"{disabled} widget(s) dim when disabled",
        "ui-headless widget tests", "",
    ))

    semantics_file = CORE / "commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiSemantics.kt"
    declared = 0
    if semantics_file.exists():
        declared = len(re.findall(r"^\s*(Progress|Toast|Separator|Avatar),", semantics_file.read_text(), re.M))
    wired = grep_count(HEADLESS, r"UiSemanticRole\.(Progress|Toast|Separator|Avatar)")
    rows.append((
        "Semantics", DONE if declared >= 4 and wired >= 4 else PARTIAL,
        f"{declared} role(s) declared, {wired} wired into widgets",
        "ui-headless widget tests",
        "No accessibility surface consumes these yet.",
    ))

    # --- Verification ----------------------------------------------------
    entries = metrics()
    good = [e for e in entries if crop_quality(e) == "good"]
    rows.append((
        "Reference capture", DONE if PREVIEWS.exists() else MISSING,
        f"{len(list(PREVIEWS.glob('*.png')))} capture(s) from ui.shadcn.com",
        "`tools/capture_shadcn_reference.py`",
        "Light mode only.",
    ))
    rows.append((
        "Comparison harness",
        PARTIAL if entries and good else (MISSING if not entries else PARTIAL),
        f"{len(good)}/{len(entries)} pair(s) aligned well enough to read" if entries else "no metrics on disk",
        "`ShadcnReferenceComparisonTest`",
        "Misaligned pairs report framing differences, not fidelity. Matching preview "
        "framing to the captures is the blocking work.",
    ))

    dark = len(list(PREVIEWS.glob("*dark*.png"))) if PREVIEWS.exists() else 0
    rows.append((
        "Dark mode parity", DONE if dark else MISSING,
        f"{dark} dark capture(s)",
        "none",
        "" if dark else "No dark reference captures exist; every parity preview is light.",
    ))

    orphaned = [
        name for name in ("dialog", "tabs", "accordion", "collapsible")
        if grep_count(DS, rf"fun (?:\w+Scope|UiScope)(?:<[^>]+>)?\.shadcn{name.capitalize()}\b")
        and not grep_count(HEADLESS, rf"fun (?:\w+Scope|UiScope)(?:<[^>]+>)?\.{name}\b")
    ]
    rows.append((
        "Layer ownership", PARTIAL if orphaned else DONE,
        f"{len(orphaned)} branded component(s) with no headless primitive",
        "`verifyUiOwnership`",
        ("Behaviour for " + ", ".join(orphaned) + " lives only in ui-designsystem, so it "
         "cannot be reused unbranded.") if orphaned else "",
    ))

    return rows


def main() -> None:
    rows = build_rows()
    counts = {s: sum(1 for r in rows if r[1] == s) for s in (DONE, PARTIAL, MISSING)}

    lines: list[str] = []
    add = lines.append
    add("# UI fidelity status matrix")
    add("")
    add("**Generated by `tools/generate_ui_status.py` — do not hand-edit.**")
    add(f"Reflects the working tree at `{git_head()}`.")
    add("")
    add("Where Awake's UI stands against its targets — shadcn/ui for the design system, Radix")
    add("for headless behaviour, correct rendering for the engine — and what is still missing.")
    add("Each status is derived from a probe against the source (a marker grep, a file check, a")
    add("count), not from a written claim, so a row cannot report done for unwired work.")
    add("")
    add(f"**{counts[DONE]} done · {counts[PARTIAL]} partial · {counts[MISSING]} missing**")
    add("")
    add("| Area | Status | Evidence | Verified by | Gap |")
    add("|---|---|---|---|---|")
    for area, status, evidence, verified, gap in rows:
        add(f"| {area} | {BADGE[status]} | {evidence} | {verified} | {gap or '—'} |")
    add("")
    add("## Reading this")
    add("")
    add("- **Verified by `none`** means no automated test guards the area. A `done` row with no")
    add("  verifier is an assertion about the code as it stands today, not a regression guard.")
    add("- **Gap** is what remains even when the status is `done`; a done row can still carry a")
    add("  real limitation.")
    add("- Status is deliberately coarse. It answers whether the mechanism exists, not whether")
    add("  it is pixel-exact — `docs/reference/shadcn-parity.md` carries the measured numbers,")
    add("  and `docs/reference/ui-validation.md` the policy for what counts as proof.")
    add("")

    OUT.write_text("\n".join(lines) + "\n")
    print(f"wrote {OUT.relative_to(REPO)}")
    print(f"  {counts[DONE]} done, {counts[PARTIAL]} partial, {counts[MISSING]} missing")


if __name__ == "__main__":
    main()
