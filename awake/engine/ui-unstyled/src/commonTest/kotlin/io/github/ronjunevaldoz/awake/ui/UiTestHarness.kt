// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.context.UiContext

/** Builds a one-off [UiInputState] for a test frame. */
fun testSnapshot(x: Float = -100f, y: Float = -100f, down: Boolean = false, scrollDeltaY: Float = 0f): UiInputState {
    val input = Input()
    input.setPointer(down, x, y)
    input.scrollDeltaY = scrollDeltaY
    return input.updateSnapshot().toUiInputState()
}

/**
 * One simulated render frame: moves the pointer, runs [beginFrame]/[endFrame] around
 * [widgetCalls].
 */
fun UiContext.simulateFrame(
    pointerDown: Boolean,
    x: Float,
    y: Float,
    screenWidth: Float = 200f,
    screenHeight: Float = 200f,
    input: Input = Input(),
    widgetCalls: () -> Unit
) {
    input.setPointer(down = pointerDown, x = x, y = y)
    beginFrame(screenWidth, screenHeight, input.updateSnapshot().toUiInputState())
    widgetCalls()
    endFrame()
}

fun UiContext.simulateScrollFrame(
    x: Float,
    y: Float,
    scrollDeltaY: Float,
    screenWidth: Float = 200f,
    screenHeight: Float = 200f,
    input: Input = Input(),
    widgetCalls: () -> Unit
) {
    input.scrollDeltaY = scrollDeltaY
    simulateFrame(
        pointerDown = false,
        x = x,
        y = y,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        input = input,
        widgetCalls = widgetCalls
    )
}

/**
 * The two-frame press-then-release-while-still-hovered sequence.
 */
fun UiContext.simulateClick(
    x: Float,
    y: Float,
    screenWidth: Float = 200f,
    screenHeight: Float = 200f,
    input: Input = Input(),
    widgetCalls: () -> Unit
) {
    simulateFrame(pointerDown = true, x = x, y = y, screenWidth = screenWidth, screenHeight = screenHeight, input = input, widgetCalls = widgetCalls)
    simulateFrame(pointerDown = false, x = x, y = y, screenWidth = screenWidth, screenHeight = screenHeight, input = input, widgetCalls = widgetCalls)
}
