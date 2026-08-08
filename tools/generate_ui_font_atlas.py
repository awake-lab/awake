#!/usr/bin/env python3
"""
Generate a Kotlin packed UI font atlas from a TTF source.

This intentionally bakes a real font into common Kotlin source so every Awake UI backend
can consume the same atlas + metrics without parsing TTFs at runtime.
"""

from __future__ import annotations

import argparse
import base64
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ASCII_GLYPHS = "".join(chr(code) for code in range(32, 127))


def chunked(sequence: list[str], size: int) -> list[list[str]]:
    return [sequence[index:index + size] for index in range(0, len(sequence), size)]


def format_int_array(name: str, values: list[int]) -> str:
    joined = ", ".join(str(value) for value in values)
    return f"    override val {name}: IntArray = intArrayOf({joined})\n"


def format_float_array(name: str, values: list[float]) -> str:
    joined = ", ".join(f"{value:.6f}f" for value in values)
    return f"    override val {name}: FloatArray = floatArrayOf({joined})\n"


def escape_kotlin_string(value: str) -> str:
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--font", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--object-name", default="RobotoRegularUiFontData")
    parser.add_argument("--display-name", default="Roboto Regular")
    parser.add_argument("--logical-cell", type=int, default=16)
    parser.add_argument("--oversample", type=int, default=4)
    parser.add_argument("--padding", type=int, default=6)
    parser.add_argument("--columns", type=int, default=16)
    args = parser.parse_args()

    font_path = Path(args.font)
    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    render_size = args.logical_cell * args.oversample
    font = ImageFont.truetype(str(font_path), render_size)
    ascent, descent = font.getmetrics()
    line_height_px = ascent + descent
    cell_height_px = line_height_px + args.padding * 2
    cell_width_px = max(
        math.ceil(font.getlength(char)) for char in ASCII_GLYPHS if char != " "
    ) + args.padding * 2
    # Em values are multiplied at runtime by glyphPx, which is a FONT SIZE (cellSize *
    # textScale). Normalising them by the taller line-height cell instead made every advance and
    # quad exactly logical_cell/line_cell too small -- measured against the TTF's own advances,
    # every glyph came out at 0.8421x, precisely 16/19, so all text rendered ~19% narrow.
    em_reference = args.logical_cell
    base_cell_size = math.ceil(line_height_px / args.oversample)

    rows = math.ceil(len(ASCII_GLYPHS) / args.columns)
    atlas_width = cell_width_px * args.columns
    atlas_height = cell_height_px * rows
    atlas = Image.new("L", (atlas_width, atlas_height), 0)
    draw = ImageDraw.Draw(atlas)

    glyph_order = []
    uv_bounds_px: list[int] = []
    quad_metrics_em: list[float] = []
    advances_em: list[float] = []

    for index, char in enumerate(ASCII_GLYPHS):
        col = index % args.columns
        row = index // args.columns
        cell_x = col * cell_width_px
        cell_y = row * cell_height_px
        baseline_x = cell_x + args.padding
        baseline_y = cell_y + args.padding + ascent

        draw.text((baseline_x, baseline_y), char, fill=255, font=font, anchor="ls")

        glyph_mask = atlas.crop((cell_x, cell_y, cell_x + cell_width_px, cell_y + cell_height_px))
        bbox = glyph_mask.getbbox()
        if bbox is None:
            left = baseline_x
            top = cell_y + args.padding
            right = left + max(1, render_size // 4)
            bottom = top + max(1, render_size // 4)
            offset_x_em = 0.0
            offset_y_em = 0.0
            width_em = 0.0
            height_em = 0.0
        else:
            # 1px of raster headroom around each glyph's tight ink bbox. Widening this
            # grows the glyph quad without growing its pen advance, so adjacent glyphs start
            # overlapping at small sizes (confirmed visually -- do not increase this without
            # also adjusting advance_em to match).
            crop_bleed = 1
            left = cell_x + max(0, bbox[0] - crop_bleed)
            top = cell_y + max(0, bbox[1] - crop_bleed)
            right = cell_x + min(cell_width_px, bbox[2] + crop_bleed)
            bottom = cell_y + min(cell_height_px, bbox[3] + crop_bleed)
            # offset_x_em is pen-relative (measured from baseline_x, the same origin as
            # advance_em below), not cell-relative -- otherwise it silently carries the
            # `padding` gap between the cell origin and the pen origin, which inflates
            # `offset_x_em + width_em` by a constant amount that PackedUiFont.advanceFor
            # compares directly against advance_em. Since that padding term is constant but
            # each glyph's ink width differs, the inflation is uneven per glyph -- this is
            # the root cause of the reported uneven letter spacing (see PackedUiFont.kt).
            # offset_y_em stays cell-relative; vertical baseline placement is a separate,
            # currently self-consistent convention untouched by this fix.
            offset_x_em = (left - baseline_x) / args.oversample / em_reference
            offset_y_em = (top - cell_y) / args.oversample / em_reference
            width_em = max(1, right - left) / args.oversample / em_reference
            height_em = max(1, bottom - top) / args.oversample / em_reference

        advance_em = font.getlength(char) / args.oversample / em_reference

        glyph_order.append(char)
        uv_bounds_px.extend([left, top, right, bottom])
        quad_metrics_em.extend([offset_x_em, offset_y_em, width_em, height_em])
        advances_em.append(advance_em)

    encoded_alpha = base64.b64encode(atlas.tobytes()).decode("ascii")
    encoded_lines = "\n".join(f"        {line}" for line in chunked([encoded_alpha[i:i + 120] for i in range(0, len(encoded_alpha), 120)], 1)[0:999999])  # type: ignore
    # The comprehension above is only to reuse the 120-char wrapping; flatten it back.
    encoded_lines = "\n".join(
        f"        {encoded_alpha[i:i + 120]}" for i in range(0, len(encoded_alpha), 120)
    )

    kotlin = [
        "// Copyright (c) Ron June Valdoz",
        "// SPDX-License-Identifier: Apache-2.0",
        "package io.github.ronjunevaldoz.awake.ui.font",
        "",
        f"internal object {args.object_name} : PackedUiFontData {{",
        f"    override val name: String = \"{escape_kotlin_string(args.display_name)}\"",
        f"    override val baseCellSize: Int = {em_reference}",
        f"    override val lineHeightEm: Float = {line_height_px / args.oversample / em_reference:.6f}f",
        "    override val textScaleStep: Float = 0.25f",
        f"    override val atlasWidth: Int = {atlas_width}",
        f"    override val atlasHeight: Int = {atlas_height}",
        "    override val samplingMode: UiFontSamplingMode = UiFontSamplingMode.CoverageAlpha",
        "    override val fallbackChar: Char = '?'",
        f"    override val glyphOrder: String = \"{escape_kotlin_string(''.join(glyph_order))}\"",
        format_int_array("uvBoundsPx", uv_bounds_px).rstrip(),
        format_float_array("quadMetricsEm", quad_metrics_em).rstrip(),
        format_float_array("advancesEm", advances_em).rstrip(),
        '    override val encodedAlphaBase64: String = """',
        encoded_lines,
        '    """',
        "}",
        "",
    ]
    out_path.write_text("\n".join(kotlin))


if __name__ == "__main__":
    main()
