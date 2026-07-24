// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.toPx

fun UiScope.separator(
    width: Dimension = Dimension.FillMax,
    thickness: Dp = 1f.dp,
    modifier: UiModifier = Modifier,
    color: Color = context.currentTheme.tokens.border
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
