// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import kotlin.test.Test

/**
 * Throwaway probe driving the real [animatedHeight] (as used by `shadcnCollapsible`) through an
 * expand -> settle -> collapse sequence, sampling the animated slot height every frame to look
 * for a discontinuity/overshoot on the collapse path after the d67e23de perf fix gated
 * remeasurement on the false->true transition only.
 */
class AnimatedHeightCollapseProbeTest {

    @Test
    fun probeCollapseSequenceIsMonotonicWithNoJump() {
        val ui = UiContext()
        var expanded = true

        fun frame(): Float {
            ui.beginFrame(400f, 800f, testSnapshot())
            val slot = ui.createColumn(x = 0f, y = 0f, width = 300f).animatedHeight(
                id = "probe",
                expanded = expanded,
            ) {
                spacer(Modifier.height(120f.dp))
            }
            ui.endFrame()
            return slot?.height ?: 0f
        }

        // Expand and let it fully settle at the real measured height (120).
        var settled = 0f
        repeat(60) { settled = frame() }
        check(settled > 100f) { "expected settled height near 120, got $settled" }

        // Now collapse and sample every frame.
        expanded = false
        val heights = mutableListOf(settled)
        repeat(30) { heights.add(frame()) }

        println("collapse sequence: $heights")

        var prev = heights.first()
        for ((i, h) in heights.withIndex()) {
            check(h <= prev + 0.01f) {
                "height INCREASED at frame $i (looks like the reported 'jump up'): $prev -> $h in $heights"
            }
            prev = h
        }
        // animateFloatTween (see below) reaches the EXACT target once elapsed >= durationMs,
        // not just asymptotically close -- default durationMs=250ms is well under 30 frames
        // (~500ms) at a real 60fps delta, so this should already be sitting at exactly 0.
        check(heights.last() == 0f) { "collapse did not reach exactly 0 within 30 frames: $heights" }
    }

    @Test
    fun collapseConvergesWithinItsOwnFixedDuration() {
        // Regression for two live reports on the OLD animateFloat-based implementation: (1)
        // "extra space lingers, then suddenly clears" -- exponential decay chasing a target
        // forever, gated behind an epsilon "snapDistance" to decide when to call it done, meant
        // content looked fully collapsed within ~10 frames while the container kept an
        // imperceptible sliver of leftover height for dozens more frames, then hard-snapped to
        // exactly 0 once it finally crossed the epsilon; (2) "the parent didn't have same
        // animation" -- a wrap-sized parent (e.g. a card) re-measures this child every frame, so
        // that epsilon-vs-true-zero mismatch meant the parent's own shrink desynced from the
        // child's right at the very end. animateFloatTween has neither problem: it reaches the
        // EXACT target in a known, bounded number of frames (no epsilon), so this asserts the
        // slot becomes null (fully collapsed) at or right after that exact duration, not "well
        // before" some old worst-case bound.
        val ui = UiContext()
        var expanded = true
        val durationMs = 250f
        val frameDeltaMs = 1000f / 60f
        val expectedFrames = kotlin.math.ceil(durationMs / frameDeltaMs).toInt()

        fun frame(): UiBounds? {
            ui.beginFrame(400f, 800f, testSnapshot())
            val slot = ui.createColumn(x = 0f, y = 0f, width = 300f).animatedHeight(
                id = "shadcn-collapsible-probe",
                expanded = expanded,
            ) {
                spacer(Modifier.height(120f.dp))
            }
            ui.endFrame()
            return slot
        }

        repeat(60) { frame() }
        expanded = false

        var framesToFullyCollapse = -1
        repeat(expectedFrames + 5) { i ->
            val slot = frame()
            if (framesToFullyCollapse < 0 && slot == null) {
                framesToFullyCollapse = i
            }
        }

        check(framesToFullyCollapse in (expectedFrames - 2)..(expectedFrames + 2)) {
            "expected collapse to fully clip within ~$expectedFrames frames (durationMs=$durationMs " +
                "at a 60fps delta), took $framesToFullyCollapse"
        }
    }
}
