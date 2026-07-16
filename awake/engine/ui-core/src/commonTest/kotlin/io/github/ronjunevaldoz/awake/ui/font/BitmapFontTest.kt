// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class BitmapFontTest {

    @Test
    fun lowercaseGlyphsAliasToUppercaseAtlasCells() {
        val font = BitmapFont()

        val lower = font.uvFor('g')
        val upper = font.uvFor('G')

        assertNotNull(lower)
        assertNotNull(upper)
        assertEquals(upper, lower)
    }

    @Test
    fun atlasUsesHigherResolutionCoverageThanLogicalGlyphSize() {
        val font = BitmapFont()

        assertTrue(font.atlasHeight > font.cellSize, "atlas should carry more detail than the logical glyph advance size")
        assertEquals(0.25f, font.textScaleStep, "default font atlas should expose quarter-step sizing granularity")
    }

    @Test
    fun atlasContainsIntermediateAlphaCoverageForSmoothedEdges() {
        val font = BitmapFont()
        val alphas = font.atlasPixelsRgba.filterIndexed { index, _ -> index % 4 == 3 }.map { it.toInt() and 0xFF }

        assertTrue(alphas.any { it in 1 until 255 }, "coverage atlas should include partially transparent edge pixels")
    }
}
