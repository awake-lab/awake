// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.verticalPx

fun UiScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = smartColumn(
    id = id,
    width = width,
    height = height,
    gap = gap,
    style = Style {
        shape(radius)
        borderWidth(borderWidth)
    } then style,
    modifier = modifier,
    insets = UiInsets.Zero,
    role = UiSemanticRole.Panel,
    content = content
)

fun ColumnScope.surface(
    id: String,
    height: Dimension,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(id, width, height, gap, radius, borderWidth, style, modifier, content)

fun RowScope.surface(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(id, width, height, gap, radius, borderWidth, style, modifier, content)

fun AbsoluteScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(id, width, height, gap, radius, borderWidth, style, modifier, content)

fun BoxScope.surface(
    id: String,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(id, width, height, gap, radius, borderWidth, style, modifier, content)

fun UiScope.rawSurface(
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
        defaults = theme.components.surface then Style.Companion {
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
        Dimension.WrapContent -> {
            val contentHeight = (requireNotNull(measured).height - resolved.contentPadding.top.toPx()).coerceAtLeast(0f)
            Dimension.Fixed((contentHeight + paddingHeight).px)
        }
        else -> height
    }
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
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