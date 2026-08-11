// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import kotlin.test.Test
import kotlin.test.assertTrue

class ShimmerPrimitivesTest {

    /** Same proof shape as `UiAnimationTest.shimmerSweepPhaseIsAOneDirectionalLoopNotAPingPongBounce`
     * (commit c2db6ec9), driven through the new widget-agnostic [shimmerBand] instead of a raw
     * `animateFloatRepeatable` call -- proves the decoupled primitive still produces a clean
     * one-directional sweep (0 -> 1, snap back to 0, repeat), not a ping-pong bounce. */
    @Test
    fun shimmerBandPhaseIsAOneDirectionalLoopNotAPingPongBounce() {
        val ui = UiContext()
        val durationMs = 1200f
        val frameDeltaSeconds = 1f / 60f
        val totalFrames = ((durationMs / 1000f) * 60f * 2.5f).toInt() // ~2.5 cycles worth of frames
        val slot = UiBounds(x = 0f, y = 0f, width = 200f, height = 40f)

        val samples = mutableListOf<Float>()
        repeat(totalFrames) {
            ui.beginFrame(320f, 200f, testSnapshot(), deltaSeconds = frameDeltaSeconds)
            val scope = ui.createAbsolute(x = 0f, y = 0f)
            samples += scope.shimmerBand(id = "shimmer-band-probe", slot = slot, durationMs = durationMs).phase
            ui.endFrame()
        }

        var resetCount = 0
        for (i in 1 until samples.size) {
            val prev = samples[i - 1]
            val next = samples[i]
            if (next < prev) {
                assertTrue(prev > 0.9f, "a decrease must only happen right at a completed cycle, prev was $prev")
                assertTrue(next < 0.1f, "a cycle reset must snap straight back near 0, not partway down, was $next")
                resetCount++
            }
        }

        assertTrue(resetCount >= 2, "expected at least two clean forward-restart boundaries across ~2.5 cycles, saw $resetCount")
    }

    @Test
    fun shimmerBandWidthIsAtLeastTheFloorAndSweepsAcrossTheSlot() {
        val ui = UiContext()
        ui.beginFrame(320f, 200f, testSnapshot())
        val scope = ui.createAbsolute(x = 0f, y = 0f)
        val slot = UiBounds(x = 10f, y = 0f, width = 40f, height = 20f) // narrower than the 160px floor
        val band = scope.shimmerBand(id = "shimmer-band-width-probe", slot = slot)
        ui.endFrame()

        assertTrue(band.width >= 160f, "band width must respect the 160px floor even for a narrow slot, was ${band.width}")
        // At phase 0f, the band starts fully to the left of the slot.
        assertTrue(band.x <= slot.x, "band must start left of the slot at phase 0, x=${band.x} slot.x=${slot.x}")
    }
}
