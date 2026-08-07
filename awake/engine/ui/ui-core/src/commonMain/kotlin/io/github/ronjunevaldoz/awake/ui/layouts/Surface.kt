// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.resolveClickable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx

fun UiScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = smartColumn(
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
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.width(modifier.widthDimension ?: Dimension.FillMax),
    clipContent = clipContent,
    content = content
)

fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.height(modifier.heightDimension ?: Dimension.FillMax),
    clipContent = clipContent,
    content = content
)

fun AbsoluteScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = (this as UiScope).surface(
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
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = (this as UiScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun UiScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds {
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
    resolveClickable(id = id, slot = slot, modifier = modifier)
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: context.currentTheme.colors.border,
        shapeSpec = resolved.shapeSpec,
        fillTokenId = resolved.backgroundToken,
        borderTokenId = resolved.borderColorToken
    )
    recordSemantic(
        role = UiSemanticRole.Panel,
        id = id,
        bounds = slot,
        backgroundColor = resolved.background,
        backgroundToken = resolved.backgroundToken,
        foregroundColor = resolved.foreground,
        foregroundToken = resolved.foregroundToken,
        borderColor = resolved.borderColor,
        borderToken = resolved.borderColorToken,
        borderRadius = resolved.shape.toPx()
    )
    context.pushTextStyle(resolved.textStyle, tokenId = resolved.textStyleToken)
    val effectiveShape = resolved.shapeSpec ?: UiShapeSpec.RoundedRectangle(resolved.shape)
    context.pushShapeSpec(effectiveShape)
    
    val contentScope = childColumn(
        slot,
        verticalArrangement = Arrangement.spacedBy(gap.px),
        modifier = UiModifier(insets = resolved.contentPadding, testTag = containerTag),
        hasBoundedFillWidth = width != Dimension.WrapContent,
        hasBoundedFillHeight = height != Dimension.WrapContent
    )
    // Any non-zero shape/radius must clip its own children, not just round the background
    // paint -- otherwise square-cornered content (a badge, a button row) can visibly poke
    // out past the curve at large radii. `clipContent` remains available for callers that
    // want clipping on an otherwise-square surface (e.g. Dialog/DropdownMenu clip *and* have
    // a real corner radius, so this condition already covers them too).
    //
    // Matches UiScope.row()/UiScope.column()'s own `withMeasuredRecordingSuppressed { scope.
    // content(slot) }` (see the comment there) -- surface() is just as much a composite widget
    // as a plain row()/column(), so its own real children's claimSlot() calls must not leak
    // into an ancestor row/column's in-progress measuredSlots/measuredWeights/fillsMainAxis
    // trial when a fixed-width surface() (shadcnSidebar/shadcnSurface/shadcnCard, all route
    // through here) sits next to a weight()-tagged sibling that forces the ancestor into its
    // trial-measurement (hasWeightedChild/plannedSlots) path. Without this, real content inside
    // the surface (e.g. a `text()` call) adds an extra, spurious entry to those parallel lists,
    // desyncing resolveWeightedMainAxis()'s index pairing and -- worse -- the ancestor's
    // plannedSlots consumption order, silently handing later siblings the wrong slot.
    context.withMeasuredRecordingSuppressed {
        if (clipContent || resolved.shape.toPx() > 0f || resolved.shapeSpec != null) {
            clip(effectiveShape, slot) { contentScope.content(slot) }
        } else {
            contentScope.content(slot)
        }
    }
    context.popShapeSpec()
    context.popTextStyle()
    return slot
}
