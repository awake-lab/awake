package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.dp
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
    tokenId: String? = null
) {
    val strokeColor = color ?: context.currentTheme.colors.border
    val w = width.toPx()
    if (w <= 0f) return
    val x = pixelPerfectPixel(slot.x)
    val y = pixelPerfectPixel(slot.y)
    val width_ = pixelPerfectPixel(slot.width).coerceAtLeast(1f)
    val height_ = pixelPerfectPixel(slot.height).coerceAtLeast(1f)
    val strokeWidth = pixelPerfectPixel(w).coerceAtLeast(1f)
    emitPrimitive(UiDrawPrimitive.Quad(x, y, width_, strokeWidth, strokeColor, tokenId = tokenId), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(x, y + height_ - strokeWidth, width_, strokeWidth, strokeColor, tokenId = tokenId),
        overlay
    )
    emitPrimitive(UiDrawPrimitive.Quad(x, y, strokeWidth, height_, strokeColor, tokenId = tokenId), overlay)
    emitPrimitive(
        UiDrawPrimitive.Quad(x + width_ - strokeWidth, y, strokeWidth, height_, strokeColor, tokenId = tokenId),
        overlay
    )
}
