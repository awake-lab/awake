// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.pixelPerfectTextScale
import io.github.ronjunevaldoz.awake.ui.toPx
//
//enum class UiAnchor {
//    TopLeft,
//    TopRight,
//    BottomLeft,
//    BottomRight
//}
//
//fun UiSlot.anchored(
//    anchor: UiAnchor,
//    width: Float,
//    height: Float,
//    margin: UiInsets = UiInsets.Zero
//): UiSlot {
//    val resolvedWidth = width.coerceAtLeast(0f)
//    val resolvedHeight = height.coerceAtLeast(0f)
//    val anchoredX = when (anchor) {
//        UiAnchor.TopLeft, UiAnchor.BottomLeft -> x + margin.start.toPx()
//        UiAnchor.TopRight, UiAnchor.BottomRight -> x + this.width - resolvedWidth - margin.end.toPx()
//    }
//    val anchoredY = when (anchor) {
//        UiAnchor.TopLeft, UiAnchor.TopRight -> y + margin.top.toPx()
//        UiAnchor.BottomLeft, UiAnchor.BottomRight -> y + this.height - resolvedHeight - margin.bottom.toPx()
//    }
//    return UiSlot(
//        x = anchoredX,
//        y = anchoredY,
//        width = resolvedWidth,
//        height = resolvedHeight
//    )
//}

fun UiFont.textBlockHeight(
    lineCount: Int,
    textScale: Float = 1f,
    gap: Float = UiSpacing.sm.toPx()
): Float {
    if (lineCount <= 0) {
        return 0f
    }
    val glyphPx = cellSize * pixelPerfectTextScale(textScale)
    return lineCount * glyphPx + (lineCount - 1) * gap
}
