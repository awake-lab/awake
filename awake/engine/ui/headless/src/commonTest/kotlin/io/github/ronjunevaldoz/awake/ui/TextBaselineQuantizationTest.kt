// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.GlyphRect
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFontSamplingMode
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the fix for visibly "wavy" text -- glyphs on one line sitting a pixel above or below
 * their neighbours.
 *
 * The cause was quantization shape, not font metrics. `Glyph(round(y), round(h))` renders a
 * bottom edge of `round(y) + round(h)`: two independent roundings of two different fractional
 * values. Glyphs resting on one baseline have different `offsetYEm`/`heightEm` (correctly --
 * 'S' and 'o' start at different ink heights), so those fractions differ and the pair of
 * roundings lands their bottoms up to a pixel apart. The baseline they share was never snapped
 * itself; it was re-derived per glyph from two separately-rounded numbers. `BasicText` now
 * rounds the right/bottom EDGES from the same unrounded origin, so glyphs on a common true
 * baseline round to a common bottom.
 *
 * Uses [VaryingMetricsFont] rather than `BitmapFont`: `BitmapFont.uvFor` returns a default
 * [GlyphRect], so every glyph has identical `offsetYEm`/`heightEm` and the defect is
 * arithmetically unreachable -- a BitmapFont version of this test passes against the *broken*
 * code too. That is open-risk 3 in docs/reference/ui-status.md (test font != app font) turning
 * a gate into a decoration, so the metrics that matter are declared here explicitly.
 */
class TextBaselineQuantizationTest {

    /**
     * Minimal font whose glyphs share a baseline while differing in ink height, mimicking the
     * packed Roboto atlas. Each glyph's `offsetYEm + heightEm` is a constant 0.8 (the baseline)
     * with deliberately awkward fractions, so top and height round in different directions.
     * 'p' is the descender: it alone extends past the baseline.
     */
    private class VaryingMetricsFont : UiFont {
        override val samplingMode = UiFontSamplingMode.CoverageAlpha
        override val cellSize = 8
        override val textScaleStep = 1f
        override val atlasWidth = 8
        override val atlasHeight = 8
        override val atlasPixelsRgba = ByteArray(8 * 8 * 4)

        /** top -> ink height; every pair sums to [BASELINE_EM] except the descender. */
        private val metrics = mapOf(
            'T' to (0.13f to 0.67f),
            'o' to (0.31f to 0.49f),
            'l' to (0.09f to 0.71f),
            'e' to (0.33f to 0.47f),
            'p' to (0.31f to 0.63f), // descends 0.14em below the baseline
        )

        override fun uvFor(char: Char): GlyphRect? {
            val (top, height) = metrics[char] ?: return null
            return GlyphRect(u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f, offsetYEm = top, heightEm = height)
        }

        override fun advanceFor(char: Char, glyphPx: Float) = glyphPx * ADVANCE_EM
    }

    private fun glyphsOf(label: String, slotY: Float): List<UiDrawPrimitive.Glyph> {
        val font = VaryingMetricsFont()
        val ui = UiContext()
        ui.pushFont(font)
        ui.createAbsolute(x = 0f, y = slotY).text(
            label = label,
            slot = UiBounds(0f, slotY, 400f, GLYPH_PX),
            font = font,
        )
        return ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
    }

    @Test
    fun glyphsOnOneLineShareOneQuantizedBaseline() {
        // A fractional slot origin is the point: an integral one lets each glyph's fraction come
        // from its own metrics alone, which hides half the defect.
        val glyphs = glyphsOf("Tole", slotY = 20.4f)
        assertEquals(4, glyphs.size, "expected one quad per glyph")

        val bottoms = glyphs.map { it.y + it.h }.distinct()
        assertEquals(
            1,
            bottoms.size,
            "glyphs on one line must share a single baseline; found ${bottoms.size} distinct " +
                "bottom edges: ${bottoms.sorted()}. This is the wavy-text regression.",
        )
    }

    @Test
    fun descenderStillExtendsBelowThatBaseline() {
        // The fix must not "align" everything by flattening descenders onto the baseline --
        // that would be an equally wrong all-green result.
        val glyphs = glyphsOf("op", slotY = 20.4f)
        val (o, p) = glyphs
        assertTrue(
            p.y + p.h > o.y + o.h,
            "'p' must render below the shared baseline, got ${p.y + p.h} vs ${o.y + o.h}",
        )
    }

    private companion object {
        const val GLYPH_PX = 14f
        const val ADVANCE_EM = 0.6f
        const val BASELINE_EM = 0.8f
    }
}
