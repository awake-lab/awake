// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.dispatchPrimitive
import io.github.ronjunevaldoz.awake.ui.graphics.drawFillAndBorder
import io.github.ronjunevaldoz.awake.ui.graphics.gradientBorder
import io.github.ronjunevaldoz.awake.ui.graphics.gradientRect
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

@AwakeUiDsl
class CanvasScope internal constructor(
    internal val scope: UiPrimitiveScope,
    val bounds: UiBounds,
) {
    fun nested(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: CanvasScope.() -> Unit,
    ) {
        if (width <= 0f || height <= 0f) return
        CanvasScope(
            scope = scope,
            bounds = UiBounds(bounds.x + x, bounds.y + y, width, height),
        ).content()
    }

    fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        scope.dispatchPrimitive(
            UiDrawPrimitive.Quad(
                x = bounds.x + x,
                y = bounds.y + y,
                w = width,
                h = height,
                color = color,
            ),
            overlay = overlay,
        )
    }

    fun drawGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        gradient: UiLinearGradient,
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        scope.gradientRect(
            slot = UiBounds(bounds.x + x, bounds.y + y, width, height),
            gradient = gradient,
            overlay = overlay,
        )
    }

    fun drawGradientBorder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        gradient: UiLinearGradient,
        borderWidth: Dp = 1f.dp,
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        scope.gradientBorder(
            slot = UiBounds(bounds.x + x, bounds.y + y, width, height),
            width = borderWidth,
            gradient = gradient,
            overlay = overlay,
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
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        drawFillAndBorder(
            slot = UiBounds(bounds.x + x, bounds.y + y, width, height),
            fillColor = color,
            radiusPx = radius.toPx(),
            borderWidth = borderWidth,
            borderColor = borderColor,
            shapeSpec = shapeSpec,
            overlay = overlay,
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
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        val slot = UiBounds(bounds.x + x, bounds.y + y, width, height)
        val radius = when (shape) {
            is UiShapeSpec.RoundedRectangle -> shape.radius.toPx()
            else -> 0f
        }
        drawFillAndBorder(
            slot = slot,
            fillColor = color,
            radiusPx = radius,
            borderWidth = borderWidth,
            borderColor = borderColor,
            shapeSpec = shape,
            overlay = overlay,
        )
    }

    fun drawCircle(
        x: Float,
        y: Float,
        diameter: Float,
        color: Color,
        borderWidth: Dp = UiShape.none,
        borderColor: Color = Color.Transparent,
        overlay: Boolean = false,
    ) = drawShape(
        shape = UiShapeSpec.Circle,
        x = x,
        y = y,
        width = diameter,
        height = diameter,
        color = color,
        borderWidth = borderWidth,
        borderColor = borderColor,
        overlay = overlay,
    )

    fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: Color,
        stroke: UiStroke = UiStroke(),
        overlay: Boolean = false,
    ) {
        strokePath(
            path = uiPath {
                moveTo(startX, startY)
                lineTo(endX, endY)
            },
            color = color,
            stroke = stroke,
            overlay = overlay,
        )
    }

    fun fillPath(path: UiPath, color: Color, overlay: Boolean = false) {
        scope.dispatchPrimitive(
            UiDrawPrimitive.FilledPath(
                path = path.transform(translateX = bounds.x, translateY = bounds.y),
                color = color,
            ),
            overlay = overlay,
        )
    }

    fun strokePath(
        path: UiPath,
        color: Color,
        stroke: UiStroke = UiStroke(),
        overlay: Boolean = false,
    ) {
        scope.dispatchPrimitive(
            UiDrawPrimitive.StrokedPath(
                path = path.transform(translateX = bounds.x, translateY = bounds.y),
                stroke = stroke,
                color = color,
            ),
            overlay = overlay,
        )
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        // Reads scope.context directly (not a public CanvasScope.context accessor -- removed,
        // see docs/tasks/2026-08-18-ui-capability-scopes-plan.md step 3): these are drawText's
        // own convenience defaults, an existing CanvasScope capability untouched by the
        // ShapePainter/Option B migration, not a new external theme dependency.
        color: Color = scope.context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle).color
            ?: scope.context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTheme).colors.foreground,
        font: UiFont = scope.context.current(io.github.ronjunevaldoz.awake.ui.context.LocalFont),
        textStyle: TextStyle = scope.context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle),
        overlay: Boolean = false,
    ) {
        if (text.isEmpty()) return
        val glyphPx = scope.resolveGlyphPx(font = font, textStyle = textStyle)
        var cursorX = bounds.x + x
        val baselineY = bounds.y + y
        text.forEach { char ->
            if (char == '\n') return@forEach
            val glyph = font.glyphFor(char, textStyle.weight)
            if (glyph != null) {
                val glyphWidth = glyph.widthEm * glyphPx
                val glyphHeight = glyph.heightEm * glyphPx
                scope.dispatchPrimitive(
                    UiDrawPrimitive.Glyph(
                        x = cursorX + glyph.offsetXEm * glyphPx,
                        y = baselineY + glyph.offsetYEm * glyphPx,
                        w = glyphWidth,
                        h = glyphHeight,
                        u0 = glyph.u0,
                        v0 = glyph.v0,
                        u1 = glyph.u1,
                        v1 = glyph.v1,
                        color = color,
                    ),
                    overlay = overlay,
                )
            }
            cursorX += font.advanceFor(char, glyphPx, textStyle.weight)
        }
    }

    fun drawImage(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        material: Any,
        overlay: Boolean = false,
    ) {
        if (width <= 0f || height <= 0f) return
        scope.dispatchPrimitive(
            UiDrawPrimitive.Texture(
                x = bounds.x + x,
                y = bounds.y + y,
                w = width,
                h = height,
                material = material,
            ),
            overlay = overlay,
        )
    }

    fun clipRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        content: CanvasScope.() -> Unit,
    ) {
        if (width <= 0f || height <= 0f) return
        scope.clip(UiBounds(bounds.x + x, bounds.y + y, width, height)) {
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
        content: CanvasScope.() -> Unit,
    ) {
        if (width <= 0f || height <= 0f) return
        val clipRect = UiBounds(bounds.x + x, bounds.y + y, width, height)
        scope.clip(
            shape = shape,
            rect = clipRect,
        ) {
            // Must match [nested]'s local-coordinate-frame translation: the clip mask is
            // anchored at [clipRect], but the old code re-based the inner CanvasScope on the
            // *outer*, untranslated bounds -- content drawn at its own local (0, 0) landed at
            // the outer canvas's top-left corner, entirely outside the (x, y)-offset clip mask,
            // so every clipShape { ... } block silently clipped its own content away to nothing.
            CanvasScope(this, clipRect).content()
        }
    }
}

fun UiPrimitiveScope.canvas(
    modifier: UiModifier = Modifier,
    content: CanvasScope.() -> Unit,
): UiBounds {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    CanvasScope(this, slot).content()
    return slot
}

fun UiPrimitiveScope.canvas(
    slot: UiBounds,
    content: CanvasScope.() -> Unit,
) {
    CanvasScope(this, slot).content()
}
