// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Asserts that repeating one glyph renders it at the same weight every time.
 *
 * This exists because nothing else we own could see the defect it guards. Baseline fidelity
 * measures vertical ink-bottom spread; snapshot signatures only assert "pixels match what was
 * last recorded", which they do just as happily for wrong pixels as right ones. Both were green
 * through two separate rounds of visibly uneven text, and each round was caught by a person
 * looking at the screen instead.
 *
 * The measurement is deliberately narrow: the SAME character, drawn repeatedly on one line, must
 * have identical rendered stem width at each instance. Same glyph means same UV window, same
 * metrics and same scale, so any variance can only come from where the quad lands -- sub-pixel
 * phase as fractional advances accumulate, or from a quad whose size was rounded independently
 * of its scale. Both are real defects this repo shipped:
 *
 *  - Rounding each glyph's edges made a glyph's SIZE vary, so a fixed UV window resampled into
 *    differently-sized quads drew the same stroke at different weights.
 *  - A coverage atlas resolved phase differently per instance, so a 1px stem straddling two
 *    texels rendered as two half-lit pixels in one place and one solid pixel in another.
 *
 * MTSDF resolves the edge analytically per pixel, which is what makes this assertion holdable.
 */
class GlyphStemWeightTest {

    /** Single vertical stems, no curves or crossbars: their rendered width IS the stroke weight,
     * with nothing else in the glyph to confound the measurement. */
    private val stemGlyphs = listOf('i', 'l', 'I')

    private val sizesPx = listOf(12f, 14f, 16f)

    /** Widths of each horizontal run of lit pixels on the row through the glyph bodies. With a
     * repeated single-stem glyph, one run == one stem. */
    private fun stemWidths(char: Char, sizePx: Float): List<Int> {
        val width = 640
        val height = 96
        val ui = UiContext()
        ui.beginFrame(width.toFloat(), height.toFloat(), testSnapshot())
        // Enough repetitions that fractional advances sweep through a full range of sub-pixel
        // phases -- a short string can land every instance on a similar phase and hide the bug.
        ui.createAbsolute(x = 16f, y = 48f)
            .text(char.toString().repeat(24), style = Style { textSize(sizePx.sp) })
        // The font MUST be passed: without it the rasterizer draws its null-font placeholder
        // rect for every glyph, and this gate silently measures placeholder geometry instead
        // of glyph sampling. That is exactly what happened -- see
        // docs/tasks/2026-08-10-glyph-scale-regression.md.
        val pixels = ui.endFrame().rasterize(width, height, background = Color.Black, font = UiFonts.default())
        // Coverage lives in the alpha channel: drawGlyph writes the tint's full RGB into any
        // pixel with nonzero coverage, so thresholding red would count the entire AA skirt.
        fun luma(x: Int, y: Int) = pixels[(y * width + x) * 4 + 3].toInt() and 0xFF

        // Sample the row with the most ink: guaranteed to cross every stem, and avoids the
        // ascender/dot rows where 'i' legitimately differs from 'l'.
        val row = (0 until height).maxBy { y -> (0 until width).count { x -> luma(x, y) > 40 } }
        val runs = mutableListOf<Int>()
        var run = 0
        for (x in 0 until width) {
            if (luma(x, row) > 40) {
                run++
            } else if (run > 0) {
                runs += run
                run = 0
            }
        }
        if (run > 0) runs += run
        return runs
    }

    /** Widest stem-width spread currently rendered, in pixels, against a target of 0.
     *
     * Every entry is a defect; the goal is an empty map. Recorded rather than asserted away so
     * it cannot widen unnoticed, matching how `FontBaselineFidelityTest` tracks baseline drift.
     *
     * MTSDF was expected to close this and did not: a distance field resolves an edge
     * analytically, but the glyph quad still lands at a different sub-pixel PHASE per instance
     * as fractional advances accumulate, and a 1px stem straddling two device pixels still
     * resolves to two half-lit columns in one instance and one solid column in another. */
    private val knownStemWidthSpread = 1

    @Test
    fun oneGlyphRepeatedRendersAtOneWeight() {
        val failures = mutableListOf<String>()
        stemGlyphs.forEach { char ->
            sizesPx.forEach { sizePx ->
                val widths = stemWidths(char, sizePx)
                assertTrue(
                    widths.size >= 8,
                    "'$char' @${sizePx}px produced only ${widths.size} stems; the probe is not measuring what it thinks",
                )
                val spread = widths.max() - widths.min()
                if (spread > knownStemWidthSpread) {
                    failures += "'$char' @${sizePx}px: stem widths ${widths.min()}..${widths.max()} $widths"
                }
            }
        }
        assertEquals(
            emptyList(),
            failures.toList(),
            "the same glyph must render within $knownStemWidthSpread px of one weight " +
                "regardless of sub-pixel phase; shrink knownStemWidthSpread, never widen it:\n" +
                failures.joinToString("\n"),
        )
    }
}
