// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RoundedQuadTest {

    @Test
    fun equalsFoldsInRadius() {
        val color = Color.White
        val a = UiDrawPrimitive.RoundedQuad(0f, 0f, 10f, 10f, color, radius = 4f)
        val b = UiDrawPrimitive.RoundedQuad(0f, 0f, 10f, 10f, color, radius = 8f)
        assertNotEquals(a, b, "two otherwise-identical RoundedQuads with different radius must not be equal")
    }

    @Test
    fun equalsMatchesWhenEverythingIsIdentical() {
        val color = Color.White
        val a = UiDrawPrimitive.RoundedQuad(0f, 0f, 10f, 10f, color, radius = 4f)
        val b = UiDrawPrimitive.RoundedQuad(0f, 0f, 10f, 10f, color, radius = 4f)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
