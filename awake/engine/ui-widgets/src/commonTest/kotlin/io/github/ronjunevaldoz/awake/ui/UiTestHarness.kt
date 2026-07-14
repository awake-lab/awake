// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input

/**
 * One simulated render frame: moves the pointer, runs [beginFrame]/[endFrame] around
 * [widgetCalls], which does whatever widget calls the test needs at this pointer state.
 * Every existing `UiContext` test hand-rolled this exact
 * `Input.setPointer(...); beginFrame(...); <widget calls>; endFrame()` sequence -- pulled out
 * here so a new test states *what pointer state* it wants, not the frame plumbing.
 */
fun UiContext.simulateFrame(
    pointerDown: Boolean,
    x: Float,
    y: Float,
    screenWidth: Float = 200f,
    screenHeight: Float = 200f,
    widgetCalls: () -> Unit
) {
    Input.setPointer(down = pointerDown, x = x, y = y)
    beginFrame(screenWidth, screenHeight)
    widgetCalls()
    endFrame()
}

/**
 * The two-frame press-then-release-while-still-hovered sequence every widget in this
 * library (`buttonSlot`'s `tryClaimActive`/`releaseActiveIfMatches` pair) keys a "click" off
 * of -- see [simulateFrame]'s doc comment for what this replaces. [widgetCalls] runs once per
 * frame (press, then release), same as calling a widget function twice in a real click.
 */
fun UiContext.simulateClick(
    x: Float,
    y: Float,
    screenWidth: Float = 200f,
    screenHeight: Float = 200f,
    widgetCalls: () -> Unit
) {
    simulateFrame(pointerDown = true, x = x, y = y, screenWidth = screenWidth, screenHeight = screenHeight, widgetCalls = widgetCalls)
    simulateFrame(pointerDown = false, x = x, y = y, screenWidth = screenWidth, screenHeight = screenHeight, widgetCalls = widgetCalls)
}
