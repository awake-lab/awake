// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScrollContainersTest {

    @Test
    fun hoveredScrollConsumesDeltaAndMovesState() {
        val ui = UiContext()
        val state = UiScrollState()
        val scope = ui.absolute(0f, 0f, font = UiFonts.default())

        ui.simulateScrollFrame(x = 20f, y = 20f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                width = 140f.toDimension(),
                height = 80f.toDimension(),
                state = state,
                scrollSpeed = 24f
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row)
                }
            }
        }

        assertEquals(24f, state.offsetY)
        assertEquals(0f, Input.scrollDeltaY)
        assertTrue(state.canScroll)
    }

    @Test
    fun nonHoveredScrollLeavesDeltaForOtherConsumers() {
        val ui = UiContext()
        val state = UiScrollState()
        val scope = ui.absolute(0f, 0f, font = UiFonts.default())

        ui.simulateScrollFrame(x = 180f, y = 180f, scrollDeltaY = -1f) {
            scope.scrollPanel(
                id = "scroll",
                width = 140f.toDimension(),
                height = 80f.toDimension(),
                state = state,
                scrollSpeed = 24f
            ) {
                repeat(8) { index ->
                    val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                    text("Row $index", slot = row)
                }
            }
        }

        assertEquals(0f, state.offsetY)
        assertEquals(-1f, Input.scrollDeltaY)
    }

    @Test
    fun scrollPanelEmitsViewportClipAndScrollbarThumb() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        Input.scrollDeltaY = 0f
        val ui = UiContext()
        ui.beginFrame(220f, 200f)
        val scope = ui.absolute(0f, 0f, font = UiFonts.default())

        val result = scope.scrollPanel(
            id = "scroll",
            width = 140f.toDimension(),
            height = 80f.toDimension(),
            state = UiScrollState()
        ) {
            repeat(8) { index ->
                val row = claimSlot(Dimension.FillMax, 20f.toDimension())
                text("Row $index", slot = row)
            }
        }
        val primitives = ui.endFrame()

        assertNotNull(result.thumb)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.ClipPush>().any { it.rect == result.viewport })
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.ClipPop>().isNotEmpty())
        assertTrue(
            primitives.any { primitive ->
                when (primitive) {
                    is UiDrawPrimitive.Quad -> primitive.x == result.thumb.thumb.x && primitive.y == result.thumb.thumb.y
                    is UiDrawPrimitive.RoundedQuad -> primitive.x == result.thumb.thumb.x && primitive.y == result.thumb.thumb.y
                    else -> false
                }
            }
        )
    }
}
