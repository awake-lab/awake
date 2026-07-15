// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
