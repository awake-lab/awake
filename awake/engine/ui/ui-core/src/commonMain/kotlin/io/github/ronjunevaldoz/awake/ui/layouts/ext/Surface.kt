// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = smartColumn(
    id = id,
    gap = verticalArrangement.baseSpacingPx(),
    verticalArrangement = verticalArrangement,
    style = Style {
        shape(UiShape.md)
        borderWidth(UiShape.none)
    } then style,
    modifier = modifier,
    clipContent = clipContent,
    role = UiSemanticRole.Panel,
    content = content
)

fun ColumnScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.copy(widthDimension = modifier.widthDimension ?: Dimension.FillMax),
    clipContent = clipContent,
    content = content
)

fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.copy(heightDimension = modifier.heightDimension ?: Dimension.FillMax),
    clipContent = clipContent,
    content = content
)

fun AbsoluteScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun BoxScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun UiScope.rawSurface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val width = modifier.widthDimension ?: Dimension.WrapContent
    val height = modifier.heightDimension ?: Dimension.WrapContent
    val gap = verticalArrangement.baseSpacingPx()
    val effectiveStyle = modifier.styleable ?: Style.Empty
    val containerTag = modifier.testTag ?: id
    val hasWrapContent = width == Dimension.WrapContent || height == Dimension.WrapContent
    
    // Only perform early slot claim and hit test if we don't have WrapContent dimensions.
    // WrapContent dimensions must be measured first before claiming any slot.
    val (initialSlot, initialHovered) = if (!hasWrapContent) {
        val slot = claimModifiedSlot(modifier.withSizeFallback(width, height))
        slot to hitTest(slot)
    } else {
        null to false
    }
    
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: initialHovered,
        active = modifier.forceActive ?: isActive(id),
        focused = modifier.forceFocus ?: context.isFocused(id)
    )
    val resolved = resolveStyle(
        style = effectiveStyle,
        defaults = context.currentTheme.components.surface,
        state = styleState
    )
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val measured = if (hasWrapContent) {
        val maxContentWidth = when (width) {
            is Dimension.Fixed -> (width.dp.toPx() - paddingWidth).coerceAtLeast(0f)
            Dimension.FillMax -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 0f
            Dimension.WrapContent -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 4096f
        }
        context.measureColumnContent(
            width = maxContentWidth,
            gap = gap,
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
            Dimension.Fixed((requireNotNull(measured).height + paddingHeight).px)
        }
        else -> height
    }
    val slot = initialSlot ?: claimModifiedSlot(modifier.width(resolvedWidth).height(resolvedHeight))
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: context.currentTheme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    recordSemantic(
        role = UiSemanticRole.Panel,
        id = id,
        bounds = slot
    )
    context.pushTextStyle(resolved.textStyle)
    val effectiveShape = resolved.shapeSpec ?: UiShapeSpec.RoundedRectangle(resolved.shape)
    context.pushShapeSpec(effectiveShape)
    
    val contentScope = childColumn(
        slot,
        verticalArrangement = Arrangement.spacedBy(gap.px),
        modifier = UiModifier(insets = resolved.contentPadding, testTag = containerTag),
        hasBoundedFillWidth = width != Dimension.WrapContent,
        hasBoundedFillHeight = height != Dimension.WrapContent
    )
    if (clipContent) {
        clip(effectiveShape, slot) { contentScope.content(slot) }
    } else {
        contentScope.content(slot)
    }
    context.popShapeSpec()
    context.popTextStyle()
    return slot
}
