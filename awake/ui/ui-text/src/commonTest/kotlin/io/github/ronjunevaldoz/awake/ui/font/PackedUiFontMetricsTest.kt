// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the vertical metrics [PackedUiFont] derives from the packed Roboto atlas.
 *
 * These were previously inherited from [UiFont]'s fabricated defaults (ascent 0.8, descent 0.2,
 * cap height 0.7) because `PackedUiFont` never overrode them -- so anything centering text
 * against them, including `inspectOpticalCentering`, was working from invented numbers.
 */
class PackedUiFontMetricsTest {

    // Narrowed to PackedUiFont on purpose: metrics must be verified against inkFor()'s
    // outline-exact box, not uvFor()'s render quad, which carries the crop bleed and texel
    // snap. Checking a baseline against the quad reads padding as ink.
    // `default()` is intentionally platform-backed on desktop now. This test pins the
    // bundled Roboto atlas specifically, so request that implementation explicitly.
    private val font = UiFonts.trueSans() as PackedUiFont

    @Test
    fun baselineComesFromFlatCapitalsNotInkExtremes() {
        // Every flat-bottomed capital must agree, or the reference-glyph approach is unsound.
        val bottoms = listOf('H', 'I', 'E', 'T', 'X').map { char ->
            val glyph = font.inkFor(char)!!
            glyph.offsetYEm + glyph.heightEm
        }
        assertEquals(1, bottoms.distinct().size, "flat capitals disagree on the baseline: $bottoms")
        assertEquals(bottoms.first(), font.ascentEm, "ascentEm must be the measured baseline")
    }

    // 2026-08-10: re-pinned after tools/generate_ui_font_atlas.py was replaced by
    // :awake:ui:font-atlas-generator, which reads these from the TTF's own outline
    // geometry (continuous doubles) instead of the antialiased raster ink bbox (quantized to
    // 1/64 em). Values are no longer exact multiples of 1/64 -- that is the fix, not a defect.
    @Test
    fun metricsAreTheMeasuredValuesNotTheInterfaceDefaults() {
        assertEquals(0.927735f, font.ascentEm)
        assertEquals(0.22656202f, font.descentEm)
        assertEquals(0.710938f, font.capHeightEm)
    }

    @Test
    fun inkExtremesAreDrivenByOutlierGlyphsSoTheyCannotStandInForMetrics() {
        // The reason the overrides exist. visibleTop/visibleBottom are set by '$' and '(' --
        // punctuation, not the typographic ascent/descent -- so centering against that band
        // makes every label's position hostage to which symbols the atlas happens to pack.
        // ('(' had a tied bottom with ')'; '@' no longer reaches as deep once outline geometry
        // replaced the raster bbox -- see the file header for why the qualifying glyph changed.)
        assertEquals(font.visibleTopEm, font.inkFor('$')!!.offsetYEm)
        val paren = font.inkFor('(')!!
        assertEquals(font.visibleBottomEm, paren.offsetYEm + paren.heightEm)
        assertTrue(
            font.visibleTopEm < font.ascentEm - font.capHeightEm,
            "'\$' should reach above cap height, else this test no longer proves the point",
        )
    }

    @Test
    fun descendersSitBetweenTheBaselineAndTheDescentBound() {
        val descentBound = font.ascentEm + font.descentEm
        listOf('g', 'p', 'y', 'j').forEach { char ->
            val glyph = font.inkFor(char)!!
            val bottom = glyph.offsetYEm + glyph.heightEm
            assertTrue(bottom > font.ascentEm, "'$char' should fall below the baseline, got $bottom")
            assertTrue(bottom <= descentBound, "'$char' bottom $bottom exceeds descent $descentBound")
        }
    }
}
