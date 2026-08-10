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

    private val font = UiFonts.default()

    @Test
    fun baselineComesFromFlatCapitalsNotInkExtremes() {
        // Every flat-bottomed capital must agree, or the reference-glyph approach is unsound.
        val bottoms = listOf('H', 'I', 'E', 'T', 'X').map { char ->
            val glyph = font.uvFor(char)!!
            glyph.offsetYEm + glyph.heightEm
        }
        assertEquals(1, bottoms.distinct().size, "flat capitals disagree on the baseline: $bottoms")
        assertEquals(bottoms.first(), font.ascentEm, "ascentEm must be the measured baseline")
    }

    @Test
    fun metricsAreTheMeasuredValuesNotTheInterfaceDefaults() {
        assertEquals(1.046875f, font.ascentEm)
        assertEquals(0.21875f, font.descentEm)
        assertEquals(0.75f, font.capHeightEm)
    }

    @Test
    fun inkExtremesAreDrivenByOutlierGlyphsSoTheyCannotStandInForMetrics() {
        // The reason the overrides exist. visibleTop/visibleBottom are set by '(' and '@' --
        // punctuation, not the typographic ascent/descent -- so centering against that band
        // makes every label's position hostage to which symbols the atlas happens to pack.
        assertEquals(font.visibleTopEm, font.uvFor('(')!!.offsetYEm)
        val at = font.uvFor('@')!!
        assertEquals(font.visibleBottomEm, at.offsetYEm + at.heightEm)
        assertTrue(
            font.visibleTopEm < font.ascentEm - font.capHeightEm,
            "'(' should reach above cap height, else this test no longer proves the point",
        )
    }

    @Test
    fun descendersSitBetweenTheBaselineAndTheDescentBound() {
        val descentBound = font.ascentEm + font.descentEm
        listOf('g', 'p', 'y', 'j').forEach { char ->
            val glyph = font.uvFor(char)!!
            val bottom = glyph.offsetYEm + glyph.heightEm
            assertTrue(bottom > font.ascentEm, "'$char' should fall below the baseline, got $bottom")
            assertTrue(bottom <= descentBound, "'$char' bottom $bottom exceeds descent $descentBound")
        }
    }
}
