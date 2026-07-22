package io.github.ronjunevaldoz.awake.ui.graphics.internal

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.toPx

internal fun UiScope.gradientBorder(
    slot: UiSlot,
    width: Dp = 1f.dp,
    gradient: UiLinearGradient,
    overlay: Boolean = false
) {
    val borderPx = width.toPx()
    if (borderPx <= 0f) return
    gradientRect(UiSlot(slot.x, slot.y, slot.width, borderPx), gradient, overlay)
    gradientRect(
        UiSlot(slot.x, slot.y + slot.height - borderPx, slot.width, borderPx),
        gradient,
        overlay
    )
    gradientRect(UiSlot(slot.x, slot.y, borderPx, slot.height), gradient, overlay)
    gradientRect(
        UiSlot(slot.x + slot.width - borderPx, slot.y, borderPx, slot.height),
        gradient,
        overlay
    )
}
