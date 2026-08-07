// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.toPx

/** Draws a [color] outline of [width] around an already-claimed [slot] as four thin
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad] strips (top/right/bottom/left).
 * Every strip is pixel-snapped the same way [BasicText.kt]'s glyph emission already is -- a
 * sub-pixel border position/thickness reads as a soft/antialiased edge next to crisp text. */
fun UiScope.border(
    slot: UiBounds,
    width: Dp = 1f.dp,
    color: Color? = null,
    overlay: Boolean = false,
    tokenId: String? = null,
) {
    val strokeColor = color ?: context.currentTheme.colors.border
    val w = width.toPx()
    if (w <= 0f) return
    val x = pixelPerfectPixel(slot.x)
    val y = pixelPerfectPixel(slot.y)
    val snappedWidth = pixelPerfectPixel(slot.width).coerceAtLeast(1f)
    val snappedHeight = pixelPerfectPixel(slot.height).coerceAtLeast(1f)
    val strokeWidth = pixelPerfectPixel(w).coerceAtLeast(1f)
    emitPrimitive(UiDrawPrimitive.Quad(x, y, snappedWidth, strokeWidth, strokeColor, tokenId = tokenId), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(x, y + snappedHeight - strokeWidth, snappedWidth, strokeWidth, strokeColor, tokenId = tokenId),
        overlay,
    )
    emitPrimitive(UiDrawPrimitive.Quad(x, y, strokeWidth, snappedHeight, strokeColor, tokenId = tokenId), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(x + snappedWidth - strokeWidth, y, strokeWidth, snappedHeight, strokeColor, tokenId = tokenId),
        overlay,
    )
}
