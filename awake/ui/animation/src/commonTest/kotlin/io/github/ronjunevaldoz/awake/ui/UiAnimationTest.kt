// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
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
            responsiveness = 12f,
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
                modifier = Modifier.verticalScroll(UiScrollState()).width(Dimension.FillMax).height(Dimension.FillMax),
            ) {
                surface(
                    id = "shimmer-host",
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent),
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
            responsiveness = 12f,
        )
        assertEquals(
            expectedSingleStep,
            afterFrame1,
            "a WrapContent/scroll trial-measurement re-execution of the same content must not " +
                "step the animation an extra time on top of the real pass",
        )

        drawFrame()
        val afterFrame2 = lastPhase
        val expectedTwoSteps = animateFloatStep(
            current = expectedSingleStep,
            target = 1f,
            deltaSeconds = 1f / 60f,
            responsiveness = 12f,
        )
        assertEquals(expectedTwoSteps, afterFrame2, "each real frame must advance the phase by exactly one step")
        assertTrue(afterFrame2 > afterFrame1, "phase must keep advancing monotonically toward the target")
    }

    @Test
    fun largeDeltaSpikeDoesNotSnapMostOfTheWayToTarget() {
        // Regression for a live "still snapping on collapse" report: every other test in this
        // file drives a fixed 1/60f delta, which never exercises a real frame-time spike (a GC
        // pause, a dropped frame, a window resize). Unclamped, responsiveness=8's exponential
        // factor at delta=0.3s is ~0.91 -- the animated value would jump 91% of the way to
        // target in a single step, reading as a visible snap.
        val next = animateFloatStep(current = 0f, target = 120f, deltaSeconds = 0.3f, responsiveness = 8f)
        assertTrue(next < 60f, "a single frame-time spike must not jump the value most of the way to target, was $next")
    }

    @Test
    fun zeroResponsivenessSnapsToTarget() {
        assertEquals(
            expected = 8f,
            actual = animateFloatStep(current = 1f, target = 8f, deltaSeconds = 1f / 60f, responsiveness = 0f),
        )
    }

    @Test
    fun linearEasingIsLinearAtMidpoint() {
        assertEquals(0.5f, LinearEasing.transform(0.5f), absoluteTolerance = 1e-4f)
    }

    @Test
    fun easeInStartsSlowerThanLinear() {
        assertTrue(EaseIn.transform(0.25f) < 0.25f, "ease-in should lag behind linear progress early on")
    }

    @Test
    fun easeOutStartsFasterThanLinear() {
        assertTrue(EaseOut.transform(0.25f) > 0.25f, "ease-out should lead linear progress early on")
    }

    @Test
    fun easeInOutDeviatesInBothDirections() {
        assertTrue(EaseInOut.transform(0.25f) < 0.25f, "ease-in-out should start slow like ease-in")
        assertTrue(EaseInOut.transform(0.75f) > 0.75f, "ease-in-out should finish fast like ease-out")
    }

    @Test
    fun tweenCompletesExactlyAtTargetOnceElapsedReachesDuration() {
        assertEquals(
            expected = 10f,
            actual = animateFloatTweenStep(startValue = 0f, target = 10f, elapsedMs = 300f, durationMs = 300f, easing = LinearEasing),
        )
        assertEquals(
            expected = 10f,
            actual = animateFloatTweenStep(startValue = 0f, target = 10f, elapsedMs = 500f, durationMs = 300f, easing = LinearEasing),
            "elapsed past duration must clamp, not overshoot",
        )
    }

    @Test
    fun uiContextAnimateFloatTweenSamplesMultipleFramesAlongTheCurve() {
        // Per this session's animation-coverage rule: sample several points along the curve, not
        // just the rest frame, so a regression that only breaks mid-tween interpolation (and not
        // the initial/final values) would still be caught.
        val ui = UiContext()
        val durationMs = 1000f // 6 frames of 1/60s (~100ms) land well short of a 1000ms duration.
        val samples = mutableListOf<Float>()

        repeat(6) {
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            samples += ui.animateFloatTween(id = "slider-thumb", target = 100f, initial = 0f, durationMs = durationMs, easing = LinearEasing)
            ui.endFrame()
        }

        for (i in 1 until samples.size) {
            assertTrue(samples[i] > samples[i - 1], "linear tween must strictly progress every frame while mid-flight")
        }
        assertTrue(samples.last() < 100f, "6 frames (~100ms) into a 1000ms tween should still be mid-flight, not complete")
    }

    @Test
    fun uiContextAnimateFloatTweenCompletesAtTargetOnceDurationElapses() {
        val ui = UiContext()

        repeat(30) {
            // 30 frames * 1/60s = 500ms, comfortably past a 300ms duration.
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.animateFloatTween(id = "panel-slide", target = 50f, initial = 0f, durationMs = 300f, easing = LinearEasing)
            ui.endFrame()
        }

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val final = ui.animateFloatTween(id = "panel-slide", target = 50f, initial = 0f, durationMs = 300f, easing = LinearEasing)
        assertEquals(50f, final)
    }

    @Test
    fun uiContextAnimateFloatTweenRetargetsFromCurrentValueNotInitial() {
        val ui = UiContext()

        repeat(15) {
            // 15 frames * 1/60s = 250ms into a 300ms tween toward 100 -- clearly mid-flight.
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.animateFloatTween(id = "retarget-demo", target = 100f, initial = 0f, durationMs = 300f, easing = LinearEasing)
            ui.endFrame()
        }

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val valueBeforeRetarget = ui.animateFloatTween(id = "retarget-demo", target = 100f, initial = 0f, durationMs = 300f, easing = LinearEasing)

        // Retarget mid-flight: the very next frame must continue from valueBeforeRetarget, not
        // snap back toward `initial` (0f).
        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val firstFrameAfterRetarget =
            ui.animateFloatTween(id = "retarget-demo", target = 0f, initial = 0f, durationMs = 300f, easing = LinearEasing)

        assertTrue(
            firstFrameAfterRetarget < valueBeforeRetarget,
            "retargeting toward 0 should move down from the current value",
        )
        assertTrue(
            firstFrameAfterRetarget > 0f,
            "retargeting must start easing from the current animated value, not jump straight to 0",
        )
    }

    @Test
    fun repeatModeRestartSnapsCleanlyAtCycleBoundaries() {
        val durationMs = 300f
        val justBeforeBoundary = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs - 1f,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = Int.MAX_VALUE,
        )
        val atBoundary = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = Int.MAX_VALUE,
        )

        assertTrue(justBeforeBoundary > 9f, "just before the boundary the cycle should be nearly complete")
        assertEquals(0f, atBoundary, "Restart must jump straight back to the start value at a cycle boundary")
    }

    @Test
    fun repeatModeReverseIsSmoothAcrossACycleBoundary() {
        val durationMs = 300f
        val epsilon = 0.5f
        val justBefore = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs - epsilon,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Reverse,
            iterations = Int.MAX_VALUE,
        )
        val justAfter = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs + epsilon,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Reverse,
            iterations = Int.MAX_VALUE,
        )

        assertTrue(
            kotlin.math.abs(justAfter - justBefore) < 0.1f,
            "Reverse must not jump at a cycle boundary -- sampled just before ($justBefore) and just " +
                "after ($justAfter) the boundary should be nearly identical",
        )
    }

    @Test
    fun finiteIterationsStopAndHoldAtTheFinalValue() {
        val durationMs = 300f
        val midSecondCycle = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs * 1.5f,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = 2,
        )
        val wayPastExhaustion = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs * 50f,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = 2,
        )

        assertEquals(5f, midSecondCycle, "with 2 iterations, the second cycle should still play toward target, not already be held")
        assertEquals(10f, wayPastExhaustion, "once iterations are exhausted the value must hold at target, not wrap into a phantom cycle")
    }

    @Test
    fun infiniteRepeatKeepsAdvancingAcrossSeveralCycles() {
        val durationMs = 300f
        val midCycle0 = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs * 0.5f,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = Int.MAX_VALUE,
        )
        val midCycle5 = animateFloatRepeatableStep(
            startValue = 0f,
            target = 10f,
            elapsedMs = durationMs * 5.5f,
            durationMs = durationMs,
            easing = LinearEasing,
            repeatMode = RepeatMode.Restart,
            iterations = Int.MAX_VALUE,
        )

        assertEquals(5f, midCycle0, absoluteTolerance = 1e-3f)
        assertEquals(5f, midCycle5, absoluteTolerance = 1e-3f)
    }

    @Test
    fun shimmerSweepPhaseIsAOneDirectionalLoopNotAPingPongBounce() {
        // Regression for a live screenshot report: BasicText.kt's shimmer used to manually flip a
        // `shimmerForward` boolean on a spring-style animateFloat, producing a literal 0->1->0->1
        // ping-pong bounce instead of a continuous sweep. It now drives the exact same
        // animateFloatRepeatable(durationMs = 1200f, repeatMode = RepeatMode.Restart) call BasicText
        // uses for its shimmer phase; sample real frames across more than two full cycles and prove
        // the sequence only ever goes forward, snapping cleanly back to ~0 at cycle boundaries
        // instead of gradually descending back down like a bounce would.
        val ui = UiContext()
        val durationMs = 1200f
        val frameDeltaSeconds = 1f / 60f
        val totalFrames = ((durationMs / 1000f) * 60f * 2.5f).toInt() // ~2.5 cycles worth of frames

        val samples = mutableListOf<Float>()
        repeat(totalFrames) {
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = frameDeltaSeconds)
            samples += ui.animateFloatRepeatable(
                id = "shimmer-probe",
                initialValue = 0f,
                targetValue = 1f,
                durationMs = durationMs,
                repeatMode = RepeatMode.Restart,
            )
            ui.endFrame()
        }

        var resetCount = 0
        for (i in 1 until samples.size) {
            val prev = samples[i - 1]
            val next = samples[i]
            if (next < prev) {
                // Only legal decrease is a clean cycle-boundary snap: was nearly at the end,
                // instantly back near the start -- never a gradual reverse-direction descent.
                assertTrue(prev > 0.9f, "a decrease must only happen right at a completed cycle, prev was $prev")
                assertTrue(next < 0.1f, "a cycle reset must snap straight back near 0, not partway down, was $next")
                resetCount++
            }
        }

        assertTrue(resetCount >= 2, "expected at least two clean forward-restart boundaries across ~2.5 cycles, saw $resetCount")
    }

    @Test
    fun uiContextAnimateFloatRepeatableAdvancesTheAccumulatorOncePerFrame() {
        val ui = UiContext()
        val durationMs = 300f

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val first = ui.animateFloatRepeatable(
            id = "pulse",
            initialValue = 0f,
            targetValue = 10f,
            durationMs = durationMs,
            easing = LinearEasing,
        )

        ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        val second = ui.animateFloatRepeatable(
            id = "pulse",
            initialValue = 0f,
            targetValue = 10f,
            durationMs = durationMs,
            easing = LinearEasing,
        )

        assertTrue(second > first, "each real frame must advance the underlying elapsed-time accumulator")
    }
}
