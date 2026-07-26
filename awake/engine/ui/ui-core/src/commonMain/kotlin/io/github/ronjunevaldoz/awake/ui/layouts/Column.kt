// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.plan
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.scrollPanel
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Dispatches [column] and [surface] to one of three explicit container strategies, chosen by
 * inspecting [modifier]. This is the one place that decides which strategy applies -- each
 * strategy itself is a plain, linearly-readable function, not a branch buried in a larger one.
 */
internal fun UiScope.smartColumn(
    id: String?,
    gap: Float,
    verticalArrangement: Arrangement,
    style: Style,
    modifier: UiModifier,
    clipContent: Boolean = false,
    role: UiSemanticRole = UiSemanticRole.None,
    // ponytail: only the plain-measured-column strategy below actually applies this -- the
    // scrollable/visual-surface strategies delegate to scrollPanel()/surface(), which are a
    // separate widget surface (out of this task's row()/column() scope) and always keep their
    // own Start default. Thread it through those too if a scrollable/surfaced column ever needs
    // a non-default cross-axis alignment.
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    if (modifier.scrollState != null && id != null) {
        return resolveScrollableContainer(id, modifier, style, verticalArrangement, content)
    }

    val effectiveStyle = style then (modifier.styleable ?: Style.Empty)
    if (hasResolvedVisuals(modifier, effectiveStyle, role, id) && id != null) {
        return resolveVisualSurface(id, modifier, effectiveStyle, verticalArrangement, clipContent, content)
    }

    return resolveMeasuredColumn(id, modifier, effectiveStyle, verticalArrangement, role, horizontalAlignment, content)
}

/** True if [effectiveStyle] (merged with this role's theme defaults) resolves to a real
 * background/border/shape -- the signal that a container should paint itself as a surface
 * rather than lay out as a plain, invisible column. */
private fun UiScope.hasResolvedVisuals(
    modifier: UiModifier,
    effectiveStyle: Style,
    role: UiSemanticRole,
    id: String?
): Boolean {
    // Avoid claiming a slot (which may be WrapContent) just to check hover. Use the forced
    // hover when provided, otherwise assume not hovered for this initial style resolution;
    // actual hover is checked later once a slot is claimed.
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: false,
        active = modifier.forceActive ?: (id?.let { isActive(it) } ?: false),
        focused = modifier.forceFocus ?: (id?.let { context.isFocused(it) } ?: false)
    )
    val visualDefaults = if (role == UiSemanticRole.Panel) context.currentTheme.components.surface else Style.Empty
    return (visualDefaults then effectiveStyle).resolve(styleState, context.currentTextStyle).let {
        it.background != null || it.borderWidth.toPx() > 0f || it.shape.toPx() > 0f
    }
}

private fun UiScope.resolveScrollableContainer(
    id: String,
    modifier: UiModifier,
    style: Style,
    verticalArrangement: Arrangement,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = scrollPanel(
    id = id,
    modifier = modifier,
    style = style,
    verticalArrangement = verticalArrangement,
    content = content
).slot

private fun UiScope.resolveVisualSurface(
    id: String,
    modifier: UiModifier,
    effectiveStyle: Style,
    verticalArrangement: Arrangement,
    clipContent: Boolean,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    return surface(
        id = id,
        verticalArrangement = verticalArrangement,
        modifier = modifier.styleable(effectiveStyle).width(requestedWidth).height(requestedHeight),
        clipContent = clipContent,
        content = content
    )
}

private fun UiScope.resolveMeasuredColumn(
    id: String?,
    modifier: UiModifier,
    effectiveStyle: Style,
    verticalArrangement: Arrangement,
    role: UiSemanticRole,
    horizontalAlignment: UiAlignment.Horizontal,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val insets = modifier.insets
    // A weight()-tagged column's width (its host row's main axis) is never actually decided by
    // its own WrapContent content -- it's decided later by the row's weight-distribution pass
    // (see UiScope.row()/resolveWeightedMainAxis()). Falling through to WrapContent here like
    // any other column would let this function's own content trial below permanently bake a
    // Fixed(measuredWidth) into the modifier -- and because Dimension.Fixed.resolveAgainst()
    // always returns its own value regardless of the container slot it's placed into (see
    // Layout.kt), that bogus Fixed width then overrides whatever real share the row's weight
    // distribution assigns downstream. FillMax defers width resolution to claimSlot()/
    // claimModifiedSlot(), which already special-case a weighted child correctly.
    val isWeighted = modifier.layoutWeight != null
    val requestedWidth = modifier.widthDimension ?: (if (isWeighted) Dimension.FillMax else Dimension.WrapContent)
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent

    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent ->
                if (isWeighted) {
                    // Bound this column's own cross-axis (height) content trial by the row's
                    // real, static configured width -- not fillWidthOrNull()'s cursor-shrunk
                    // "space left after earlier siblings claimed theirs" reading, which starves
                    // every weighted sibling after the first toward (or below) zero during any
                    // un-resolved trial pass (real final placement always goes through
                    // resolveWeightedMainAxis()/plannedSlots, so this bound only needs to be
                    // sane, not exact).
                    (this as? RowScope)?.width ?: fillWidthOrNull() ?: 4096f
                } else {
                    fillWidthOrNull() ?: 4096f
                }
        }
        context.measureColumnContent(
            width = (availableWidth - insets.horizontalPx()).coerceAtLeast(0f),
            gap = verticalArrangement.baseSpacingPx(),
            insets = insets,
            content = content
        )
    } else {
        null
    }

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width + insets.horizontalPx()).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> {
            val contentHeight = (requireNotNull(measured).height - insets.top.toPx()).coerceAtLeast(0f)
            Dimension.Fixed((contentHeight + insets.verticalPx()).px)
        }
        else -> requestedHeight
    }

    val rawSlot = column(
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        content = content
    )
    if (role != UiSemanticRole.None && id != null) {
        recordSemantic(role = role, id = id, bounds = rawSlot)
    }
    return rawSlot
}

fun ColumnScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier.withSizeFallback(Dimension.FillMax, Dimension.WrapContent),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    content = content
)

fun RowScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    // Width is this column's main axis (the row's horizontal axis) -- a plain column hugs its
    // own content (WrapContent) by default, but a weight()-tagged one must default to FillMax
    // instead: resolveMeasuredColumn()'s own weight-aware WrapContent deferral (see isWeighted
    // there) only ever sees a *null* widthDimension to override -- withSizeFallback() already
    // bakes in a concrete Dimension before that check runs, so leaving this unconditional here
    // would silently defeat it, right back to a Fixed(measuredContentWidth) that resolveAgainst()
    // then honors over the row's real weighted share (see UiScopeMetrics.claimModifiedSlot).
    modifier.withSizeFallback(
        if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent,
        Dimension.FillMax
    ),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    content = content
)

fun AbsoluteScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier.withSizeFallback(Dimension.WrapContent, Dimension.WrapContent),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    content = content
)

fun BoxScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier.withSizeFallback(Dimension.WrapContent, Dimension.WrapContent),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    content = content
)


fun UiScope.column(
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val sizedModifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.WrapContent)
    val slot = claimModifiedSlot(sizedModifier)
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )
    val textStyle = (style then (modifier.styleable ?: Style.Empty)).resolve(styleState, context.currentTextStyle).textStyle

    context.pushTextStyle(textStyle)
    val requestedWidth = sizedModifier.widthDimension ?: Dimension.FillMax
    val requestedHeight = sizedModifier.heightDimension ?: Dimension.WrapContent
    val effectiveArrangement = verticalArrangement
    // ponytail: see the matching comment in UiScope.row() -- a plain Start/SpacedBy column
    // with no weighted children still takes the original fast childColumn() path below.
    val measured = context.measureColumnContent(
        width = slot.width,
        // See the matching comment in UiScope.row() -- real gap so a FillMax child's trial
        // height already accounts for the gap before its next sibling.
        gap = effectiveArrangement.baseSpacingPx(),
        height = slot.height,
        content = content
    )
    val hasWeightedChild = measured.weights.any { it != null }
    val scope = if (effectiveArrangement.requiresMeasuredDistribution() || hasWeightedChild) {
        val childHeights = resolveWeightedMainAxis(
            measuredSizes = measured.slots.map { it.height },
            weights = measured.weights,
            containerSize = slot.height,
            gap = effectiveArrangement.baseSpacingPx()
        )
        val occupiedHeight = childHeights.sum() + effectiveArrangement.baseSpacingPx() * (childHeights.size - 1).coerceAtLeast(0)
        val plan = effectiveArrangement.plan(slot.height, childHeights.size, occupiedHeight)
        var y = slot.y + plan.leadingSpacePx
        val arrangedSlots = childHeights.mapIndexed { index, height ->
            UiSlot(slot.x, y, measured.slots[index].width, height).also {
                y += height + plan.betweenSpacePx
            }
        }
        context.createColumn(
            slot = slot,
            gap = plan.betweenSpacePx,
            verticalArrangement = effectiveArrangement,
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            overlayOnly = emitsToOverlay,
            plannedSlots = arrangedSlots,
            horizontalAlignment = horizontalAlignment
        )
    } else {
        childColumn(
            slot,
            verticalArrangement = effectiveArrangement,
            modifier = UiModifier(testTag = modifier.testTag),
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            horizontalAlignment = horizontalAlignment
        )
    }
    // See the matching comment in UiScope.row() -- suppress recording while rendering this
    // column's own real children so a composite (e.g. weighted) child's grandchildren don't
    // corrupt an ancestor row/column's own in-progress measured.slots/weights trial.
    context.withMeasuredRecordingSuppressed { scope.content(slot) }
    context.popTextStyle()
    return slot
}
