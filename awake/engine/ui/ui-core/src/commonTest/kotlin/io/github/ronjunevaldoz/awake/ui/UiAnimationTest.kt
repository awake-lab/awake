// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
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
    fun animateFloatInsideWrapContentSurfaceDoesNotDoubleAdvancePerFrame() {
        // Regression test for the shimmer-looks-broken report: UiContextMeasureState now shares
        // the real state store with WrapContent/scroll trial-measurement passes (see 7a999d09),
        // which re-execute the same content. animateFloat() must therefore step exactly once per
        // real frame even when its call site sits inside a WrapContent surface nested in a
        // verticalScroll column -- the exact nesting depth a shimmer-decorated label lives at.
        val ui = UiContext()
        var lastPhase = 0f

        fun drawFrame() {
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.createBox(x = 0f, y = 0f, width = 320f, height = 200f).column(
                id = "viewport",
                modifier = Modifier.verticalScroll(UiScrollState()).width(Dimension.FillMax).height(Dimension.FillMax)
            ) {
                surface(
                    id = "shimmer-host",
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)
                ) {
                    lastPhase = animateFloat(id = "shimmer-phase", target = 1f, initial = 0f, responsiveness = 12f)
                }
            }
            ui.endFrame()
        }

        drawFrame()
        val afterFrame1 = lastPhase

        // Reference: a single un-nested animateFloat call stepping from the same starting point
        // over one frame's worth of delta -- this is what "advances once per real frame" means.
        val expectedSingleStep = animateFloatStep(
            current = 0f,
            target = 1f,
            deltaSeconds = 1f / 60f,
            responsiveness = 12f
        )
        assertEquals(
            expectedSingleStep,
            afterFrame1,
            "a WrapContent/scroll trial-measurement re-execution of the same content must not " +
                "step the animation an extra time on top of the real pass"
        )

        drawFrame()
        val afterFrame2 = lastPhase
        val expectedTwoSteps = animateFloatStep(
            current = expectedSingleStep,
            target = 1f,
            deltaSeconds = 1f / 60f,
            responsiveness = 12f
        )
        assertEquals(expectedTwoSteps, afterFrame2, "each real frame must advance the phase by exactly one step")
        assertTrue(afterFrame2 > afterFrame1, "phase must keep advancing monotonically toward the target")
    }

    @Test
    fun zeroResponsivenessSnapsToTarget() {
        assertEquals(
            expected = 8f,
            actual = animateFloatStep(current = 1f, target = 8f, deltaSeconds = 1f / 60f, responsiveness = 0f)
        )
    }
}
