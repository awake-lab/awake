// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.toPx

data class UiInsets(
    val start: Dp = UiShape.none,
    val top: Dp = UiShape.none,
    val end: Dp = UiShape.none,
    val bottom: Dp = UiShape.none,
) {
    companion object {
        val Zero = UiInsets()
    }
}

fun UiInsets(all: Dp): UiInsets = UiInsets(all, all, all, all)

fun UiInsets(horizontal: Dp, vertical: Dp): UiInsets = UiInsets(horizontal, vertical, horizontal, vertical)

fun UiInsets.horizontalPx(): Float = (start + end).toPx()

fun UiInsets.verticalPx(): Float = (top + bottom).toPx()

fun UiBounds.inset(insets: UiInsets): UiBounds {
    val startPx = insets.start.toPx()
    val topPx = insets.top.toPx()
    val endPx = insets.end.toPx()
    val bottomPx = insets.bottom.toPx()
    return UiBounds(
        x = x + startPx,
        y = y + topPx,
        width = (width - startPx - endPx).coerceAtLeast(0f),
        height = (height - topPx - bottomPx).coerceAtLeast(0f),
    )
}
