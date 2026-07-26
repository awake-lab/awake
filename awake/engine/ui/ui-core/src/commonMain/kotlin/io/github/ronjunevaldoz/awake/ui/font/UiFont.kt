// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

enum class UiFontSamplingMode {
    CoverageAlpha,
    DistanceField
}

data class GlyphRect(
    val u0: Float,
    val v0: Float,
    val u1: Float,
    val v1: Float,
    val offsetXEm: Float = 0f,
    val offsetYEm: Float = 0f,
    val widthEm: Float = 1f,
    val heightEm: Float = 1f
)

interface UiFont {
    val samplingMode: UiFontSamplingMode
    val cellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val atlasPixelsRgba: ByteArray
    val visibleTopEm: Float
        get() = 0f
    val visibleBottomEm: Float
        get() = 1f
    val distanceFieldRangePx: Float
        get() = 0f

    fun uvFor(char: Char): GlyphRect?

    fun advanceFor(char: Char, glyphPx: Float): Float = glyphPx
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
fun UiFont.measureTextWidth(label: String, glyphPx: Float): Float {
    var width = 0f
    var lastChar: Char? = null
    label.forEach { char ->
        if (char != '\n') {
            width += advanceFor(char, glyphPx)
            lastChar = char
        }
    }
    val trailingChar = lastChar ?: return width
    val glyph = uvFor(trailingChar) ?: return width
    val lastAdvance = advanceFor(trailingChar, glyphPx)
    val lastGlyphRightEdge = (width - lastAdvance) + (glyph.offsetXEm + glyph.widthEm) * glyphPx
    return maxOf(width, lastGlyphRightEdge)
}
