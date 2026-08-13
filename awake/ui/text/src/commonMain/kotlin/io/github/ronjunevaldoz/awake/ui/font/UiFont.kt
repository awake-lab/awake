// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import io.github.ronjunevaldoz.awake.ui.font.FontWeight

enum class UiFontSamplingMode {
    CoverageAlpha,
    DistanceField,
}

data class GlyphRect(
    val u0: Float,
    val v0: Float,
    val u1: Float,
    val v1: Float,
    val offsetXEm: Float = 0f,
    val offsetYEm: Float = 0f,
    val widthEm: Float = 1f,
    val heightEm: Float = 1f,
)

interface UiFont {
    val samplingMode: UiFontSamplingMode
    val cellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val atlasPixelsRgba: ByteArray

    /** Ascent + descent as a multiple of the em size. 1.0 for fonts whose glyphs never exceed
     * their em box; the packed Roboto atlas reports ~1.19. */
    val lineHeightEm: Float get() = 1f

    val visibleTopEm: Float
        get() = 0f
    val visibleBottomEm: Float
        get() = 1f
    val distanceFieldRangePx: Float
        get() = 0f
    val ascentEm: Float
        get() = 0.8f
    val descentEm: Float
        get() = 0.2f
    val capHeightEm: Float
        get() = 0.7f

    fun uvFor(char: Char): GlyphRect?

    /** Glyph atlas entry for a requested weight. Fonts with one packed face fall back to [uvFor]. */
    fun glyphFor(char: Char, weight: FontWeight): GlyphRect? = uvFor(char)

    fun advanceFor(char: Char, glyphPx: Float): Float = glyphPx

    /**
     * Advance for a requested weight. A single-face atlas must keep its original metrics; it
     * cannot synthesize a different weight by widening the pen, because that changes intrinsic
     * layout and can push the trailing glyph outside its slot. Platform providers may override
     * this with measurements from a real weighted face.
     */
    fun advanceFor(char: Char, glyphPx: Float, weight: FontWeight): Float = advanceFor(char, glyphPx)
}

/**
 * Sums per-glyph [advanceFor] to get the pen distance the string travels -- but a glyph's own
 * ink (its [GlyphRect.offsetXEm]/[GlyphRect.widthEm] quad) can extend past its own advance;
 * several packed fonts (e.g. the embedded Roboto data) declare advances a few percent narrower
 * than the glyph's actual right edge (positive `offsetXEm + widthEm - advance`), which every
 * *interior* glyph gets away with since the next glyph's quad simply overlaps/redraws over that
 * overhang -- only the string's trailing glyph has nothing after it to hide the clip. A caller
 * that boxes text exactly to this width (see `shadcnLabel`) would otherwise clip that last
 * glyph's overhanging pixels. Widen the result to also cover the last glyph's real right edge.
 */
fun UiFont.measureTextWidth(label: String, glyphPx: Float, weight: FontWeight = FontWeight.Normal): Float {
    var width = 0f
    var lastChar: Char? = null
    label.forEach { char ->
        if (char != '\n') {
            width += advanceFor(char, glyphPx, weight)
            lastChar = char
        }
    }
    val trailingChar = lastChar ?: return width
    val glyph = glyphFor(trailingChar, weight) ?: return width
    val lastAdvance = advanceFor(trailingChar, glyphPx, weight)
    val lastGlyphRightEdge = (width - lastAdvance) + (glyph.offsetXEm + glyph.widthEm) * glyphPx
    return maxOf(width, lastGlyphRightEdge)
}
