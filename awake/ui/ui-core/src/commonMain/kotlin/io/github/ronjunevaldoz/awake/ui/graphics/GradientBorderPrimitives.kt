// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.toPx

internal fun UiPrimitiveScope.gradientBorder(
    slot: UiBounds,
    width: Dp = 1f.dp,
    gradient: UiLinearGradient,
    overlay: Boolean = false,
) {
    val borderPx = width.toPx()
    if (borderPx <= 0f) return
    gradientRect(UiBounds(slot.x, slot.y, slot.width, borderPx), gradient, overlay)
    gradientRect(
        UiBounds(slot.x, slot.y + slot.height - borderPx, slot.width, borderPx),
        gradient,
        overlay,
    )
    gradientRect(UiBounds(slot.x, slot.y, borderPx, slot.height), gradient, overlay)
    gradientRect(
        UiBounds(slot.x + slot.width - borderPx, slot.y, borderPx, slot.height),
        gradient,
        overlay,
    )
}
