// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Asserts rendered ink SIZE against the font's own metrics -- the absolute gate this repo never
 * had. Every other font gate is scale-invariant (baseline spread) or self-referential (snapshot
 * signatures, stem consistency), which is how glyphs could render at a fraction of their metrics
 * with every gate green: the 2026-08-10 "0.6x glyphs" investigation turned out to be exactly
 * that, a harness measuring the rasterizer's null-font placeholder rects while believing it
 * measured glyph sampling (docs/tasks/2026-08-10-glyph-scale-regression.md).
 *
 * 'H' is the measuring stick because capHeightEm is derived from its ink: flat top on the cap
 * line, flat bottom on the baseline, no overshoot. Its rendered ink height must therefore equal
 * capHeightEm * sizePx, and its width widthEm * sizePx, within a pixel of quantization. This
 * fails against the placeholder-rect path (which draws ~0.5-0.62x of the quad), so the gate also
 * guards the harness itself against ever silently losing its font again.
 */
class GlyphAbsoluteSizeTest {

    /** Half of the stored alpha range. The metric boundary is the outline (signed distance
     * 0.5), which stores as ~158 after stem-darkening gamma; thresholding at half range keeps
     * boundary pixels while excluding the antialiasing skirt, whose rows would otherwise
     * overcount ink height by up to a pixel at larger sizes. */
    private val inkAlpha = 128

    private val sizesPx = listOf(12f, 14f, 16f)

    private data class InkBox(val width: Int, val height: Int)

    /** A pixel scan is inherently a nested loop over rows and columns; flattening it into helpers
     * would hide what is being measured. */
    @Suppress("NestedBlockDepth")
    private fun renderedInk(sizePx: Float): InkBox {
        val width = 64
        val height = 64
        val ui = UiContext()
        ui.beginFrame(width.toFloat(), height.toFloat(), testSnapshot())
        ui.createAbsolute(x = 20f, y = 32f).text("H", style = Style { textSize(sizePx.sp) })
        // Transparent background so the alpha channel holds pure glyph coverage -- an opaque
        // background writes alpha 255 everywhere and the ink scan degenerates to the canvas.
        val pixels = ui.endFrame().rasterize(
            width,
            height,
            background = Color(0f, 0f, 0f, 0f),
            font = UiFonts.default(),
        )
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = -1
        var maxY = -1
        for (y in 0 until height) {
            for (x in 0 until width) {
                if ((pixels[(y * width + x) * 4 + 3].toInt() and 0xFF) > inkAlpha) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        assertTrue(maxX >= 0, "no ink rendered at ${sizePx}px; the probe is not measuring anything")
        return InkBox(width = maxX - minX + 1, height = maxY - minY + 1)
    }

    @Test
    fun capitalHRendersItsOwnCapHeight() {
        val font = UiFonts.default()
        val failures = mutableListOf<String>()
        sizesPx.forEach { sizePx ->
            val ink = renderedInk(sizePx)
            // Height only: capHeightEm is the one public, ink-true, externally-verifiable
            // metric (0.710938 matches Roboto's published cap height). GlyphRect's em fields
            // describe the render quad, which deliberately includes crop bleed, so they are
            // not valid ink expectations.
            val expectedHeight = font.capHeightEm * sizePx
            if (abs(ink.height - expectedHeight) > 1f) {
                failures += "'H' @${sizePx}px: ink height ${ink.height}px, metrics say $expectedHeight"
            }
        }
        assertTrue(
            failures.isEmpty(),
            "rendered ink must match the font's own metrics within a pixel:\n" +
                failures.joinToString("\n"),
        )
    }
}
