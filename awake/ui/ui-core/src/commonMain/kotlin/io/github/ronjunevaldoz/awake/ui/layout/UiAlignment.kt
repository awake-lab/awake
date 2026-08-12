// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets

fun UiBounds.place(
    width: Float,
    height: Float,
    alignment: UiAlignment = UiAlignment.TopStart,
    insets: UiInsets = UiInsets.Zero,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
): UiBounds {
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
    return UiBounds(x, y, resolvedWidth, resolvedHeight)
}
