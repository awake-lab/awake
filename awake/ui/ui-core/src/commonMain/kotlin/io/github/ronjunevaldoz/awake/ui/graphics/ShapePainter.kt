// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.CanvasScope
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiStroke
import io.github.ronjunevaldoz.awake.ui.UiStrokeCap
import io.github.ronjunevaldoz.awake.ui.UiStrokeJoin
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.uiPath

/** Snaps a [UiBounds] to whole device pixels the same way [BasicText.kt]'s glyph emission
 * already does for every glyph -- position rounds to the nearest pixel, size rounds and never
 * drops below 1px. Without this, quads/rounded-quads (panels, dividers, icon backgrounds) draw
 * at sub-pixel coordinates that glyphs never do, reading as blurrier/softer-edged than text at
 * non-integer DPI scales or fractional layout positions. */
private fun UiBounds.pixelSnapped(): UiBounds = UiBounds(
    pixelPerfectPixel(x),
    pixelPerfectPixel(y),
    pixelPerfectPixel(width).coerceAtLeast(1f),
    pixelPerfectPixel(height).coerceAtLeast(1f),
)

/** Routes a raw primitive to the normal or overlay layer. Deliberately not `draw*`-named: this
 * operates on the raw [UiPrimitiveScope], one layer below [CanvasScope] -- [CanvasScope]'s own
 * `draw*` members (and the helpers below) call through this, not the other way around. */
fun UiPrimitiveScope.dispatchPrimitive(primitive: UiDrawPrimitive, overlay: Boolean) {
    if (overlay) emitOverlay(primitive) else emit(primitive)
}

private fun roundedRadiusFor(slot: UiBounds, radiusPx: Float, shapeSpec: UiShapeSpec?): Float =
    when (shapeSpec) {
        // Theme tokens such as `shapes.full` are intentionally large (for example 9999dp).
        // A rounded quad must still use a radius bounded by its own slot; otherwise outline
        // pills collapse into a single horizontal stroke when the renderer receives a radius
        // larger than the widget's height.
        null -> radiusPx.coerceIn(0f, minOf(slot.width, slot.height) / 2f)
        UiShapeSpec.Rectangle -> 0f
        is UiShapeSpec.RoundedRectangle -> shapeSpec.radius.toPx()
            .coerceIn(0f, minOf(slot.width, slot.height) / 2f)

        UiShapeSpec.Pill -> minOf(slot.width, slot.height) / 2f
        UiShapeSpec.Circle -> if (slot.width == slot.height) slot.width / 2f else 0f
        is UiShapeSpec.CutCorner -> 0f
        is UiShapeSpec.RoundedCorners -> 0f
    }

// No receiver -- doesn't read draw or layout state, purely a shapeSpec classifier.
private fun pathOnlyShape(slot: UiBounds, shapeSpec: UiShapeSpec?): UiShapeSpec? =
    when (shapeSpec) {
        null, UiShapeSpec.Rectangle, UiShapeSpec.Pill, is UiShapeSpec.RoundedRectangle -> null
        UiShapeSpec.Circle -> if (slot.width == slot.height) null else shapeSpec
        is UiShapeSpec.CutCorner -> shapeSpec
        // Always a path: RoundedQuad's SDF takes one radius, so per-corner has no primitive.
        is UiShapeSpec.RoundedCorners -> shapeSpec
    }

private fun CanvasScope.drawFillShape(
    slot: UiBounds,
    color: Color,
    radiusPx: Float,
    shapeSpec: UiShapeSpec?,
    overlay: Boolean = scope.emitsToOverlay,
    tokenId: String? = null,
) {
    if (color.isTransparent()) return
    val pathShape = pathOnlyShape(slot, shapeSpec)
    if (pathShape != null) {
        scope.dispatchPrimitive(UiDrawPrimitive.FilledPath(pathShape.toPath(slot), color, tokenId = tokenId), overlay)
        return
    }
    val resolvedRadius = roundedRadiusFor(slot, radiusPx, shapeSpec)
    val snapped = slot.pixelSnapped()
    val primitive = if (resolvedRadius > 0f) {
        UiDrawPrimitive.RoundedQuad(snapped.x, snapped.y, snapped.width, snapped.height, color, resolvedRadius, tokenId = tokenId)
    } else {
        UiDrawPrimitive.Quad(snapped.x, snapped.y, snapped.width, snapped.height, color, tokenId = tokenId)
    }
    scope.dispatchPrimitive(primitive, overlay)
}

/** Fill + border for one widget slot, sharing the corner radius correctly between the two.
 *
 * [borderColor] has no theme-resolved default -- [CanvasScope] is a pure draw surface with no
 * theme/context access (Option B, docs/tasks/2026-08-18-ui-capability-scopes-plan.md); callers
 * that want a themed border resolve it themselves before calling. */
fun CanvasScope.drawFillAndBorder(
    slot: UiBounds,
    fillColor: Color,
    radiusPx: Float,
    borderWidth: Dp,
    borderColor: Color = Color.Transparent,
    shapeSpec: UiShapeSpec? = null,
    overlay: Boolean = scope.emitsToOverlay,
    fillTokenId: String? = null,
    borderTokenId: String? = null,
) {
    val hasFill = !fillColor.isTransparent()
    val borderPx = borderWidth.toPx()
    // CSS `border: 1px solid transparent` still belongs to the border-box (intrinsic sizing
    // already accounts for [borderWidth]), but it must not become a draw primitive. Emitting a
    // transparent stroke makes the software rasterizer blend the stroke's black RGB channels
    // into otherwise untouched pixels and produces the dark phantom outlines seen on shadcn
    // tabs and filled badges.
    val hasBorder = borderPx > 0f && !borderColor.isTransparent()
    val pathShape = pathOnlyShape(slot, shapeSpec)
    if (pathShape != null) {
        val path = pathShape.toPath(slot)
        if (hasFill) scope.dispatchPrimitive(UiDrawPrimitive.FilledPath(path, fillColor, tokenId = fillTokenId), overlay)
        if (hasBorder) {
            scope.dispatchPrimitive(
                UiDrawPrimitive.StrokedPath(
                    path,
                    UiStroke(borderWidth),
                    borderColor,
                    tokenId = borderTokenId,
                ),
                overlay,
            )
        }
        return
    }

    val resolvedRadius = roundedRadiusFor(slot, radiusPx, shapeSpec)
    if (resolvedRadius > 0f && hasBorder) {
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
            scope.dispatchPrimitive(
                UiDrawPrimitive.StrokedPath(
                    ringShape.toPath(slot),
                    UiStroke(borderWidth),
                    borderColor,
                    tokenId = borderTokenId,
                ),
                overlay,
            )
            return
        }
        val snapped = slot.pixelSnapped()
        scope.dispatchPrimitive(
            UiDrawPrimitive.RoundedQuad(
                snapped.x,
                snapped.y,
                snapped.width,
                snapped.height,
                borderColor,
                resolvedRadius,
                tokenId = borderTokenId,
            ),
            overlay,
        )
        val innerRadius = (resolvedRadius - borderPx).coerceAtLeast(0f)
        val innerSnapped = UiBounds(
            slot.x + borderPx,
            slot.y + borderPx,
            slot.width - 2 * borderPx,
            slot.height - 2 * borderPx,
        ).pixelSnapped()
        scope.dispatchPrimitive(
            UiDrawPrimitive.RoundedQuad(
                innerSnapped.x,
                innerSnapped.y,
                innerSnapped.width,
                innerSnapped.height,
                fillColor,
                innerRadius,
                tokenId = fillTokenId,
            ),
            overlay,
        )
        return
    }
    if (hasFill) drawFillShape(slot, fillColor, resolvedRadius, shapeSpec, overlay, tokenId = fillTokenId)
    if (hasBorder) scope.border(slot, borderWidth, borderColor, overlay, tokenId = borderTokenId)
}

/** The shadcn checkbox indicator is a check icon, not a filled inset square. [color] is the
 * caller's already-resolved theme color (typically `theme.colors.primaryForeground`) -- see
 * [drawFillAndBorder]'s doc comment for why [CanvasScope] never resolves this itself. */
fun CanvasScope.drawCheckmark(slot: UiBounds, color: Color) {
    val path = uiPath {
        moveTo(slot.x + slot.width * 0.25f, slot.y + slot.height * 0.52f)
        lineTo(slot.x + slot.width * 0.44f, slot.y + slot.height * 0.72f)
        lineTo(slot.x + slot.width * 0.76f, slot.y + slot.height * 0.30f)
    }
    scope.dispatchPrimitive(
        UiDrawPrimitive.StrokedPath(
            path = path,
            stroke = UiStroke(1.75f.px, UiStrokeCap.Round, UiStrokeJoin.Round),
            color = color,
        ),
        overlay = false,
    )
}

/** Paints the centered selected dot used by a radio indicator. */
fun CanvasScope.drawRadioDot(slot: UiBounds, color: Color) {
    // shadcn's `RadioGroupItem` uses `size-4` for the ring and `size-2` for the indicator:
    // the inner dot is exactly half the outer diameter. The old 30% inset produced a 6.4px
    // dot inside a 16px radio instead of the reference's 8px dot.
    val inset = minOf(slot.width, slot.height) * 0.25f
    drawFillShape(
        slot = UiBounds(slot.x + inset, slot.y + inset, slot.width - inset * 2f, slot.height - inset * 2f),
        color = color,
        radiusPx = minOf(slot.width, slot.height) / 2f,
        shapeSpec = UiShapeSpec.Circle,
    )
}

/** Tri-state checkbox's "indeterminate" mark: a horizontal dash -- mirrors real shadcn's
 * checkbox drawing a horizontal line (not a checkmark) when its ToggleableState is
 * Indeterminate. [color] is the caller's already-resolved theme color (typically
 * `theme.colors.primary`). */
fun CanvasScope.drawInsetDash(
    slot: UiBounds,
    inset: Float,
    color: Color,
) {
    val innerW = slot.width - inset * 2
    val innerH = slot.height - inset * 2
    val thickness = (minOf(innerW, innerH) * 0.22f).coerceAtLeast(1f)
    drawFillShape(
        slot = UiBounds(
            slot.x + inset,
            slot.y + inset + (innerH - thickness) / 2f,
            innerW,
            thickness,
        ),
        color = color,
        radiusPx = thickness / 2f,
        shapeSpec = null,
    )
}
