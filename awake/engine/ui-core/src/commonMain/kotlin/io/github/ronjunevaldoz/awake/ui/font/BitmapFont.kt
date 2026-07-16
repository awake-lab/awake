// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.math.floor

/** Normalized UV rect for one glyph cell in [BitmapFont.atlasPixelsRgba]. */
data class GlyphRect(val u0: Float, val v0: Float, val u1: Float, val v1: Float)

/**
 * A minimal, hand-authored 8x8 monospace bitmap font -- deliberately not `msdf-bmfont-xml`/
 * MSDF rendering (see docs/MVP_PLAN.md's custom-UI decision log): this is a debug-catalog
 * label, not user-facing typography, so a real signed-distance-field toolchain + shader
 * would be solving a problem this UI doesn't have yet.
 *
 * Covers the full uppercase A-Z/0-9 range plus the punctuation this repo's UI labels use
 * (` .-:_`). Lowercase input aliases to the matching uppercase cell so mixed-case labels stay
 * legible without doubling the atlas size for what is still intentionally a tiny debug font.
 * Extending coverage further means adding another row to [GLYPH_ROWS] below, each entry a
 * `Char` to 8 row-bitmasks (bit 7 = leftmost pixel). Builds its own atlas pixel buffer at
 * construction time rather than loading an external asset -- no offline generation step, no
 * metadata file to parse.
 *
 * The source drawings stay 8x8 bitmasks, but the uploaded atlas is a higher-resolution
 * coverage texture with a transparent gutter around every glyph. That gives the renderer a
 * smoother sample target for non-trivial `Sp` sizing without changing the logical layout
 * metrics that widgets already use.
 *
 * Doesn't hold a [io.github.ronjunevaldoz.awake.render.texture.TextureAsset] itself (that
 * type lives in `awake-engine-render-api`, which depends on this module -- holding one here
 * would be circular); callers wrap [atlasPixelsRgba] into their own texture.
 */
class BitmapFont(
    val cellSize: Int = 12,
    private val atlasScale: Int = 4
) {
    private val sourceCellSize = 8
    private val chars: List<Char> = GLYPH_ROWS.keys.toList()
    private val columns = chars.size
    private val atlasCellSize = cellSize * atlasScale.coerceAtLeast(1)
    private val atlasInsetPx = maxOf(1, atlasScale - 1)
    private val atlasInnerSize = (atlasCellSize - atlasInsetPx * 2).coerceAtLeast(1)
    val atlasWidth = columns * atlasCellSize
    val atlasHeight = atlasCellSize

    val atlasPixelsRgba: ByteArray = ByteArray(atlasWidth * atlasHeight * 4).also { pixels ->
        chars.forEachIndexed { charIndex, char ->
            val rows = GLYPH_ROWS.getValue(char)
            val cellX = charIndex * atlasCellSize
            for (row in 0 until atlasCellSize) {
                for (col in 0 until atlasCellSize) {
                    val alpha = glyphCoverage(rows, col, row)
                    if (alpha <= 0f) {
                        continue
                    }
                    val pixelX = cellX + col
                    val pixelY = row
                    val offset = (pixelY * atlasWidth + pixelX) * 4
                    val alphaByte = (alpha * 255f).toInt().coerceIn(0, 255).toByte()
                    pixels[offset] = -1 // R
                    pixels[offset + 1] = -1 // G
                    pixels[offset + 2] = -1 // B
                    pixels[offset + 3] = alphaByte
                }
            }
        }
    }

    fun uvFor(char: Char): GlyphRect? {
        val atlasChar = atlasCharFor(char)
        val index = chars.indexOf(atlasChar)
        if (index < 0) return null
        val cellStart = index * atlasCellSize
        val u0 = (cellStart + atlasInsetPx).toFloat() / atlasWidth
        val u1 = (cellStart + atlasCellSize - atlasInsetPx).toFloat() / atlasWidth
        val v0 = atlasInsetPx.toFloat() / atlasHeight
        val v1 = (atlasCellSize - atlasInsetPx).toFloat() / atlasHeight
        return GlyphRect(u0 = u0, v0 = v0, u1 = u1, v1 = v1)
    }

    private fun atlasCharFor(char: Char): Char =
        when {
            GLYPH_ROWS.containsKey(char) -> char
            char.isLowerCase() && GLYPH_ROWS.containsKey(char.uppercaseChar()) -> char.uppercaseChar()
            else -> char
        }

    private fun glyphCoverage(rows: IntArray, atlasX: Int, atlasY: Int): Float {
        if (atlasX < atlasInsetPx || atlasY < atlasInsetPx) return 0f
        if (atlasX >= atlasCellSize - atlasInsetPx || atlasY >= atlasCellSize - atlasInsetPx) return 0f
        var covered = 0
        var total = 0
        for (sampleY in 0 until SUPERSAMPLE_GRID) {
            for (sampleX in 0 until SUPERSAMPLE_GRID) {
                val sourceX = ((atlasX - atlasInsetPx) + (sampleX + 0.5f) / SUPERSAMPLE_GRID) / atlasInnerSize * sourceCellSize
                val sourceY = ((atlasY - atlasInsetPx) + (sampleY + 0.5f) / SUPERSAMPLE_GRID) / atlasInnerSize * sourceCellSize
                val sourceCol = floor(sourceX).toInt()
                val sourceRow = floor(sourceY).toInt()
                if (sourceCol in 0 until sourceCellSize && sourceRow in 0 until sourceCellSize) {
                    val bits = rows[sourceRow]
                    if (((bits shr (sourceCellSize - 1 - sourceCol)) and 1) == 1) {
                        covered += 1
                    }
                }
                total += 1
            }
        }
        return covered.toFloat() / total.toFloat()
    }

    private companion object {
        private const val SUPERSAMPLE_GRID = 4

        // Each entry: 8 row-bitmasks, top to bottom, bit 7 = leftmost of 8 columns. Simple
        // hand-designed block letters -- readable at small size, not a typographic font.
        val GLYPH_ROWS: Map<Char, IntArray> = mapOf(
            ' ' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
            '.' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18, 0x18),
            '-' to intArrayOf(0x00, 0x00, 0x00, 0x7E, 0x00, 0x00, 0x00, 0x00),
            ':' to intArrayOf(0x00, 0x18, 0x18, 0x00, 0x00, 0x18, 0x18, 0x00),
            '0' to intArrayOf(0x7C, 0x82, 0x86, 0x8A, 0x92, 0xA2, 0x82, 0x7C),
            '1' to intArrayOf(0x18, 0x38, 0x18, 0x18, 0x18, 0x18, 0x18, 0x7E),
            '2' to intArrayOf(0x7C, 0x82, 0x02, 0x0C, 0x30, 0x40, 0x80, 0xFE),
            '3' to intArrayOf(0xFE, 0x04, 0x08, 0x1C, 0x02, 0x02, 0x84, 0x78),
            '4' to intArrayOf(0x0C, 0x14, 0x24, 0x44, 0x84, 0xFE, 0x04, 0x04),
            '5' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x02, 0x02, 0x84, 0x78),
            '6' to intArrayOf(0x3C, 0x40, 0x80, 0xFC, 0x82, 0x82, 0x82, 0x7C),
            '7' to intArrayOf(0xFE, 0x02, 0x04, 0x08, 0x10, 0x20, 0x20, 0x20),
            '8' to intArrayOf(0x7C, 0x82, 0x82, 0x7C, 0x82, 0x82, 0x82, 0x7C),
            '9' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x7E, 0x02, 0x04, 0x78),
            'A' to intArrayOf(0x38, 0x44, 0x82, 0x82, 0xFE, 0x82, 0x82, 0x82),
            'B' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x84, 0x84, 0x84, 0xF8),
            'C' to intArrayOf(0x7C, 0x82, 0x80, 0x80, 0x80, 0x80, 0x82, 0x7C),
            'D' to intArrayOf(0xF8, 0x84, 0x82, 0x82, 0x82, 0x82, 0x84, 0xF8),
            'E' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0xFE),
            'F' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0x80),
            'G' to intArrayOf(0x7C, 0x82, 0x80, 0x80, 0x8E, 0x82, 0x82, 0x7C),
            'H' to intArrayOf(0x82, 0x82, 0x82, 0xFE, 0x82, 0x82, 0x82, 0x82),
            'I' to intArrayOf(0x7C, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x7C),
            'J' to intArrayOf(0x3E, 0x04, 0x04, 0x04, 0x04, 0x04, 0x84, 0x78),
            'K' to intArrayOf(0x82, 0x84, 0x88, 0x90, 0x90, 0x88, 0x84, 0x82),
            'L' to intArrayOf(0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0xFE),
            'M' to intArrayOf(0x82, 0xC6, 0xAA, 0x92, 0x82, 0x82, 0x82, 0x82),
            'N' to intArrayOf(0x82, 0xC2, 0xA2, 0x92, 0x8A, 0x86, 0x82, 0x82),
            'O' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C),
            'P' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x80, 0x80, 0x80, 0x80),
            'Q' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x82, 0x8A, 0x84, 0x7A),
            'R' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x90, 0x88, 0x84, 0x82),
            'S' to intArrayOf(0x7C, 0x82, 0x80, 0x7C, 0x02, 0x02, 0x82, 0x7C),
            'T' to intArrayOf(0xFE, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18),
            'U' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C),
            'V' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x44, 0x44, 0x28, 0x10),
            'W' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x82, 0x92, 0xAA, 0x44),
            'X' to intArrayOf(0x82, 0x44, 0x28, 0x10, 0x10, 0x28, 0x44, 0x82),
            'Y' to intArrayOf(0x82, 0x82, 0x44, 0x28, 0x10, 0x10, 0x10, 0x10),
            'Z' to intArrayOf(0xFE, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0xFE),
            '_' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE)
        )
    }
}
