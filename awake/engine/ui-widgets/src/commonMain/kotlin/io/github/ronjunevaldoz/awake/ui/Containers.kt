// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

fun UiScope.textureQuad(width: Float, height: Float, material: Any, modifier: UiModifier = UiModifier()) {
    val slot = claimModifiedSlot(width.toDimension(), height.toDimension(), modifier)
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}

fun UiScope.panel(
    id: String,
    width: Dimension,
    height: Dimension,
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.panel then Style {
            shape(radius)
            borderWidth(borderWidth)
        }
    )
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val measured = if (width == Dimension.WrapContent || height == Dimension.WrapContent) {
        val maxContentWidth = when (width) {
            is Dimension.Fixed -> (width.dp.toPx() - paddingWidth).coerceAtLeast(0f)
            Dimension.FillMax -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 0f
            Dimension.WrapContent -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 4096f
        }
        context.measureColumnContent(
            width = maxContentWidth,
            font = font,
            theme = theme,
            gap = UiSpacing.sm.toPx(),
            textScale = resolved.textScale,
            content = content
        )
    } else {
        null
    }
    val resolvedWidth = when (width) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width + paddingWidth).px)
        else -> width
    }
    val resolvedHeight = when (height) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height + paddingHeight).px)
        else -> height
    }
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: TransparentColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val contentScope = context.column(slot, font = font, theme = theme, textScale = resolved.textScale, insets = resolved.contentPadding)
    val effectiveShape = resolved.shapeSpec ?: if (resolved.shape.toPx() > 0f) UiShapeSpec.RoundedRectangle(resolved.shape) else null
    if (clipContent && effectiveShape != null) {
        clip(effectiveShape, slot) { contentScope.content(slot) }
    } else {
        contentScope.content(slot)
    }
    return slot
}

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) {
    val resolved = context.pushClip(rect)
    emit(UiDrawPrimitive.ClipPush(resolved))
    content()
    val restore = context.popClip()
    emit(UiDrawPrimitive.ClipPop(restore))
}

fun UiScope.clip(path: UiPath, content: UiScope.() -> Unit) {
    val resolvedBounds = context.pushClip(path.bounds())
    emit(UiDrawPrimitive.ClipPathPush(path, resolvedBounds))
    content()
    val restore = context.popClip()
    emit(UiDrawPrimitive.ClipPop(restore))
}

fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) {
    clip(shape.toPath(rect), content)
}
