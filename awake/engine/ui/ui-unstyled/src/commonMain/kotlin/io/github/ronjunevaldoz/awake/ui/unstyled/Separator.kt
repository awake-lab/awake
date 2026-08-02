// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*

/** Axis a [separator] draws along -- [Horizontal] (the default) fills width and is
 * [thickness] tall; [Vertical] fills height and is [thickness] wide. */
enum class SeparatorOrientation { Horizontal, Vertical }

fun UiScope.separator(
    thickness: Dp = 1f.dp,
    modifier: UiModifier = Modifier,
    color: Color = context.currentTheme.colors.border,
    orientation: SeparatorOrientation = SeparatorOrientation.Horizontal
): UiBounds {
    val fallback = when (orientation) {
        SeparatorOrientation.Horizontal -> Dimension.FillMax to Dimension.Fixed(thickness)
        SeparatorOrientation.Vertical -> Dimension.Fixed(thickness) to Dimension.FillMax
    }
    val slot = claimModifiedSlot(modifier.withSizeFallback(fallback.first, fallback.second))
    when (orientation) {
        SeparatorOrientation.Horizontal -> {
            val lineHeight = thickness.toPx().coerceAtLeast(1f)
            val lineY = slot.y + (slot.height - lineHeight) / 2f
            emit(UiDrawPrimitive.Quad(slot.x, lineY, slot.width, lineHeight, color))
        }
        SeparatorOrientation.Vertical -> {
            val lineWidth = thickness.toPx().coerceAtLeast(1f)
            val lineX = slot.x + (slot.width - lineWidth) / 2f
            emit(UiDrawPrimitive.Quad(lineX, slot.y, lineWidth, slot.height, color))
        }
    }
    return slot.toBounds()
}
