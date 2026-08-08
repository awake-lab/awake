#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Captures shadcn reference PNGs from the local reference app.

Replaces scraping ui.shadcn.com. That page could only ever show whatever its
demos happened to render: nine of eleven captures had to be tiled by us, demo
content did not match our previews, and focus/disabled/hover were uncapturable.
Worse, it applies its own docs-site theming -- comparing against it produced a
false "dark destructive token bug" that the pinned source disproved.

Here the components come verbatim from the pinned shadcn checkout and we own the
page, so a case is one component group, hugged to its own content, in any theme
or radius.

Builds the app if needed, serves it, screenshots each case, shuts down.

Usage:
    python3 tools/capture_shadcn_local.py
    python3 tools/capture_shadcn_local.py --only button-variants --theme both
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
APP = REPO / "tools/shadcn-reference-app"
MANIFEST = REPO / "tools/shadcn_reference_cases.json"
OUT_DIR = REPO / "docs/reference/shadcn-previews-local"
PORT = 4319
BASE = f"http://localhost:{PORT}"


def ensure_built() -> bool:
    if not (APP / "node_modules").exists():
        print("installing app dependencies (first run)...", file=sys.stderr)
        if subprocess.run(["npm", "install"], cwd=APP).returncode != 0:
            return False
    if subprocess.run(["npm", "run", "build"], cwd=APP).returncode != 0:
        return False
    return True


def wait_for_server(timeout_s: float = 30.0) -> bool:
    deadline = time.time() + timeout_s
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(f"{BASE}/", timeout=2):
                return True
        except (urllib.error.URLError, ConnectionError, TimeoutError):
            time.sleep(0.3)
    return False


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--only", help="comma-separated case ids")
    parser.add_argument("--theme", default="both", choices=["light", "dark", "both"])
    parser.add_argument("--out-dir", type=Path, default=OUT_DIR)
    parser.add_argument("--skip-build", action="store_true")
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

    cases = json.loads(MANIFEST.read_text())["cases"]
    if args.only:
        wanted = set(args.only.split(","))
        unknown = wanted - {c["id"] for c in cases}
        if unknown:
            print(f"Unknown case id(s): {sorted(unknown)}", file=sys.stderr)
            return 1
        cases = [c for c in cases if c["id"] in wanted]

    if not args.skip_build and not ensure_built():
        print("BLOCKED: app build failed", file=sys.stderr)
        return 1

    themes = ["light", "dark"] if args.theme == "both" else [args.theme]
    args.out_dir.mkdir(parents=True, exist_ok=True)

    server = subprocess.Popen(
        ["npm", "run", "preview"], cwd=APP, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
    )
    results: list[tuple[str, str]] = []
    try:
        if not wait_for_server():
            print("BLOCKED: preview server did not come up", file=sys.stderr)
            return 1
        with sync_playwright() as pw:
            browser = pw.chromium.launch(headless=True)
            context = browser.new_context(
                viewport={"width": 1400, "height": 900},
                device_scale_factor=1,
                reduced_motion="reduce",
            )
            page = context.new_page()
            for case in cases:
                for theme in themes:
                    url = f"{BASE}/?case={case['id']}&theme={theme}"
                    page.goto(url, wait_until="networkidle")
                    page.wait_for_timeout(150)
                    target = page.locator("#case")
                    if target.get_attribute("data-error"):
                        results.append((f"{case['id']} [{theme}]", "FAIL app has no such case"))
                        continue
                    out = args.out_dir / f"{case['id']}_{theme}.png"
                    target.screenshot(path=str(out))
                    box = target.bounding_box()
                    results.append(
                        (f"{case['id']} [{theme}]", f"OK  {out.relative_to(REPO)} ({round(box['width'])}x{round(box['height'])})")
                    )
            browser.close()
    finally:
        server.terminate()
        server.wait(timeout=10)

    width = max(len(n) for n, _ in results) if results else 0
    failed = 0
    for name, status in results:
        print(f"{name.ljust(width)}  {status}")
        failed += status.startswith("FAIL")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
