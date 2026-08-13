// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Real correctness coverage for [animatedVisibility]'s exit lifecycle -- this is genuinely new
 * behavior (content survives past the frame [visible] flips false, unlike a bare
 * `if (visible) content()`), not just a perf tweak, so it needs more than a
 * baseline-unchanged confirmation.
 */
class UiAnimatedVisibilityTest {

    private fun UiPrimitiveScope.emitMarkerQuad() {
        emit(UiDrawPrimitive.Quad(x = 0f, y = 0f, w = 10f, h = 10f, color = Color.White))
    }

    private fun UiContext.frame(visible: Boolean, durationMs: Float = 100f): List<UiDrawPrimitive> {
        beginFrame(320f, 200f, testSnapshot(), deltaSeconds = 1f / 60f)
        createAbsolute(x = 0f, y = 0f).animatedVisibility(id = "panel", visible = visible, durationMs = durationMs) {
            emitMarkerQuad()
        }
        return finishFrame().primitives
    }

    @Test
    fun contentIsUnmountedInstantlyWhenNeverShown() {
        val ui = UiContext()
        val primitives = ui.frame(visible = false)
        assertTrue(primitives.isEmpty(), "content that was never made visible must not render at all")
    }

    @Test
    fun contentRendersFullyOpaqueWhileVisible() {
        val ui = UiContext()
        ui.frame(visible = true) // seed initial=1f
        val primitives = ui.frame(visible = true)

        val quad = primitives.filterIsInstance<UiDrawPrimitive.Quad>().single()
        assertEquals(1f, quad.color.a, "fully visible content must not be dimmed")
    }

    @Test
    fun contentKeepsRenderingDuringExitFadeInsteadOfSnappingAway() {
        val ui = UiContext()
        ui.frame(visible = true) // establish alpha = 1f while visible

        // First frame after visible flips false: a bare `if (visible) content()` would already
        // have zero primitives here. The real fade must still be rendering, dimmed.
        val firstExitFrame = ui.frame(visible = false, durationMs = 300f)
        val quad = firstExitFrame.filterIsInstance<UiDrawPrimitive.Quad>().singleOrNull()
        assertTrue(quad != null, "content must keep rendering into the first frame of the exit fade")
        assertTrue(quad!!.color.a < 1f, "the exit fade must actually be dimming the content, not holding it opaque")
        assertTrue(quad.color.a > 0f, "the exit fade must not have already snapped to fully transparent")
    }

    @Test
    fun exitAnimationReallyStopsRenderingOnceItSettles() {
        val ui = UiContext()
        ui.frame(visible = true)

        // 40 frames * 1/60s ~= 667ms, comfortably past a 100ms exit duration.
        var lastFramePrimitives: List<UiDrawPrimitive> = emptyList()
        repeat(40) {
            lastFramePrimitives = ui.frame(visible = false, durationMs = 100f)
        }

        assertTrue(
            lastFramePrimitives.isEmpty(),
            "once the exit tween has settled at zero alpha, content must really stop being emitted",
        )
    }

    @Test
    fun retriggeringVisibilityMidFadeResumesSmoothlyFromCurrentAlpha() {
        val ui = UiContext()
        ui.frame(visible = true)

        // Nudge partway into an exit fade, then flip back to visible before it completes.
        repeat(3) { ui.frame(visible = false, durationMs = 300f) }
        val midFadeAlpha = ui.frame(visible = false, durationMs = 300f)
            .filterIsInstance<UiDrawPrimitive.Quad>().single().color.a
        assertTrue(midFadeAlpha in 0f..1f && midFadeAlpha < 1f, "sanity: should be genuinely mid-fade here")

        val resumedAlpha = ui.frame(visible = true, durationMs = 300f)
            .filterIsInstance<UiDrawPrimitive.Quad>().single().color.a

        assertTrue(
            resumedAlpha > midFadeAlpha,
            "re-triggering visible=true mid-fade must resume climbing back toward opaque from the " +
                "current alpha, not jump or restart from zero",
        )
        assertFalse(resumedAlpha == 0f, "resuming visibility must not have snapped back to invisible")
    }
}
