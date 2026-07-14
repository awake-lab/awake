// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

internal val TransparentColor: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)

/** Draws a [color] outline of [width] around an already-claimed [slot] as four thin
 * [UiDrawPrimitive.Quad] strips (top/right/bottom/left). */
fun UiScope.border(slot: UiSlot, width: Dp = 1f.dp, color: FloatArray = theme.tokens.border) {
    val w = width.toPx()
    if (w <= 0f) return
    emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, w, color))
    emit(UiDrawPrimitive.Quad(slot.x, slot.y + slot.height - w, slot.width, w, color))
    emit(UiDrawPrimitive.Quad(slot.x, slot.y, w, slot.height, color))
    emit(UiDrawPrimitive.Quad(slot.x + slot.width - w, slot.y, w, slot.height, color))
}

/** Fill + border for one widget slot, sharing the corner radius correctly between the two. */
fun UiScope.emitFillAndBorder(slot: UiSlot, fillColor: FloatArray, radiusPx: Float, borderWidth: Dp, borderColor: FloatArray) {
    val hasFill = fillColor[3] > 0f
    val borderPx = borderWidth.toPx()
    if (radiusPx > 0f && borderPx > 0f) {
        emit(UiDrawPrimitive.RoundedQuad(slot.x, slot.y, slot.width, slot.height, borderColor, radiusPx))
        if (hasFill) {
            val innerRadius = (radiusPx - borderPx).coerceAtLeast(0f)
            emit(
                UiDrawPrimitive.RoundedQuad(
                    slot.x + borderPx,
                    slot.y + borderPx,
                    slot.width - 2 * borderPx,
                    slot.height - 2 * borderPx,
                    fillColor,
                    innerRadius
                )
            )
        }
        return
    }
    if (hasFill) {
        val primitive = if (radiusPx > 0f) {
            UiDrawPrimitive.RoundedQuad(slot.x, slot.y, slot.width, slot.height, fillColor, radiusPx)
        } else {
            UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, fillColor)
        }
        emit(primitive)
    }
    if (borderPx > 0f) border(slot, borderWidth, borderColor)
}

internal fun UiScope.emitInsetAccent(slot: UiSlot, inset: Float, radiusPx: Float) {
    val x = slot.x + inset
    val y = slot.y + inset
    val w = slot.width - inset * 2
    val h = slot.height - inset * 2
    val primitive = if (radiusPx > 0f) {
        UiDrawPrimitive.RoundedQuad(x, y, w, h, theme.tokens.accent, (radiusPx - inset).coerceAtLeast(0f))
    } else {
        UiDrawPrimitive.Quad(x, y, w, h, theme.tokens.accent)
    }
    emit(primitive)
}
