// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class ScrollContainersTest {

    @Test
    fun hoveredScrollConsumesDeltaAndMovesState() {
        val ui = UiContext()
        val state = UiScrollState()
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp), font = UiFonts.default())

        ui.simulateScrollFrame(x = 20f, y = 20f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                modifier = Modifier
                    .width(140f.px)
                    .height(80f.px)
                    .verticalScroll(state, UiScrollConfig.Default.copy(scrollSpeed = 24f))
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row.toBounds())
                }
            }
        }

        assertEquals(24f, state.offsetY)
        assertTrue(ui.inputResult().isScrollConsumed, "a hovered scrollable scroll panel must mark the frame's delta as consumed")
        assertTrue(state.canScroll)
    }

    @Test
    fun nonHoveredScrollLeavesDeltaForOtherConsumers() {
        val ui = UiContext()
        val state = UiScrollState()
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp), font = UiFonts.default())

        ui.simulateScrollFrame(x = 180f, y = 180f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                modifier = Modifier
                    .width(140f.px)
                    .height(80f.px)
                    .verticalScroll(state, UiScrollConfig.Default.copy(scrollSpeed = 24f))
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row.toBounds())
                }
            }
        }

        assertEquals(0f, state.offsetY)
        // Not consumed by the (non-hovered) panel above -- a scene-facing consumer (camera
        // pinch-zoom) reads UiInputResult.isScrollConsumed, not a live Input field, to decide
        // whether it's still free to use this frame's delta.
        assertFalse(ui.inputResult().isScrollConsumed, "a non-hovered scroll panel must not mark the frame's delta as consumed")
    }

    @Test
    fun scrollPanelEmitsViewportClipAndScrollbarThumb() {
        val ui = UiContext()
        ui.beginFrame(220f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp), font = UiFonts.default())

        val result = scope.scrollPanel(
                id = "scroll",
                modifier = Modifier
                .width(140f.px)
                .height(80f.px)
                .verticalScroll(UiScrollState())
        ) {
            repeat(8) { index ->
                val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                text("Row $index", slot = row.toBounds())
            }
        }
        val primitives = ui.endFrame()

        assertNotNull(result.verticalThumb)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.ClipPush>().any { it.rect == result.viewport })
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.ClipPop>().isNotEmpty())
        assertTrue(
            primitives.any { primitive ->
                when (primitive) {
                    is UiDrawPrimitive.Quad -> primitive.x == result.verticalThumb!!.thumb.x && primitive.y == result.verticalThumb!!.thumb.y
                    is UiDrawPrimitive.RoundedQuad -> primitive.x == result.verticalThumb!!.thumb.x && primitive.y == result.verticalThumb!!.thumb.y
                    else -> false
                }
            }
        )
    }
}
