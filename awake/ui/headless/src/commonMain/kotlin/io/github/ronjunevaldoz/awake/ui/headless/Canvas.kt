// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiStroke
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.CanvasScope as PrimitiveCanvasScope
import io.github.ronjunevaldoz.awake.ui.canvas as primitiveCanvas

/** Runtime-neutral gradient description for advanced Headless drawing. */
data class HeadlessCanvasGradient(val start: Color, val end: Color, val horizontal: Boolean = true)

/**
 * Deliberate advanced drawing facade. It keeps Core's renderer-facing CanvasScope behind a
 * Headless receiver while exposing only API values and simple geometry operations.
 */
class HeadlessCanvasScope internal constructor(
    private val primitive: PrimitiveCanvasScope,
    val bounds: UiBounds,
) {
    fun nested(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: HeadlessCanvasScope.() -> Unit,
    ) {
        primitive.nested(x, y, width, height) {
            HeadlessCanvasScope(this, UiBounds(bounds.x + x, bounds.y + y, width, height)).content()
        }
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color) =
        primitive.drawRect(x, y, width, height, color)

    fun drawGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        gradient: HeadlessCanvasGradient,
    ) =
        primitive.drawGradientRect(
            x,
            y,
            width,
            height,
            if (gradient.horizontal) {
                UiLinearGradient.horizontal(gradient.start, gradient.end)
            } else {
                UiLinearGradient.vertical(gradient.start, gradient.end)
            },
        )

    fun drawRoundRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        radius: Dp = 0f.dp,
        borderWidth: Dp = 0f.dp,
        borderColor: Color = Color.Transparent,
    ) = primitive.drawRoundRect(x, y, width, height, color, radius, borderWidth, borderColor)

    fun drawCircle(x: Float, y: Float, diameter: Float, color: Color) =
        primitive.drawCircle(x, y, diameter, color)

    fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: Color,
        strokeWidth: Dp = 1f.dp,
    ) = primitive.drawLine(startX, startY, endX, endY, color, UiStroke(width = strokeWidth))

    fun drawText(text: String, x: Float, y: Float, color: Color) =
        primitive.drawText(text, x, y, color)

    fun clipRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: HeadlessCanvasScope.() -> Unit,
    ) {
        primitive.clipRect(x, y, width, height) {
            HeadlessCanvasScope(this, bounds).content()
        }
    }

    fun clipCircle(x: Float, y: Float, diameter: Float, content: HeadlessCanvasScope.() -> Unit) {
        primitive.clipShape(UiShapeSpec.Circle, x, y, diameter, diameter) {
            HeadlessCanvasScope(
                this,
                UiBounds(bounds.x + x, bounds.y + y, diameter, diameter),
            ).content()
        }
    }
}

fun UiScope.canvas(
    id: String = "canvas",
    modifier: UiModifier = Modifier,
    content: HeadlessCanvasScope.() -> Unit,
): UiBounds = primitive.primitiveCanvas(modifier = modifier) {
    // Explicit receiver: this@canvas's UiScope and the CanvasScope lambda receiver now share one
    // @AwakeUiDsl marker (see B7), so an implicit `primitive` here would resolve nowhere.
    this@canvas.primitive.recordSemantic(role = UiSemanticRole.Panel, id = id, bounds = bounds)
    HeadlessCanvasScope(this, bounds).content()
}
