// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.UiStroke
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.ui.toPx


fun UiScope.emitPrimitive(primitive: UiDrawPrimitive, overlay: Boolean) {
    if (overlay) emitOverlay(primitive) else emit(primitive)
}

private fun roundedRadiusFor(slot: UiBounds, radiusPx: Float, shapeSpec: UiShapeSpec?): Float =
    when (shapeSpec) {
        null -> radiusPx
        UiShapeSpec.Rectangle -> 0f
        is UiShapeSpec.RoundedRectangle -> shapeSpec.radius.toPx()
            .coerceIn(0f, minOf(slot.width, slot.height) / 2f)

        UiShapeSpec.Pill -> minOf(slot.width, slot.height) / 2f
        UiShapeSpec.Circle -> if (slot.width == slot.height) slot.width / 2f else 0f
        is UiShapeSpec.CutCorner -> 0f
    }

private fun UiScope.pathOnlyShape(slot: UiBounds, shapeSpec: UiShapeSpec?): UiShapeSpec? =
    when (shapeSpec) {
        null, UiShapeSpec.Rectangle, UiShapeSpec.Pill, is UiShapeSpec.RoundedRectangle -> null
        UiShapeSpec.Circle -> if (slot.width == slot.height) null else shapeSpec
        is UiShapeSpec.CutCorner -> shapeSpec
    }

private fun UiScope.emitFillShape(
    slot: UiBounds,
    color: Color,
    radiusPx: Float,
    shapeSpec: UiShapeSpec?,
    overlay: Boolean = emitsToOverlay
) {
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

/** Fill + border for one widget slot, sharing the corner radius correctly between the two. */
fun UiScope.emitFillAndBorder(
    slot: UiBounds,
    fillColor: Color,
    radiusPx: Float,
    borderWidth: Dp,
    borderColor: Color = context.currentTheme.colors.border,
    shapeSpec: UiShapeSpec? = null,
    overlay: Boolean = emitsToOverlay
) {
    val hasFill = !fillColor.isTransparent()
    val borderPx = borderWidth.toPx()
    val pathShape = pathOnlyShape(slot, shapeSpec)
    if (pathShape != null) {
        val path = pathShape.toPath(slot)
        if (hasFill) emitPrimitive(UiDrawPrimitive.FilledPath(path, fillColor), overlay)
        if (borderPx > 0f) emitPrimitive(
            UiDrawPrimitive.StrokedPath(
                path,
                UiStroke(borderWidth), borderColor
            ), overlay
        )
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
            emitPrimitive(
                UiDrawPrimitive.StrokedPath(
                    ringShape.toPath(slot),
                    UiStroke(borderWidth), borderColor
                ), overlay
            )
            return
        }
        emitPrimitive(
            UiDrawPrimitive.RoundedQuad(
                slot.x,
                slot.y,
                slot.width,
                slot.height,
                borderColor,
                resolvedRadius
            ), overlay
        )
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

fun UiScope.emitInsetAccent(
    slot: UiBounds,
    inset: Float,
    radiusPx: Float,
    shapeSpec: UiShapeSpec? = null
) {
    val x = slot.x + inset
    val y = slot.y + inset
    val w = slot.width - inset * 2
    val h = slot.height - inset * 2
    emitFillShape(
        slot = UiBounds(x, y, w, h),
        color = context.currentTheme.colors.primary,
        radiusPx = (radiusPx - inset).coerceAtLeast(0f),
        shapeSpec = shapeSpec
    )
}

/** Tri-state checkbox's "indeterminate" mark: a horizontal dash instead of the filled
 * inset square [emitInsetAccent] draws for "checked" -- mirrors real shadcn's checkbox
 * drawing a horizontal line (not a checkmark) when its ToggleableState is Indeterminate. */
fun UiScope.emitInsetDash(
    slot: UiBounds,
    inset: Float
) {
    val innerW = slot.width - inset * 2
    val innerH = slot.height - inset * 2
    val thickness = (minOf(innerW, innerH) * 0.22f).coerceAtLeast(1f)
    emitFillShape(
        slot = UiBounds(
            slot.x + inset,
            slot.y + inset + (innerH - thickness) / 2f,
            innerW,
            thickness
        ),
        color = context.currentTheme.colors.primary,
        radiusPx = thickness / 2f,
        shapeSpec = null
    )
}
