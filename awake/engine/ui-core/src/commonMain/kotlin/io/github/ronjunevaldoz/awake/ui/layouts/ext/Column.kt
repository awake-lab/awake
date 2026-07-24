// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

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
import io.github.ronjunevaldoz.awake.ui.layouts.requiresMeasuredDistribution
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.scrollPanel
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Unified container logic for [column] and [surface].
 * Handles measurement, scrolling, and visible surface drawing in one place.
 */
internal fun UiScope.smartColumn(
    id: String?,
    width: Dimension,
    height: Dimension,
    gap: Float,
    verticalArrangement: Arrangement,
    style: Style,
    modifier: UiModifier,
    clipContent: Boolean = false,
    role: UiSemanticRole = UiSemanticRole.None,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val insets = modifier.insets
    val requestedWidth = modifier.widthDimension ?: width
    val requestedHeight = modifier.heightDimension ?: height
    val scrollState = modifier.scrollState
    val containerTag = modifier.testTag ?: id
    val hasBoundedFillWidth = requestedWidth != Dimension.WrapContent
    val hasBoundedFillHeight = requestedHeight != Dimension.WrapContent

    if (scrollState != null && id != null) {
        return scrollPanel(
            id = id,
            width = requestedWidth,
            height = requestedHeight,
            modifier = modifier,
            style = style,
            verticalArrangement = verticalArrangement,
            content = content
        ).slot
    }

    // Resolve visuals: avoid claiming a slot (which may be WrapContent) just to check hover.
    // Use the forced hover when provided, otherwise assume not hovered for initial style
    // resolution; actual hover will be checked later when a slot is claimed.
    val isHovered = modifier.forceHover ?: false
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: isHovered,
        active = modifier.forceActive ?: (id?.let { isActive(it) } ?: false),
        focused = modifier.forceFocus ?: (id?.let { context.isFocused(it) } ?: false)
    )
    val effectiveStyle = style then (modifier.styleable ?: Style.Empty)
    val visualDefaults = if (role == UiSemanticRole.Panel) context.currentTheme.components.surface else Style.Empty
    val hasVisuals = (visualDefaults then effectiveStyle).resolve(styleState, context.currentTextStyle).let {
        it.background != null || it.borderWidth.toPx() > 0f || it.shape.toPx() > 0f
    }

    if (hasVisuals && id != null) {
        return rawSurface(
            id = id,
            width = requestedWidth,
            height = requestedHeight,
            verticalArrangement = verticalArrangement,
            modifier = modifier.styleable(effectiveStyle),
            clipContent = clipContent,
            content = content
        )
    }

    // Fallback to standard measured column logic
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> fillWidthOrNull() ?: 4096f
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

    val rawSlot = rawColumn(
        width = resolvedWidth,
        height = resolvedHeight,
        verticalArrangement = verticalArrangement,
        modifier = modifier.copy(widthDimension = resolvedWidth, heightDimension = resolvedHeight),
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
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    modifier.widthDimension ?: Dimension.FillMax,
    modifier.heightDimension ?: Dimension.WrapContent,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier,
    clipContent = false,
    content = content
)

fun RowScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    modifier.widthDimension ?: Dimension.WrapContent,
    modifier.heightDimension ?: Dimension.FillMax,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier,
    clipContent = false,
    content = content
)

fun AbsoluteScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    modifier.widthDimension ?: Dimension.WrapContent,
    modifier.heightDimension ?: Dimension.WrapContent,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier,
    clipContent = false,
    content = content
)

fun BoxScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(
    id,
    modifier.widthDimension ?: Dimension.WrapContent,
    modifier.heightDimension ?: Dimension.WrapContent,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier,
    clipContent = false,
    content = content
)


fun UiScope.rawColumn(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(modifier.withSizeFallback(width, height))
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )
    val textStyle = (style then (modifier.styleable ?: Style.Empty)).resolve(styleState, context.currentTextStyle).textStyle

    context.pushTextStyle(textStyle)
    val requestedWidth = modifier.widthDimension ?: width
    val requestedHeight = modifier.heightDimension ?: height
    val effectiveArrangement = verticalArrangement
    val scope = if (effectiveArrangement.requiresMeasuredDistribution()) {
        val measured = context.measureColumnContent(
            width = slot.width,
            gap = 0f,
            content = content
        )
        val childHeights = measured.slots.map { it.height }
        val occupiedHeight = childHeights.sum() + effectiveArrangement.baseSpacingPx() * (childHeights.size - 1).coerceAtLeast(0)
        val plan = effectiveArrangement.plan(slot.height, childHeights.size, occupiedHeight)
        var y = slot.y + plan.leadingSpacePx
        val arrangedSlots = measured.slots.map { child ->
            UiSlot(slot.x, y, child.width, child.height).also {
                y += child.height + plan.betweenSpacePx
            }
        }
        context.createColumn(
            slot = slot,
            gap = plan.betweenSpacePx,
            verticalArrangement = effectiveArrangement,
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            overlayOnly = emitsToOverlay,
            plannedSlots = arrangedSlots
        )
    } else {
        childColumn(
            slot,
            verticalArrangement = effectiveArrangement,
            modifier = UiModifier(testTag = modifier.testTag),
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent
        )
    }
    scope.content(slot)
    context.popTextStyle()
    return slot
}
