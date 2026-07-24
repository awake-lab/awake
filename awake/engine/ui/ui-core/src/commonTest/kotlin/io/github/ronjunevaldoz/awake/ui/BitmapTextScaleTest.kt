// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class BitmapTextScaleTest {

    @Test
    fun pixelPerfectTextScaleSnapsToNearestQuarterStepByDefault() {
        assertEquals(1f, pixelPerfectTextScale(1f))
        assertEquals(1.25f, pixelPerfectTextScale(1.2f))
        assertEquals(1.75f, pixelPerfectTextScale(1.75f))
        assertEquals(2f, pixelPerfectTextScale(2.1f))
    }

    @Test
    fun pixelPerfectTextScaleHonorsCustomAtlasStep() {
        assertEquals(1f, pixelPerfectTextScale(1f, step = 0.5f))
        assertEquals(1.5f, pixelPerfectTextScale(1.3f, step = 0.5f))
    }

    @Test
    fun pixelPerfectTextScaleNeverDropsBelowOne() {
        assertEquals(1f, pixelPerfectTextScale(0.2f))
        assertEquals(1f, pixelPerfectTextScale(0f))
    }
}
