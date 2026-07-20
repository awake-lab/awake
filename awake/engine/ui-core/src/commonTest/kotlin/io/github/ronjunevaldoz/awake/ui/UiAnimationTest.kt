// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiAnimationTest {

    @Test
    fun animateFloatStepMovesTowardTheTarget() {
        val next = animateFloatStep(
            current = 0f,
            target = 10f,
            deltaSeconds = 1f / 60f,
            responsiveness = 12f
        )

        assertTrue(next > 0f)
        assertTrue(next < 10f)
    }

    @Test
    fun uiContextAnimateFloatPersistsAcrossFrames() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val first = ui.animateFloat(id = "panel-lift", target = 12f, initial = 0f, responsiveness = 12f)

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val second = ui.animateFloat(id = "panel-lift", target = 12f, initial = 0f, responsiveness = 12f)

        assertTrue(second > first, "subsequent frames should continue easing toward the same target")
    }

    @Test
    fun zeroResponsivenessSnapsToTarget() {
        assertEquals(
            expected = 8f,
            actual = animateFloatStep(current = 1f, target = 8f, deltaSeconds = 1f / 60f, responsiveness = 0f)
        )
    }
}
