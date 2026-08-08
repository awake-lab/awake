#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Renders text samples with the real Roboto TTF and saves them as reference PNGs.

Answers a question the atlas metrics alone cannot: when our renderer draws 'a'
a pixel below 'i', is that faithful to the typeface or introduced by our
pipeline? Chromium rendering the *same* TTF at the *same* pixel size is the
control.

Deliberately uses the exact font file baked into RobotoRegularUiFontData
(see the manifest's fontFile) rather than a system Roboto -- a different
version would make every difference unattributable.

Deterministic: fixed canvas, devicePixelRatio 1, no subpixel antialiasing,
white-on-black to match how the Kotlin side rasterizes.

Usage:
    python3 tools/capture_font_reference.py
    python3 tools/capture_font_reference.py --only email-12,roundvsflat-16
"""

from __future__ import annotations

import argparse
import base64
import json
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
MANIFEST = REPO / "tools/font_samples_manifest.json"
OUT_DIR = REPO / "docs/reference/font-previews"

# Greyscale antialiasing only. Subpixel (LCD) AA would colour-fringe every edge,
# which our alpha-coverage atlas has no equivalent for -- the diff would be
# dominated by that rather than by glyph placement.
PAGE = """<!doctype html>
<html><head><meta charset="utf-8"><style>
  @font-face {{ font-family: 'AwakeRoboto'; src: url(data:font/ttf;base64,{font_b64}) format('truetype'); }}
  html, body {{ margin: 0; padding: 0; background: #000; }}
  #stage {{
    position: relative;
    width: {width}px; height: {height}px;
    background: #000;
  }}
  #sample {{
    position: absolute;
    left: {origin_x}px;
    top: 0;
    /* Position by baseline, not by box: line-height 0 plus a top offset equal to
       the baseline keeps the text origin exactly where the Kotlin side puts it. */
    line-height: 0;
    height: {baseline_y}px;
    display: flex; align-items: flex-end;
    font-family: 'AwakeRoboto';
    font-size: {size_px}px;
    color: #fff;
    white-space: pre;
    -webkit-font-smoothing: antialiased;
    font-kerning: none;
    font-variant-ligatures: none;
    letter-spacing: 0;
  }}
</style></head>
<body><div id="stage"><div id="sample">{text}</div></div></body></html>
"""


def escape(text: str) -> str:
    return (
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")
    )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--only", help="comma-separated sample ids")
    parser.add_argument("--out-dir", type=Path, default=OUT_DIR)
    args = parser.parse_args()

    try:
        from playwright.sync_api import sync_playwright
    except ImportError:
        print(
            "BLOCKED: playwright is not installed. Run:\n"
            "  pip3 install playwright && python3 -m playwright install chromium",
            file=sys.stderr,
        )
        return 1

    manifest = json.loads(MANIFEST.read_text())
    font_path = REPO / manifest["fontFile"]
    if not font_path.exists():
        print(f"BLOCKED: font not found at {font_path}", file=sys.stderr)
        return 1
    font_b64 = base64.b64encode(font_path.read_bytes()).decode("ascii")

    canvas = manifest["canvas"]
    samples = manifest["samples"]
    if args.only:
        wanted = set(args.only.split(","))
        samples = [s for s in samples if s["id"] in wanted]
        unknown = wanted - {s["id"] for s in samples}
        if unknown:
            print(f"Unknown sample id(s): {sorted(unknown)}", file=sys.stderr)
            return 1

    args.out_dir.mkdir(parents=True, exist_ok=True)
    results: list[tuple[str, str]] = []

    with sync_playwright() as pw:
        try:
            browser = pw.chromium.launch(headless=True)
        except Exception as exc:  # noqa: BLE001 - report exactly what's blocked
            print(f"BLOCKED: could not launch chromium: {exc}", file=sys.stderr)
            return 1
        context = browser.new_context(
            viewport={"width": canvas["width"], "height": canvas["height"]},
            device_scale_factor=1,
        )
        page = context.new_page()
        for sample in samples:
            html = PAGE.format(
                font_b64=font_b64,
                width=canvas["width"],
                height=canvas["height"],
                origin_x=canvas["originX"],
                baseline_y=canvas["baselineY"],
                size_px=sample["sizePx"],
                text=escape(sample["text"]),
            )
            page.set_content(html)
            page.evaluate("document.fonts.ready")
            page.wait_for_timeout(120)
            out = args.out_dir / f"{sample['id']}.png"
            page.locator("#stage").screenshot(path=str(out))
            results.append((sample["id"], f"OK  {out.relative_to(REPO)}"))
        context.close()
        browser.close()

    width = max(len(name) for name, _ in results) if results else 0
    for name, status in results:
        print(f"{name.ljust(width)}  {status}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
