#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Downloads the official Heroicons SVG for every icon in heroicons_manifest.json and
rasterizes it to a fixed-size reference PNG -- the ground truth the icon-fidelity guard
(ui-headless desktopTest `IconFidelityTest`) compares our shipped `UiImageVector`s against.

Usage:
    python3 tools/capture_heroicons_reference.py                   # capture every icon
    python3 tools/capture_heroicons_reference.py --only chevron-down,camera

Requires playwright (already used by capture_shadcn_reference.py):
    pip3 install playwright && python3 -m playwright install chromium

Deterministic: fixed 128x128 canvas, `color:white` forces the SVG's `fill="currentColor"`
to resolve white on a solid black background -- the same tint/background combination the
Kotlin test renders our vector with (`icon(vector, tint = Color.White)` over black). Same
SVG bytes in, same PNG bytes out.
"""
from __future__ import annotations

import argparse
import json
import sys
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
MANIFEST_PATH = Path(__file__).resolve().parent / "heroicons_manifest.json"
OUT_DIR = REPO_ROOT / "docs" / "reference" / "icon-previews"
SIZE = 128
RAW_BASE = "https://raw.githubusercontent.com/tailwindlabs/heroicons/master/optimized"


def load_manifest() -> list[dict]:
    return json.loads(MANIFEST_PATH.read_text())


def fetch_svg(url_tier: str, name: str) -> str:
    url = f"{RAW_BASE}/{url_tier}/{name}.svg"
    with urllib.request.urlopen(url, timeout=15) as resp:  # noqa: S310 - fixed github raw host
        return resp.read().decode("utf-8")


def rasterize(page, svg_text: str, out_path: Path) -> None:
    html = f"""<!DOCTYPE html><html><head><style>
html,body {{ margin:0; padding:0; background:#000; }}
svg {{ display:block; width:{SIZE}px; height:{SIZE}px; color:#fff; }}
</style></head><body>{svg_text}</body></html>"""
    page.set_content(html)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    page.screenshot(path=str(out_path))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--only", help="comma-separated icon names to restrict to")
    args = parser.parse_args()

    manifest = load_manifest()
    only = set(args.only.split(",")) if args.only else None

    try:
        from playwright.sync_api import sync_playwright
    except ImportError:
        print(
            "BLOCKED: playwright is not installed. Run:\n"
            "  pip3 install playwright && python3 -m playwright install chromium",
            file=sys.stderr,
        )
        return 1

    results: list[tuple[str, str, str]] = []
    with sync_playwright() as pw:
        browser = pw.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": SIZE, "height": SIZE}, device_scale_factor=1)
        for entry in manifest:
            if only is not None and entry["name"] not in only:
                continue
            out_path = OUT_DIR / f"{entry['name']}-{entry['kotlinTier']}.png"
            try:
                svg_text = fetch_svg(entry["urlTier"], entry["name"])
                rasterize(page, svg_text, out_path)
                results.append((entry["name"], entry["kotlinTier"], f"OK   {out_path.relative_to(REPO_ROOT)}"))
            except Exception as exc:  # noqa: BLE001 - keep capturing the rest, report per-item
                results.append((entry["name"], entry["kotlinTier"], f"FAIL {exc}"))
        browser.close()

    failed = 0
    for name, tier, status in results:
        print(f"{name:30s} {tier:14s} {status}")
        if status.startswith("FAIL"):
            failed += 1
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
