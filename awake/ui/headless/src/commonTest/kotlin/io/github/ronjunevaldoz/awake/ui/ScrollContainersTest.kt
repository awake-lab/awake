// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

@io.github.ronjunevaldoz.awake.testing.ui.UiLowLevelTest("Checks scroll-state consumption and emitted viewport primitives")
class ScrollContainersTest {

    @Test
    fun hoveredScrollConsumesDeltaAndMovesState() {
        val ui = UiContext()
        val state = UiScrollState()
        ui.pushLocal(LocalFont, UiFonts.default())
        val scope = ui.createAbsolute(x = 0f, y = 0f)

        ui.simulateScrollFrame(x = 20f, y = 20f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                modifier = Modifier
                    .width(140f.px)
                    .height(80f.px)
                    .verticalScroll(state, UiScrollConfig.Default.copy(scrollSpeed = 24f)),
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row)
                }
            }
        }

        assertEquals(24f, state.offsetY)
        assertTrue(
            ui.finishFrame().ownership.isScrollConsumed,
            "a hovered scrollable scroll panel must mark the frame's delta as consumed",
        )
        assertTrue(state.canScroll)
    }

    @Test
    fun nonHoveredScrollLeavesDeltaForOtherConsumers() {
        val ui = UiContext()
        val state = UiScrollState()
        ui.pushLocal(LocalFont, UiFonts.default())
        val scope = ui.createAbsolute(x = 0f, y = 0f)

        ui.simulateScrollFrame(x = 180f, y = 180f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                modifier = Modifier
                    .width(140f.px)
                    .height(80f.px)
                    .verticalScroll(state, UiScrollConfig.Default.copy(scrollSpeed = 24f)),
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row)
                }
            }
        }

        assertEquals(0f, state.offsetY)
        // Not consumed by the (non-hovered) panel above -- a scene-facing consumer (camera
        // pinch-zoom) reads UiInputResult.isScrollConsumed, not a live Input field, to decide
        // whether it's still free to use this frame's delta.
        assertFalse(
            ui.finishFrame().ownership.isScrollConsumed,
            "a non-hovered scroll panel must not mark the frame's delta as consumed",
        )
    }

    @Test
    fun scrollPanelEmitsViewportClipAndScrollbarThumb() {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 220f, viewportHeight = 200f, input = testSnapshot()))
        ui.pushLocal(LocalFont, UiFonts.default())
        val scope = ui.createAbsolute(x = 0f, y = 0f)

        val result = scope.scrollPanel(
            id = "scroll",
            modifier = Modifier
                .width(140f.px)
                .height(80f.px)
                .verticalScroll(UiScrollState()),
        ) {
            repeat(8) { index ->
                val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                text("Row $index", slot = row)
            }
        }
        val primitives = ui.finishFrame().primitives

        assertNotNull(result.verticalThumb)
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.ClipPush>()
                .any { it.rect == result.viewport },
        )
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.ClipPop>().isNotEmpty())
        assertTrue(
            primitives.any { primitive ->
                when (primitive) {
                    is UiDrawPrimitive.Quad -> primitive.x == result.verticalThumb!!.thumb.x && primitive.y == result.verticalThumb!!.thumb.y
                    is UiDrawPrimitive.RoundedQuad -> primitive.x == result.verticalThumb!!.thumb.x && primitive.y == result.verticalThumb!!.thumb.y
                    else -> false
                }
            },
        )
    }
}
