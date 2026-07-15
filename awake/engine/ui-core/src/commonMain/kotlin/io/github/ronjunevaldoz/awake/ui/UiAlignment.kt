// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Child placement inside a parent slot. This is the Box-style alignment API; unlike
 * [UiAnchor], which is a shell-placement helper for screen corners, [UiAlignment] is about
 * how content sits INSIDE an already-claimed container.
 */
enum class UiAlignment {
    TopStart,
    TopCenter,
    TopEnd,
    CenterStart,
    Center,
    CenterEnd,
    BottomStart,
    BottomCenter,
    BottomEnd
}

fun UiSlot.place(
    width: Float,
    height: Float,
    alignment: UiAlignment = UiAlignment.TopStart,
    insets: UiInsets = UiInsets.Zero,
    offsetX: Float = 0f,
    offsetY: Float = 0f
): UiSlot {
    val content = inset(insets)
    val resolvedWidth = width.coerceIn(0f, content.width)
    val resolvedHeight = height.coerceIn(0f, content.height)
    val x = when (alignment) {
        UiAlignment.TopStart, UiAlignment.CenterStart, UiAlignment.BottomStart -> content.x
        UiAlignment.TopCenter, UiAlignment.Center, UiAlignment.BottomCenter -> content.x + (content.width - resolvedWidth) / 2f
        UiAlignment.TopEnd, UiAlignment.CenterEnd, UiAlignment.BottomEnd -> content.x + content.width - resolvedWidth
    } + offsetX
    val y = when (alignment) {
        UiAlignment.TopStart, UiAlignment.TopCenter, UiAlignment.TopEnd -> content.y
        UiAlignment.CenterStart, UiAlignment.Center, UiAlignment.CenterEnd -> content.y + (content.height - resolvedHeight) / 2f
        UiAlignment.BottomStart, UiAlignment.BottomCenter, UiAlignment.BottomEnd -> content.y + content.height - resolvedHeight
    } + offsetY
    return UiSlot(x, y, resolvedWidth, resolvedHeight)
}
