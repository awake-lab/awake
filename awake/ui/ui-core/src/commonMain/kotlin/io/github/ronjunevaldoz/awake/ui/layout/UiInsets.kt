// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.toPx

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
