#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Captures REAL shadcn/ui component screenshots from ui.shadcn.com's own docs pages and
saves them to docs/reference/shadcn-previews/ as the ground-truth fidelity reference.

This replaces the previous docs/reference/shadcn-previews/*.png set, which -- despite being
described as "real shadcn reference" images -- was actually hand-captured from
ronjunevaldoz/shadcn-compose's docs site (a third-party Kotlin/Compose reimplementation, see
docs/reference/shadcn-parity.md's "Vega" style-preset caveat), never from ui.shadcn.com itself.
Evidence: the old select_closed_light.png shows the literal text "Vega" -- shadcn-compose's own
style-preset name, not a real shadcn select demo's option text ("Select a fruit" on the real
site). These captures fix that by going straight to the upstream demo pages.

Usage:
    python3 tools/capture_shadcn_reference.py                 # capture everything
    python3 tools/capture_shadcn_reference.py --only button,badge
    python3 tools/capture_shadcn_reference.py --out-dir /tmp/preview-check --headed

Requires playwright (`pip3 install playwright && python3 -m playwright install chromium`) and
Pillow (`pip3 install pillow`) for the multi-tile composites (button/checkbox/switch/input).
Deterministic by construction: fixed 1280x800 viewport, devicePixelRatio 1, forced light theme,
CSS-injected animation/transition/caret disabling.
"""
from __future__ import annotations

import argparse
import sys
import tempfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUT_DIR = REPO_ROOT / "docs" / "reference" / "shadcn-previews"
BASE_URL = "https://ui.shadcn.com"
VIEWPORT = {"width": 1280, "height": 800}

# Injected before every capture so hover/focus/open states land on frame 1, not mid-transition.
DISABLE_MOTION_CSS = """
*, *::before, *::after {
  animation: none !important;
  transition: none !important;
  caret-color: transparent !important;
  scroll-behavior: auto !important;
}
"""

WHITE = (255, 255, 255, 255)


def _pil():
    from PIL import Image  # deferred: only needed for composite captures
    return Image


def compose(tiles: list[Path], out: Path, direction: str = "row", gap: int = 16, pad: int = 24) -> None:
    """Pastes real per-element screenshots onto one padded canvas -- used whenever a single
    real shadcn demo doesn't already show every state/variant we need together (button's 6
    variants live in 6 separate demo cards; checkbox/switch/input states are the same element
    captured before/after a real interaction). Every source tile is a real, unmodified capture;
    this only arranges them, it never draws or fakes pixels."""
    Image = _pil()
    imgs = [Image.open(t).convert("RGBA") for t in tiles]
    if direction == "row":
        w = sum(i.width for i in imgs) + gap * (len(imgs) - 1) + pad * 2
        h = max(i.height for i in imgs) + pad * 2
    else:
        w = max(i.width for i in imgs) + pad * 2
        h = sum(i.height for i in imgs) + gap * (len(imgs) - 1) + pad * 2
    canvas = Image.new("RGBA", (w, h), WHITE)
    x, y = pad, pad
    for img in imgs:
        if direction == "row":
            oy = pad + (h - pad * 2 - img.height) // 2
            canvas.paste(img, (x, oy), img)
            x += img.width + gap
        else:
            ox = pad + (w - pad * 2 - img.width) // 2
            canvas.paste(img, (ox, y), img)
            y += img.height + gap
    out.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(out)


def goto(page, slug: str) -> None:
    page.goto(f"{BASE_URL}/docs/components/{slug}", wait_until="networkidle", timeout=30_000)
    # Force light theme regardless of the site's own dark-mode detection (system pref / stored
    # localStorage choice) -- every existing shadcn-previews/*.png is a "_light" capture.
    page.evaluate(
        "document.documentElement.classList.remove('dark');"
        "document.documentElement.style.colorScheme = 'light';"
    )
    page.add_style_tag(content=DISABLE_MOTION_CSS)
    page.wait_for_timeout(250)  # let the reflow from the style/theme change above settle


def previews(page):
    return page.locator('[data-slot="preview"]')


def shoot(locator, path: Path, pad: int = 8) -> None:
    """Screenshots `locator` padded by `pad` CSS px on every side. shadcn draws focus rings
    and card/badge rings as box-shadow, which paints outside the element's own border-box --
    `locator.screenshot()` clips exactly to that border-box and silently cuts the ring off.
    Found via the input default/focus pair: the focus ring was invisible in the tight crop."""
    # page.screenshot(clip=...) -- unlike locator.screenshot() -- does not auto-scroll the
    # target into view first, so a demo further down the page clips to empty/out-of-bounds.
    locator.scroll_into_view_if_needed()
    # Content just scrolled into view can be geometrically laid out (bounding_box() succeeds,
    # is_visible() is true) one frame before it's actually painted -- reproduced intermittently
    # on the card demo (inputs/buttons present in the DOM with correct boxes, but unpainted in
    # the screenshot). Wait two real animation frames so the paint has actually happened.
    locator.page.evaluate(
        "() => new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)))"
    )
    box = locator.bounding_box()
    if box is None:
        raise RuntimeError(f"element not visible for screenshot: {path.name}")
    locator.page.screenshot(
        path=str(path),
        clip={
            "x": max(box["x"] - pad, 0),
            "y": max(box["y"] - pad, 0),
            "width": box["width"] + pad * 2,
            "height": box["height"] + pad * 2,
        },
    )


# --- one capture function per component -------------------------------------------------

def capture_button(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "button")
    blocks = previews(page)
    # The hero demo only shows Default + an icon button; each other variant is its own,
    # separate small demo card further down the page (real shadcn/ui docs layout, not a single
    # combined row) -- indices found by inspecting the live page's data-slot="preview" order.
    variants = [("default", 2), ("secondary", 4), ("outline", 3), ("ghost", 5), ("destructive", 6), ("link", 7)]
    tiles = []
    for name, idx in variants:
        btn = blocks.nth(idx).locator('button[data-slot="button"]').first
        tile = tmp_dir / f"button_{name}.png"
        shoot(btn, tile)
        tiles.append(tile)
    out = out_dir / "button_variants_light.png"
    compose(tiles, out, direction="row", gap=14, pad=24)
    return out


def capture_badge(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "badge")
    # The hero demo already shows all 4 variants (Default/Secondary/Destructive/Outline)
    # together in one row -- grab their shared parent for a single real capture, no compositing.
    row = previews(page).nth(0).locator('span[data-slot="badge"]').first.locator("xpath=..")
    out = out_dir / "badge_variants_light.png"
    tile = tmp_dir / "badge_row.png"
    shoot(row, tile)
    compose([tile], out, pad=20)
    return out


def capture_checkbox(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "checkbox")
    cb = previews(page).nth(0).locator('[role="checkbox"]').first
    unchecked = tmp_dir / "checkbox_unchecked.png"
    shoot(cb, unchecked)
    cb.click()
    page.wait_for_timeout(100)
    checked = tmp_dir / "checkbox_checked.png"
    shoot(cb, checked)
    out = out_dir / "checkbox_states_light.png"
    compose([unchecked, checked], out, direction="row", gap=16, pad=20)
    return out


def capture_switch(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "switch")
    sw = previews(page).nth(0).locator('[role="switch"]').first
    off = tmp_dir / "switch_off.png"
    shoot(sw, off)
    sw.click()
    page.wait_for_timeout(100)
    on = tmp_dir / "switch_on.png"
    shoot(sw, on)
    out = out_dir / "switch_states_light.png"
    compose([off, on], out, direction="row", gap=16, pad=20)
    return out


def capture_input(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "input")
    # Block 0 is a labelled password field; block 1 is a bare <input>, closer to what "input
    # default+focus" should isolate (no field-label chrome baked into the crop).
    inp = previews(page).nth(1).locator('input[data-slot="input"]').first
    default = tmp_dir / "input_default.png"
    shoot(inp, default)
    inp.click()
    page.wait_for_timeout(100)
    focused = tmp_dir / "input_focus.png"
    shoot(inp, focused)
    out = out_dir / "text-field_states_light.png"
    compose([default, focused], out, direction="column", gap=16, pad=20)
    return out


def capture_select(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "select")
    trigger = previews(page).nth(0).locator('[data-slot="select-trigger"]').first
    out = out_dir / "select_closed_light.png"
    tile = tmp_dir / "select_closed.png"
    shoot(trigger, tile)
    compose([tile], out, pad=20)
    return out


def capture_tabs(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "tabs")
    tabs_list = previews(page).nth(0).locator('[data-slot="tabs-list"]').first
    out = out_dir / "tabs_states_light.png"
    tile = tmp_dir / "tabs_list.png"
    shoot(tabs_list, tile)
    compose([tile], out, pad=20)
    return out


def capture_card(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "card")
    card = previews(page).nth(0).locator('[data-slot="card"]').first
    out = out_dir / "card_states_light.png"
    tile = tmp_dir / "card.png"
    shoot(card, tile)
    compose([tile], out, pad=24)
    return out


def capture_slider(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "slider")
    slider = previews(page).nth(0).locator('[data-slot="slider"]').first
    out = out_dir / "slider_states_light.png"
    tile = tmp_dir / "slider.png"
    shoot(slider, tile)
    compose([tile], out, pad=24)
    return out


def capture_tooltip(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "tooltip")
    trigger = previews(page).nth(0).locator('button[data-slot="button"]').first
    trigger.hover()
    # Base UI's Tooltip doesn't set role="tooltip" in this build -- data-slot is the stable hook.
    tooltip = page.locator('[data-slot="tooltip-content"]').first
    tooltip.wait_for(state="visible", timeout=5_000)
    page.wait_for_timeout(100)
    tb, pb = trigger.bounding_box(), tooltip.bounding_box()
    pad = 16
    x0 = min(tb["x"], pb["x"]) - pad
    y0 = min(tb["y"], pb["y"]) - pad
    x1 = max(tb["x"] + tb["width"], pb["x"] + pb["width"]) + pad
    y1 = max(tb["y"] + tb["height"], pb["y"] + pb["height"]) + pad
    out = out_dir / "tooltip_trigger_light.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    page.screenshot(path=str(out), clip={"x": x0, "y": y0, "width": x1 - x0, "height": y1 - y0})
    return out


def capture_dialog(page, out_dir: Path, tmp_dir: Path) -> Path:
    goto(page, "dialog")
    trigger = previews(page).nth(0).locator('button[data-slot="button"]').first
    trigger.click()
    dialog = page.locator('[role="dialog"]').first
    dialog.wait_for(state="visible", timeout=5_000)
    page.wait_for_timeout(100)
    # Full-viewport shot (backdrop + centered panel) -- matches the existing
    # dialog_states_light.png convention; compare_parity.py's border-trim step aligns this
    # against Awake's tighter dialog preview.
    out = out_dir / "dialog_states_light.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    page.screenshot(path=str(out))
    return out


CAPTURES = {
    "button": capture_button,
    "badge": capture_badge,
    "checkbox": capture_checkbox,
    "switch": capture_switch,
    "input": capture_input,
    "select": capture_select,
    "tabs": capture_tabs,
    "card": capture_card,
    "slider": capture_slider,
    "tooltip": capture_tooltip,
    "dialog": capture_dialog,
}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--only", help="comma-separated subset of: " + ",".join(CAPTURES))
    parser.add_argument("--out-dir", type=Path, default=DEFAULT_OUT_DIR)
    parser.add_argument("--headed", action="store_true")
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

    names = args.only.split(",") if args.only else list(CAPTURES)
    unknown = [n for n in names if n not in CAPTURES]
    if unknown:
        print(f"Unknown component(s): {unknown}. Known: {list(CAPTURES)}", file=sys.stderr)
        return 1

    args.out_dir.mkdir(parents=True, exist_ok=True)
    results: list[tuple[str, str]] = []

    with tempfile.TemporaryDirectory(prefix="shadcn-capture-") as tmp:
        tmp_dir = Path(tmp)
        with sync_playwright() as pw:
            try:
                browser = pw.chromium.launch(headless=not args.headed)
            except Exception as exc:  # noqa: BLE001 - report exactly what's blocked, per task
                print(f"BLOCKED: could not launch chromium: {exc}", file=sys.stderr)
                return 1
            # A fresh context+page per component, not one shared across every goto() -- several
            # navigations on one page intermittently left later pages mid-render (inputs/buttons
            # geometrically present and "visible" per Playwright, but not actually painted; card
            # was reproducibly broken after 7 prior navigations, never broken in isolation).
            # Isolating state per capture is the standard fix for this class of flake, not a
            # longer settle wait -- 11 short-lived contexts is cheap next to debugging Chromium
            # internals further.
            for name in names:
                context = browser.new_context(
                    viewport=VIEWPORT,
                    device_scale_factor=1,
                    color_scheme="light",
                    reduced_motion="reduce",
                )
                context.add_init_script("try { localStorage.setItem('theme', 'light'); } catch (e) {}")
                page = context.new_page()
                try:
                    out = CAPTURES[name](page, args.out_dir, tmp_dir)
                    from PIL import Image

                    with Image.open(out) as img:
                        w, h = img.size
                    results.append((name, f"OK  {out.relative_to(REPO_ROOT) if out.is_relative_to(REPO_ROOT) else out} ({w}x{h})"))
                except Exception as exc:  # noqa: BLE001 - keep capturing the rest, report per-item
                    results.append((name, f"FAIL {exc}"))
                finally:
                    context.close()

            browser.close()

    failed = 0
    for name, status in results:
        print(f"{name:10s} {status}")
        if status.startswith("FAIL"):
            failed += 1
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
