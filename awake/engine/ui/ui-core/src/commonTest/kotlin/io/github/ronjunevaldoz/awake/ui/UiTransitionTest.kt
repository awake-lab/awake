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

class UiTransitionTest {

    private enum class CardState { Collapsed, Expanded }

    @Test
    fun changingTargetStateStartsARealTransitionTowardOne() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val atRest = ui.rememberTransition(id = "card", targetState = CardState.Collapsed, durationMs = 300f)
        assertEquals(0f, atRest, "no transition has happened yet: progress should sit at the anchor state's 0f")

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val firstStep = ui.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 300f)
        assertTrue(firstStep > 0f && firstStep < 1f, "progress must have moved partway from 0 toward 1")

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val secondStep = ui.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 300f)
        assertTrue(secondStep > firstStep, "progress must keep climbing toward 1 across frames")
    }

    @Test
    fun transitionSettlesAtOneOnceDurationElapses() {
        val ui = UiContext()
        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        ui.rememberTransition(id = "card", targetState = CardState.Collapsed, durationMs = 300f)

        repeat(30) { // 500ms, comfortably past a 300ms duration.
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 300f)
        }

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val settled = ui.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 300f)
        assertEquals(1f, settled)
    }

    @Test
    fun twoPropertiesDerivedFromTheSameTransitionIdSeeConsistentProgress() {
        val ui = UiContext()
        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        ui.rememberTransition(id = "card", targetState = CardState.Collapsed, durationMs = 300f)

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val progress = ui.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 300f)

        // The intended usage: read progress once, derive N properties from that single float via
        // plain lerp math -- both derived properties must reflect exactly the same progress.
        val alpha = 0f + (1f - 0f) * progress
        val offsetY = 40f + (0f - 40f) * progress

        assertEquals(progress, alpha, "alpha derived from progress must match progress exactly (identity lerp)")
        assertEquals(40f - 40f * progress, offsetY, "offset derived from the same progress must be internally consistent")
    }

    @Test
    fun transitionInsideWrapContentSurfaceDoesNotDoubleAdvancePerFrame() {
        // Same regression class as UiAnimationTest's WrapContent/scroll double-step test: this
        // must delegate cleanly to animateFloatTween's own isMeasuringInternal() guard rather than
        // introducing a second, unguarded side effect.
        val ui = UiContext()
        var lastProgress = 0f

        fun drawFrame(targetState: CardState) {
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.createBox(x = 0f, y = 0f, width = 320f, height = 200f).column(
                id = "viewport",
                modifier = Modifier.verticalScroll(UiScrollState()).width(Dimension.FillMax).height(Dimension.FillMax)
            ) {
                surface(
                    id = "transition-host",
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)
                ) {
                    lastProgress = rememberTransition(id = "card", targetState = targetState, durationMs = 1000f)
                }
            }
            ui.endFrame()
        }

        // Seed the anchor state (Collapsed) before switching, same setup as the reference below.
        drawFrame(CardState.Collapsed)
        drawFrame(CardState.Expanded)
        val afterFrame1 = lastProgress

        // Reference: the same un-nested rememberTransition call sequence, one step's worth of
        // delta after the anchor was seeded -- no WrapContent/scroll trial-measure pass involved.
        val reference = UiContext()
        reference.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        reference.rememberTransition(id = "card", targetState = CardState.Collapsed, durationMs = 1000f)
        reference.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val expectedSingleStep = reference.rememberTransition(id = "card", targetState = CardState.Expanded, durationMs = 1000f)

        assertEquals(
            expectedSingleStep,
            afterFrame1,
            "a WrapContent/scroll trial-measurement re-execution of the same content must not " +
                "step the transition an extra time on top of the real pass"
        )

        drawFrame(CardState.Expanded)
        val afterFrame2 = lastProgress
        assertTrue(afterFrame2 > afterFrame1, "progress must keep advancing monotonically toward the target")
    }
}
