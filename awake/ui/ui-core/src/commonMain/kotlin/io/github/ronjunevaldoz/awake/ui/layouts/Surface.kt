// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.inset
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

fun UiPrimitiveScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
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
    content = content,
)

/**
 * Spans the parent's CROSS axis when the caller authored no size on it.
 *
 * Written once and called from the two scope overloads below rather than spelled out in each.
 * Duplicating this sentence per scope is exactly how the weight rule drifted -- RowScope.column
 * deferred a weighted child's main axis and ColumnScope.column did not, for months.
 *
 * The overloads themselves have to stay. Resolution is STATIC, so a call whose receiver is typed
 * UiPrimitiveScope deliberately gets no default even when the runtime instance happens to be a
 * ColumnScope. Collapsing them into one function with a `when (this)` looks equivalent and is not:
 * it hands a default to every such call site, which moved four suites (PanelTest, two signature
 * matrices, and the parity screenshots).
 */
private fun UiModifier.fillingCrossAxis(axis: CrossAxis): UiModifier = when (axis) {
    CrossAxis.Width -> width(widthDimension ?: Dimension.FillMax)
    CrossAxis.Height -> height(heightDimension ?: Dimension.FillMax)
}

private enum class CrossAxis { Width, Height }

fun ColumnScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.fillingCrossAxis(CrossAxis.Width),
    clipContent = clipContent,
    content = content,
)

fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.fillingCrossAxis(CrossAxis.Height),
    clipContent = clipContent,
    content = content,
)

fun UiPrimitiveScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
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
        focused = modifier.forceFocus ?: context.isFocused(id),
    )
    val resolved = resolveStyle(
        style = effectiveStyle,
        defaults = context.currentTheme.components.surface,
        state = styleState,
    )
    // The surface text style participates in measurement as well as painting. Without this,
    // compact surfaces such as badges are measured with the parent text metrics and then drawn
    // with their own caption metrics, producing clipped pills and a border that collapses into
    // a line. Keep the same resolved foreground propagation for both passes.
    // A surface's `foreground` IS its content colour (shadcn's bg-*/text-* pairing), so it has to
    // beat whatever colour was merely inherited. Style.resolve() seeds its builder FROM
    // context.currentTextStyle, so `resolved.textStyle.color` is non-null the moment any ancestor
    // sets one -- testing it for null only ever succeeded at the top of the tree, and every nested
    // surface silently kept the ancestor's colour instead of its own. A Primary badge measured
    // 17:1 standalone and 1.1:1 inside a card, which is dark-on-dark. Comparing against the
    // inherited value distinguishes "declared on this surface" from "merely inherited", so an
    // explicit per-call text colour still wins.
    val inheritedTextColor = context.currentTextStyle.color
    val declaresOwnTextColor =
        resolved.textStyle.color != null && resolved.textStyle.color != inheritedTextColor
    val contentTextStyle = if (!declaresOwnTextColor && resolved.foreground != null) {
        resolved.textStyle.copy(color = resolved.foreground)
    } else {
        resolved.textStyle
    }
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val measured = if (hasWrapContent) {
        val maxContentWidth = when (width) {
            is Dimension.Fixed -> (width.dp.toPx() - paddingWidth).coerceAtLeast(0f)
            Dimension.FillMax -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 0f
            Dimension.WrapContent -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 4096f
        }
        context.pushTextStyle(contentTextStyle)
        try {
            context.measureColumnContent(
                width = maxContentWidth,
                gap = gap,
                content = content,
            )
        } finally {
            context.popTextStyle()
        }
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
    resolved.shadow?.let { shadow ->
        emit(
            UiDrawPrimitive.ShadowQuad(
                x = slot.x,
                y = slot.y,
                w = slot.width,
                h = slot.height,
                radius = resolved.shape.toPx(),
                offsetX = shadow.offsetX.toPx(),
                offsetY = shadow.offsetY.toPx(),
                blurRadius = shadow.blurRadius.toPx(),
                spread = shadow.spread.toPx(),
                color = shadow.color,
                tokenId = shadow.tokenId,
            ),
        )
    }
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: context.currentTheme.colors.border,
        shapeSpec = resolved.shapeSpec,
        fillTokenId = resolved.backgroundToken,
        borderTokenId = resolved.borderColorToken,
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
        borderRadius = resolved.shape.toPx(),
    )
    // A surface's `foreground` is its content colour, so it has to reach the text inside it.
    // Without this, text() found no colour on the inherited text style and fell back to the
    // theme's own foreground -- which on an inverted surface is the same colour as the
    // background it is sitting on. shadcn's tooltip (bg-foreground/text-background) rendered as
    // an unreadable dark-on-dark pill because of it. An explicit textStyle colour still wins.
    context.pushTextStyle(contentTextStyle)
    val effectiveShape = resolved.shapeSpec ?: UiShapeSpec.RoundedRectangle(resolved.shape)
    context.pushShapeSpec(effectiveShape)

    // A surface is a column that paints. It has to divide its height the same way a plain
    // column does -- without this it dropped every child weight, so whether a container
    // distributed space came down to whether it happened to have a background.
    val contentArrangement = Arrangement.spacedBy(gap.px)
    val contentInsets = resolved.contentPadding
    val plannedSlots = planWeightedColumnSlots(
        slot = slot.inset(contentInsets),
        arrangement = contentArrangement,
        content = content,
    )
    val contentScope = childColumn(
        slot,
        verticalArrangement = contentArrangement,
        modifier = UiModifier(insets = contentInsets, testTag = containerTag),
        hasBoundedFillWidth = width != Dimension.WrapContent,
        hasBoundedFillHeight = height != Dimension.WrapContent,
        plannedSlots = plannedSlots,
    )
    // Any non-zero shape/radius must clip its own children, not just round the background
    // paint -- otherwise square-cornered content (a badge, a button row) can visibly poke
    // out past the curve at large radii. `clipContent` remains available for callers that
    // want clipping on an otherwise-square surface (e.g. Dialog/DropdownMenu clip *and* have
    // a real corner radius, so this condition already covers them too).
    //
    // Matches UiPrimitiveScope.row()/UiPrimitiveScope.column()'s own `withMeasuredRecordingSuppressed { scope.
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
