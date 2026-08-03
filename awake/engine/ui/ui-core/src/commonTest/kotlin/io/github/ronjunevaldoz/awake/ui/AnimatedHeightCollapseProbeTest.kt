// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
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
                expanded = expanded
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
        // Exponential ease-to-target (see animateFloatStep) never guarantees hitting 0 in a
        // fixed frame count, only asymptotically approaching it -- 30 frames at the default
        // responsiveness=12f already gets well under 1px, which is the real thing that matters.
        check(heights.last() < 1f) { "collapse did not converge toward 0 within 30 frames: $heights" }
    }

    @Test
    fun collapseConvergesQuicklyAtShadcnCollapsibleRealResponsiveness() {
        // Regression for the live "extra space lingers, then suddenly clears" report:
        // animatedHeight's old default snapDistance (0.001px, a fixed absolute epsilon) meant
        // convergence time scaled with ln(startHeight), so shadcnCollapsible's real
        // responsiveness=8f + a real 120px content height took ~70 frames (~1.2s) to cross the
        // render gate (animatedHeight > 0.01f) even though content itself was fully clipped
        // away and invisible after ~10 frames. This asserts the fixed slot returns null (fully
        // collapsed, matching the container's real render-gate check) well before that.
        val ui = UiContext()
        var expanded = true

        fun frame(): UiBounds? {
            ui.beginFrame(400f, 800f, testSnapshot())
            val slot = ui.createColumn(x = 0f, y = 0f, width = 300f).animatedHeight(
                id = "shadcn-collapsible-probe",
                expanded = expanded,
                responsiveness = 8f // shadcnCollapsible's real value, not animatedHeight's 12f default.
            ) {
                spacer(Modifier.height(120f.dp))
            }
            ui.endFrame()
            return slot
        }

        repeat(60) { frame() }
        expanded = false

        var framesToFullyCollapse = -1
        repeat(50) { i ->
            val slot = frame()
            if (framesToFullyCollapse < 0 && slot == null) {
                framesToFullyCollapse = i
            }
        }

        check(framesToFullyCollapse in 0..40) {
            "expected shadcnCollapsible's real 120px/responsiveness=8f collapse to fully clip " +
                "within 40 frames (was ~70 before the snapDistance fix), took $framesToFullyCollapse"
        }
    }
}
