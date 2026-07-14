// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class BitmapTextScaleTest {

    @Test
    fun pixelPerfectTextScaleSnapsToNearestWholeMultiplier() {
        assertEquals(1f, pixelPerfectTextScale(1f))
        assertEquals(2f, pixelPerfectTextScale(1.75f))
        assertEquals(2f, pixelPerfectTextScale(2.1f))
    }

    @Test
    fun pixelPerfectTextScaleNeverDropsBelowOne() {
        assertEquals(1f, pixelPerfectTextScale(0.2f))
        assertEquals(1f, pixelPerfectTextScale(0f))
    }
}
