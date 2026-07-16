// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

fun UiScope.separator(
    width: Dimension = Dimension.FillMax,
    thickness: Dp = 1f.dp,
    modifier: UiModifier = UiModifier(),
    color: Color = theme.tokens.border
): UiSlot {
    val slot = claimModifiedSlot(
        defaultWidth = width,
        defaultHeight = modifier.height ?: Dimension.Fixed(thickness),
        modifier = modifier
    )
    val lineHeight = thickness.toPx().coerceAtLeast(1f)
    val lineY = slot.y + (slot.height - lineHeight) / 2f
    emit(UiDrawPrimitive.Quad(slot.x, lineY, slot.width, lineHeight, color))
    return slot
}
