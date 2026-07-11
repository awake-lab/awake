// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

/** Normalized UV rect for one glyph cell in [BitmapFont.atlasPixelsRgba]. */
data class GlyphRect(val u0: Float, val v0: Float, val u1: Float, val v1: Float)

/**
 * A minimal, hand-authored 8x8 monospace bitmap font -- deliberately not `msdf-bmfont-xml`/
 * MSDF rendering (see docs/MVP_PLAN.md's custom-UI decision log): this is a debug-catalog
 * label, not user-facing typography, so a real signed-distance-field toolchain + shader
 * would be solving a problem this UI doesn't have yet.
 *
 * Scoped to exactly the characters `sample-hello-cube`'s verification label needs (not full
 * ASCII) -- extending coverage means adding another row to [GLYPH_ROWS] below, each entry a
 * `Char` to 8 row-bitmasks (bit 7 = leftmost pixel). Builds its own atlas pixel buffer at
 * construction time (one row of 8x8 cells, white-on-transparent) rather than loading an
 * external asset -- no offline generation step, no metadata file to parse.
 *
 * Doesn't hold a [io.github.ronjunevaldoz.awake.render.texture.TextureAsset] itself (that
 * type lives in `awake-engine-render-api`, which depends on this module -- holding one here
 * would be circular); callers wrap [atlasPixelsRgba] into their own texture.
 */
class BitmapFont {
    val cellSize = 8
    private val chars: List<Char> = GLYPH_ROWS.keys.toList()
    private val columns = chars.size
    val atlasWidth = columns * cellSize
    val atlasHeight = cellSize

    val atlasPixelsRgba: ByteArray = ByteArray(atlasWidth * atlasHeight * 4).also { pixels ->
        chars.forEachIndexed { charIndex, char ->
            val rows = GLYPH_ROWS.getValue(char)
            val cellX = charIndex * cellSize
            for (row in 0 until cellSize) {
                val bits = rows[row]
                for (col in 0 until cellSize) {
                    val bitSet = (bits shr (cellSize - 1 - col)) and 1 == 1
                    val pixelX = cellX + col
                    val pixelY = row
                    val offset = (pixelY * atlasWidth + pixelX) * 4
                    if (bitSet) {
                        pixels[offset] = -1 // R
                        pixels[offset + 1] = -1 // G
                        pixels[offset + 2] = -1 // B
                        pixels[offset + 3] = -1 // A (opaque)
                    }
                    // else leave as zero -- fully transparent
                }
            }
        }
    }

    fun uvFor(char: Char): GlyphRect? {
        val index = chars.indexOf(char)
        if (index < 0) return null
        val u0 = (index * cellSize).toFloat() / atlasWidth
        val u1 = ((index + 1) * cellSize).toFloat() / atlasWidth
        return GlyphRect(u0 = u0, v0 = 0f, u1 = u1, v1 = 1f)
    }

    private companion object {
        // Each entry: 8 row-bitmasks, top to bottom, bit 7 = leftmost of 8 columns. Simple
        // hand-designed block letters -- readable at small size, not a typographic font.
        val GLYPH_ROWS: Map<Char, IntArray> = mapOf(
            ' ' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
            ':' to intArrayOf(0x00, 0x18, 0x18, 0x00, 0x00, 0x18, 0x18, 0x00),
            'B' to intArrayOf(0xF8, 0x84, 0x84, 0xF8, 0x84, 0x84, 0x84, 0xF8),
            'D' to intArrayOf(0xF8, 0x84, 0x82, 0x82, 0x82, 0x82, 0x84, 0xF8),
            'E' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0xFE),
            'F' to intArrayOf(0xFE, 0x80, 0x80, 0xFC, 0x80, 0x80, 0x80, 0x80),
            'G' to intArrayOf(0x7C, 0x82, 0x80, 0x80, 0x8E, 0x82, 0x82, 0x7C),
            'N' to intArrayOf(0x82, 0xC2, 0xA2, 0x92, 0x8A, 0x86, 0x82, 0x82),
            'O' to intArrayOf(0x7C, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C),
            'U' to intArrayOf(0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x82, 0x7C)
        )
    }
}
