// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

internal val TransparentColor: Color = Color.Transparent

/** Draws a [color] outline of [width] around an already-claimed [slot] as four thin
 * [UiDrawPrimitive.Quad] strips (top/right/bottom/left). */
fun UiScope.border(slot: UiSlot, width: Dp = 1f.dp, color: Color = theme.tokens.border, overlay: Boolean = false) {
    val w = width.toPx()
    if (w <= 0f) return
    emitPrimitive(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, w, color), overlay)
    emitPrimitive(UiDrawPrimitive.Quad(slot.x, slot.y + slot.height - w, slot.width, w, color), overlay)
    emitPrimitive(UiDrawPrimitive.Quad(slot.x, slot.y, w, slot.height, color), overlay)
    emitPrimitive(UiDrawPrimitive.Quad(slot.x + slot.width - w, slot.y, w, slot.height, color), overlay)
}

private fun UiScope.emitPrimitive(primitive: UiDrawPrimitive, overlay: Boolean) {
    if (overlay) emitOverlay(primitive) else emit(primitive)
}

private fun roundedRadiusFor(slot: UiSlot, radiusPx: Float, shapeSpec: UiShapeSpec?): Float = when (shapeSpec) {
    null -> radiusPx
    UiShapeSpec.Rectangle -> 0f
    is UiShapeSpec.RoundedRectangle -> shapeSpec.radius.toPx().coerceIn(0f, minOf(slot.width, slot.height) / 2f)
    UiShapeSpec.Pill -> minOf(slot.width, slot.height) / 2f
    UiShapeSpec.Circle -> if (slot.width == slot.height) slot.width / 2f else 0f
    is UiShapeSpec.CutCorner -> 0f
}

private fun UiScope.pathOnlyShape(slot: UiSlot, shapeSpec: UiShapeSpec?): UiShapeSpec? = when (shapeSpec) {
    null, UiShapeSpec.Rectangle, UiShapeSpec.Pill, is UiShapeSpec.RoundedRectangle -> null
    UiShapeSpec.Circle -> if (slot.width == slot.height) null else shapeSpec
    is UiShapeSpec.CutCorner -> shapeSpec
}

private fun UiScope.emitFillShape(slot: UiSlot, color: Color, radiusPx: Float, shapeSpec: UiShapeSpec?, overlay: Boolean = false) {
    if (color.isTransparent()) return
    val pathShape = pathOnlyShape(slot, shapeSpec)
    if (pathShape != null) {
        emitPrimitive(UiDrawPrimitive.FilledPath(pathShape.toPath(slot), color), overlay)
        return
    }
    val resolvedRadius = roundedRadiusFor(slot, radiusPx, shapeSpec)
    val primitive = if (resolvedRadius > 0f) {
        UiDrawPrimitive.RoundedQuad(slot.x, slot.y, slot.width, slot.height, color, resolvedRadius)
    } else {
        UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, color)
    }
    emitPrimitive(primitive, overlay)
}

fun UiScope.gradientRect(
    slot: UiSlot,
    gradient: UiLinearGradient,
    overlay: Boolean = false
) {
    emitPrimitive(
        UiDrawPrimitive.GradientQuad(
            x = slot.x,
            y = slot.y,
            w = slot.width,
            h = slot.height,
            gradient = gradient
        ),
        overlay
    )
}

fun UiScope.gradientBorder(
    slot: UiSlot,
    width: Dp = 1f.dp,
    gradient: UiLinearGradient,
    overlay: Boolean = false
) {
    val borderPx = width.toPx()
    if (borderPx <= 0f) return
    gradientRect(UiSlot(slot.x, slot.y, slot.width, borderPx), gradient, overlay)
    gradientRect(UiSlot(slot.x, slot.y + slot.height - borderPx, slot.width, borderPx), gradient, overlay)
    gradientRect(UiSlot(slot.x, slot.y, borderPx, slot.height), gradient, overlay)
    gradientRect(UiSlot(slot.x + slot.width - borderPx, slot.y, borderPx, slot.height), gradient, overlay)
}

/** Fill + border for one widget slot, sharing the corner radius correctly between the two. */
fun UiScope.emitFillAndBorder(
    slot: UiSlot,
    fillColor: Color,
    radiusPx: Float,
    borderWidth: Dp,
    borderColor: Color,
    shapeSpec: UiShapeSpec? = null,
    overlay: Boolean = false
) {
    val hasFill = !fillColor.isTransparent()
    val borderPx = borderWidth.toPx()
    val pathShape = pathOnlyShape(slot, shapeSpec)
    if (pathShape != null) {
        val path = pathShape.toPath(slot)
        if (hasFill) emitPrimitive(UiDrawPrimitive.FilledPath(path, fillColor), overlay)
        if (borderPx > 0f) emitPrimitive(UiDrawPrimitive.StrokedPath(path, UiStroke(borderWidth), borderColor), overlay)
        return
    }

    val resolvedRadius = roundedRadiusFor(slot, radiusPx, shapeSpec)
    if (resolvedRadius > 0f && borderPx > 0f) {
        if (!hasFill) {
            // The "full border-colored quad, then an inset fill-colored quad punched on
            // top" trick below assumes a fill always exists to cover the interior -- with
            // no fill (an Outline-style button: transparent background, border only), the
            // punch-out quad never gets drawn and the border-colored background quad shows
            // through solid, covering the WHOLE shape instead of reading as a thin ring.
            // Confirmed via a real rendered screenshot (docs/reference/awake-previews/
            // awake-button-variants-light.png): Outline rendered as a solid gray fill,
            // pixel-identical to the border color, not a bordered/transparent button.
            // Stroking the actual ring path avoids ever drawing a solid interior.
            val ringShape = shapeSpec ?: UiShapeSpec.RoundedRectangle(resolvedRadius.px)
            emitPrimitive(UiDrawPrimitive.StrokedPath(ringShape.toPath(slot), UiStroke(borderWidth), borderColor), overlay)
            return
        }
        emitPrimitive(UiDrawPrimitive.RoundedQuad(slot.x, slot.y, slot.width, slot.height, borderColor, resolvedRadius), overlay)
        val innerRadius = (resolvedRadius - borderPx).coerceAtLeast(0f)
        emitPrimitive(
            UiDrawPrimitive.RoundedQuad(
                slot.x + borderPx,
                slot.y + borderPx,
                slot.width - 2 * borderPx,
                slot.height - 2 * borderPx,
                fillColor,
                innerRadius
            ),
            overlay
        )
        return
    }
    if (hasFill) emitFillShape(slot, fillColor, resolvedRadius, shapeSpec, overlay)
    if (borderPx > 0f) border(slot, borderWidth, borderColor, overlay)
}

internal fun UiScope.emitInsetAccent(slot: UiSlot, inset: Float, radiusPx: Float, shapeSpec: UiShapeSpec? = null) {
    val x = slot.x + inset
    val y = slot.y + inset
    val w = slot.width - inset * 2
    val h = slot.height - inset * 2
    emitFillShape(
        slot = UiSlot(x, y, w, h),
        color = theme.tokens.primary,
        radiusPx = (radiusPx - inset).coerceAtLeast(0f),
        shapeSpec = shapeSpec
    )
}
