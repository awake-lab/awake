// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import kotlin.test.Test
import kotlin.test.assertEquals

/** Mirrors `CheckboxTest`'s frame-by-frame simulation, but drags rather than clicks --
 * `slider`'s value tracks the pointer while `isActive(id) && pointerDown()`, so a "click"
 * doesn't apply here; this presses inside the slider, then moves the still-down pointer, then
 * releases, proving the new `enabled` param suppresses the drag entirely (not just its
 * default-true no-op case). */
class SliderEnabledTest {

    // Slider's own slot is offset(20dp,20dp) sized width(160px) x height(32px): x in [20,180].
    private fun UiContext.dragToRightEdge(enabled: Boolean): Float {
        val input = Input()
        var value = 0f
        simulateFrame(pointerDown = true, x = 20f, y = 36f, input = input) {
            value = createAbsolute(x = 20f, y = 20f)
                .slider(
                    "sl",
                    min = 0f,
                    max = 100f,
                    value = value,
                    modifier = Modifier.width(160f.px).height(32f.px),
                    enabled = enabled,
                )
        }
        simulateFrame(pointerDown = true, x = 180f, y = 36f, input = input) {
            value = createAbsolute(x = 20f, y = 20f)
                .slider(
                    "sl",
                    min = 0f,
                    max = 100f,
                    value = value,
                    modifier = Modifier.width(160f.px).height(32f.px),
                    enabled = enabled,
                )
        }
        simulateFrame(pointerDown = false, x = 180f, y = 36f, input = input) {
            value = createAbsolute(x = 20f, y = 20f)
                .slider(
                    "sl",
                    min = 0f,
                    max = 100f,
                    value = value,
                    modifier = Modifier.width(160f.px).height(32f.px),
                    enabled = enabled,
                )
        }
        return value
    }

    @Test
    fun dragMovesValueWhenEnabled() {
        val ui = UiContext()
        val value = ui.dragToRightEdge(enabled = true)
        assertEquals(
            100f,
            value,
            "dragging from the left edge to the right edge must reach max when enabled (the default)",
        )
    }

    @Test
    fun dragDoesNotMoveValueWhenDisabled() {
        val ui = UiContext()
        val value = ui.dragToRightEdge(enabled = false)
        assertEquals(
            0f,
            value,
            "enabled = false must suppress the drag even on an otherwise valid press+move+release",
        )
    }
}
