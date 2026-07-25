package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.toPx

/** Draws a [color] outline of [width] around an already-claimed [slot] as four thin
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad] strips (top/right/bottom/left). */
fun UiScope.border(
    slot: UiSlot,
    width: Dp = 1f.dp,
    color: Color? = null,
    overlay: Boolean = false
) {
    val strokeColor = color ?: context.currentTheme.tokens.border
    val w = width.toPx()
    if (w <= 0f) return
    emitPrimitive(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, w, strokeColor), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(slot.x, slot.y + slot.height - w, slot.width, w, strokeColor),
        overlay
    )
    emitPrimitive(UiDrawPrimitive.Quad(slot.x, slot.y, w, slot.height, strokeColor), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(slot.x + slot.width - w, slot.y, w, slot.height, strokeColor),
        overlay
    )
}
