// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

fun UiScope.textureQuad(material: Any, modifier: UiModifier = UiModifier()) {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.FillMax,
        modifier = modifier
    )
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}

inline fun UiScope.row(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.WrapContent,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(width, height, modifier)
    childRow(slot, gap = gap).content(slot)
    return slot
}

inline fun UiScope.column(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.WrapContent,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(width, height, modifier)
    childColumn(slot, gap = gap).content(slot)
    return slot
}

fun UiScope.panel(
    id: String,
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
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
            gap = gap,
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
    recordSemantic(
        role = UiSemanticRole.Panel,
        id = id,
        bounds = slot
    )
    val contentScope = childColumn(slot, gap = gap, insets = resolved.contentPadding, textScale = resolved.textScale)
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
