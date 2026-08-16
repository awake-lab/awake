// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

/**
 * A minimal, hand-authored 8x8 monospace bitmap font -- deliberately not `msdf-bmfont-xml`/
 * MSDF rendering (see docs/MVP_PLAN.md's custom-UI decision log): this is a debug-catalog
 * label, not user-facing typography, so a real signed-distance-field toolchain + shader
 * would be solving a problem this UI doesn't have yet.
 *
 * Covers the uppercase, lowercase, digit, and punctuation labels this repo's UI actually
 * uses. When a lowercase glyph is missing we still fall back to its uppercase sibling so
 * the atlas degrades gracefully instead of dropping characters entirely. Builds its own
 * atlas pixel buffer at construction time rather than loading an external asset -- no
 * offline generation step, no metadata file to parse.
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
    override val cellSize: Int = 12,
    atlasScale: Int = 4,
) : UiFont {
    override val samplingMode = UiFontSamplingMode.CoverageAlpha
    private val atlasScale = atlasScale.coerceAtLeast(1)
    private val chars: List<Char> = GlyphAtlasSource.chars
    private val columns = chars.size
    override val textScaleStep: Float = 1f / this.atlasScale.toFloat()
    private val atlasCellSize = cellSize * this.atlasScale
    private val atlasInsetPx = maxOf(1, this.atlasScale - 1)
    private val atlasInnerSize = (atlasCellSize - atlasInsetPx * 2).coerceAtLeast(1)
    override val atlasWidth = columns * atlasCellSize
    override val atlasHeight = atlasCellSize
    override val ascentEm: Float = 0.75f
    override val descentEm: Float = 0.125f
    override val capHeightEm: Float = 0.625f

    override val atlasPixelsRgba: ByteArray = ByteArray(atlasWidth * atlasHeight * 4).also { pixels ->
        chars.forEachIndexed { charIndex, char ->
            val rows = GlyphAtlasSource.rowsFor(char) ?: return@forEachIndexed
            val cellX = charIndex * atlasCellSize
            for (row in 0 until atlasCellSize) {
                for (col in 0 until atlasCellSize) {
                    val alpha = GlyphAtlasSource.sampleCoverage(
                        rows = rows,
                        atlasX = col,
                        atlasY = row,
                        atlasInsetPx = atlasInsetPx,
                        atlasCellSize = atlasCellSize,
                        atlasInnerSize = atlasInnerSize,
                        supersampleGrid = SUPERSAMPLE_GRID,
                    )
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

    override fun uvFor(char: Char): GlyphRect? {
        val atlasChar = GlyphAtlasSource.atlasCharFor(char)
        val index = chars.indexOf(atlasChar)
        if (index < 0) return null
        val cellStart = index * atlasCellSize
        val u0 = (cellStart + atlasInsetPx).toFloat() / atlasWidth
        val u1 = (cellStart + atlasCellSize - atlasInsetPx).toFloat() / atlasWidth
        val v0 = atlasInsetPx.toFloat() / atlasHeight
        val v1 = (atlasCellSize - atlasInsetPx).toFloat() / atlasHeight
        return GlyphRect(u0 = u0, v0 = v0, u1 = u1, v1 = v1)
    }

    /**
     * The hand-authored lowercase cells occupy rows 1..6 while capitals and digits reach row 7.
     * Move dedicated lowercase glyphs down one source row to the shared baseline; descenders
     * get one additional row below it. Fallback lowercase characters use their uppercase cell.
     */
    override fun glyphFor(char: Char, weight: FontWeight): GlyphRect? = uvFor(char)?.let { glyph ->
        if (char.isLowerCase() && GlyphAtlasSource.rowsFor(char) != null && GlyphAtlasSource.atlasCharFor(char) == char) {
            val row = 1f / GlyphAtlasSource.SOURCE_CELL_SIZE
            glyph.copy(offsetYEm = if (char in DESCENDERS) row * 2f else row)
        } else {
            glyph
        }
    }

    override fun advanceFor(char: Char, glyphPx: Float): Float = GlyphAtlasSource.advanceFor(char, glyphPx)

    private companion object {
        private const val SUPERSAMPLE_GRID = 4
        private const val DESCENDERS = "gjpqy"
    }
}
