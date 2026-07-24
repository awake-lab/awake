// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.graphics.internal.gradientBorder
import io.github.ronjunevaldoz.awake.ui.graphics.internal.gradientRect
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

@AwakeUiDsl
class CanvasScope internal constructor(
    private val scope: UiScope,
    val bounds: UiSlot
) {
    val context get() = scope.context

    fun nested(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: CanvasScope.() -> Unit
    ) {
        if (width <= 0f || height <= 0f) return
        CanvasScope(
            scope = scope,
            bounds = UiSlot(bounds.x + x, bounds.y + y, width, height)
        ).content()
    }

    fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        scope.emitPrimitive(
            UiDrawPrimitive.Quad(
                x = bounds.x + x,
                y = bounds.y + y,
                w = width,
                h = height,
                color = color
            ),
            overlay = overlay
        )
    }

    fun drawGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        gradient: UiLinearGradient,
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        scope.gradientRect(
            slot = UiSlot(bounds.x + x, bounds.y + y, width, height),
            gradient = gradient,
            overlay = overlay
        )
    }

    fun drawGradientBorder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        gradient: UiLinearGradient,
        borderWidth: Dp = 1f.dp,
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        scope.gradientBorder(
            slot = UiSlot(bounds.x + x, bounds.y + y, width, height),
            width = borderWidth,
            gradient = gradient,
            overlay = overlay
        )
    }

    fun drawRoundRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        radius: Dp = UiShape.none,
        borderWidth: Dp = UiShape.none,
        borderColor: Color = Color.Transparent,
        shapeSpec: UiShapeSpec? = UiShapeSpec.RoundedRectangle(radius),
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        scope.emitFillAndBorder(
            slot = UiSlot(bounds.x + x, bounds.y + y, width, height),
            fillColor = color,
            radiusPx = radius.toPx(),
            borderWidth = borderWidth,
            borderColor = borderColor,
            shapeSpec = shapeSpec,
            overlay = overlay
        )
    }

    fun drawShape(
        shape: UiShapeSpec,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        borderWidth: Dp = UiShape.none,
        borderColor: Color = Color.Transparent,
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        val slot = UiSlot(bounds.x + x, bounds.y + y, width, height)
        val radius = when (shape) {
            is UiShapeSpec.RoundedRectangle -> shape.radius.toPx()
            else -> 0f
        }
        scope.emitFillAndBorder(
            slot = slot,
            fillColor = color,
            radiusPx = radius,
            borderWidth = borderWidth,
            borderColor = borderColor,
            shapeSpec = shape,
            overlay = overlay
        )
    }

    fun drawCircle(
        x: Float,
        y: Float,
        diameter: Float,
        color: Color,
        borderWidth: Dp = UiShape.none,
        borderColor: Color = Color.Transparent,
        overlay: Boolean = false
    ) = drawShape(
        shape = UiShapeSpec.Circle,
        x = x,
        y = y,
        width = diameter,
        height = diameter,
        color = color,
        borderWidth = borderWidth,
        borderColor = borderColor,
        overlay = overlay
    )

    fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: Color,
        stroke: UiStroke = UiStroke(),
        overlay: Boolean = false
    ) {
        strokePath(
            path = uiPath {
                moveTo(startX, startY)
                lineTo(endX, endY)
            },
            color = color,
            stroke = stroke,
            overlay = overlay
        )
    }

    fun fillPath(path: UiPath, color: Color, overlay: Boolean = false) {
        scope.emitPrimitive(
            UiDrawPrimitive.FilledPath(
                path = path.transform(translateX = bounds.x, translateY = bounds.y),
                color = color
            ),
            overlay = overlay
        )
    }

    fun strokePath(
        path: UiPath,
        color: Color,
        stroke: UiStroke = UiStroke(),
        overlay: Boolean = false
    ) {
        scope.emitPrimitive(
            UiDrawPrimitive.StrokedPath(
                path = path.transform(translateX = bounds.x, translateY = bounds.y),
                stroke = stroke,
                color = color
            ),
            overlay = overlay
        )
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        color: Color = context.currentTextStyle.color ?: context.currentTheme.tokens.foreground,
        font: UiFont = context.currentFont,
        textStyle: TextStyle = context.currentTextStyle,
        overlay: Boolean = false
    ) {
        if (text.isEmpty()) return
        val glyphPx = scope.resolveGlyphPx(font = font, textStyle = textStyle)
        var cursorX = bounds.x + x
        val baselineY = bounds.y + y
        text.forEach { char ->
            if (char == '\n') return@forEach
            val glyph = font.uvFor(char)
            if (glyph != null) {
                val glyphWidth = glyph.widthEm * glyphPx
                val glyphHeight = glyph.heightEm * glyphPx
                scope.emitPrimitive(
                    UiDrawPrimitive.Glyph(
                        x = cursorX + glyph.offsetXEm * glyphPx,
                        y = baselineY + glyph.offsetYEm * glyphPx,
                        w = glyphWidth,
                        h = glyphHeight,
                        u0 = glyph.u0,
                        v0 = glyph.v0,
                        u1 = glyph.u1,
                        v1 = glyph.v1,
                        color = color
                    ),
                    overlay = overlay
                )
            }
            cursorX += font.advanceFor(char, glyphPx)
        }
    }

    fun drawImage(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        material: Any,
        overlay: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return
        scope.emitPrimitive(
            UiDrawPrimitive.Texture(
                x = bounds.x + x,
                y = bounds.y + y,
                w = width,
                h = height,
                material = material
            ),
            overlay = overlay
        )
    }

    fun clipRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: CanvasScope.() -> Unit
    ) {
        if (width <= 0f || height <= 0f) return
        scope.clip(UiSlot(bounds.x + x, bounds.y + y, width, height)) {
            CanvasScope(this, this@CanvasScope.bounds).content()
        }
    }

    fun clipPath(path: UiPath, content: CanvasScope.() -> Unit) {
        scope.clip(path.transform(translateX = bounds.x, translateY = bounds.y)) {
            CanvasScope(this, this@CanvasScope.bounds).content()
        }
    }

    fun clipShape(
        shape: UiShapeSpec,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: CanvasScope.() -> Unit
    ) {
        if (width <= 0f || height <= 0f) return
        scope.clip(
            shape = shape,
            rect = UiSlot(bounds.x + x, bounds.y + y, width, height)
        ) {
            CanvasScope(this, this@CanvasScope.bounds).content()
        }
    }
}

fun UiScope.canvas(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.FillMax,
    modifier: UiModifier = Modifier,
    content: CanvasScope.() -> Unit
): UiSlot {
    val slot = claimModifiedSlot(defaultWidth = width, defaultHeight = height, modifier = modifier)
    CanvasScope(this, slot).content()
    return slot
}

fun UiScope.canvas(
    slot: UiSlot,
    content: CanvasScope.() -> Unit
) {
    CanvasScope(this, slot).content()
}
