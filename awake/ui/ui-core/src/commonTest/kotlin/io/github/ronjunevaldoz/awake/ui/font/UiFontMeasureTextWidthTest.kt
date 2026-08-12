// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression coverage for the "shadcnLabel clips the last character" bug (Task #33): a box sized
 * exactly to [UiFont.measureTextWidth] must fully contain the trailing glyph's own ink, not just
 * the pen distance the string travels. The embedded Roboto data declares every glyph's advance a
 * few percent narrower than its actual quad (`offsetXEm + widthEm`), which only becomes visible
 * clipping on a string's last character (interior glyphs get overdrawn by their neighbor).
 */
class UiFontMeasureTextWidthTest {

    private val font = UiFonts.trueSans()
    private val glyphPx = 24f

    /** Ground truth: the pixel position where the trailing glyph's ink actually ends. */
    private fun trueRightEdgePx(label: String): Float {
        var penX = 0f
        var rightEdge = 0f
        label.forEach { char ->
            val glyph = font.uvFor(char)
            if (glyph != null) {
                rightEdge = penX + (glyph.offsetXEm + glyph.widthEm) * glyphPx
            }
            penX += font.advanceFor(char, glyphPx)
        }
        return rightEdge
    }

    @Test
    fun measuredWidthFullyContainsTrailingGlyphInk() {
        for (label in listOf("Style", "Base", "Mode", "Accent", "Draw", "Flow")) {
            val measured = font.measureTextWidth(label, glyphPx)
            val trueRightEdge = trueRightEdgePx(label)
            assertTrue(
                measured >= trueRightEdge - 0.001f,
                "measureTextWidth(\"$label\") = $measured must be >= the last glyph's true right " +
                    "edge $trueRightEdge, otherwise a box sized to it clips that glyph",
            )
        }
    }
}
