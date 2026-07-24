// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.fillHeightOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.plan
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layouts.requiresMeasuredDistribution
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun ColumnScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    val effectiveArrangement = horizontalArrangement
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> 4096f
            }
            context.measureRowContent(
                availableHeight,
                effectiveArrangement.baseSpacingPx(),
                content = content
            )
        } else null

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val effectiveStyle = modifier.styleable ?: Style.Empty
    return (this as UiScope).row(
        horizontalArrangement = effectiveArrangement,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        content = content
    )
}

fun RowScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    val requestedHeight = modifier.heightDimension ?: Dimension.FillMax
    val effectiveArrangement = horizontalArrangement
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
            }
            context.measureRowContent(
                availableHeight,
                effectiveArrangement.baseSpacingPx(),
                content = content
            )
        } else null

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val effectiveStyle = modifier.styleable ?: Style.Empty
    return (this as UiScope).row(
        horizontalArrangement = effectiveArrangement,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        content = content
    )
}

fun AbsoluteScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    val effectiveArrangement = horizontalArrangement
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> 4096f
            }
            context.measureRowContent(
                availableHeight,
                effectiveArrangement.baseSpacingPx(),
                content = content
            )
        } else null

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val effectiveStyle = modifier.styleable ?: Style.Empty
    return (this as UiScope).row(
        horizontalArrangement = effectiveArrangement,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        content = content
    )
}

fun BoxScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    val effectiveArrangement = horizontalArrangement
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
            }
            context.measureRowContent(
                availableHeight,
                effectiveArrangement.baseSpacingPx(),
                content = content
            )
        } else null

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val effectiveStyle = modifier.styleable ?: Style.Empty
    return (this as UiScope).row(
        horizontalArrangement = effectiveArrangement,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        content = content
    )
}


fun UiScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    testTag: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: RowScope.(slot: UiSlot) -> Unit
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
    val effectiveArrangement = horizontalArrangement
    val scope = if (effectiveArrangement.requiresMeasuredDistribution()) {
        val measured = context.measureRowContent(
            height = slot.height,
            gap = 0f,
            content = content
        )
        val childWidths = measured.slots.map { it.width }
        val occupiedWidth = childWidths.sum() + effectiveArrangement.baseSpacingPx() * (childWidths.size - 1).coerceAtLeast(0)
        val plan = effectiveArrangement.plan(slot.width, childWidths.size, occupiedWidth)
        var x = slot.x + plan.leadingSpacePx
        val arrangedSlots = measured.slots.map { child ->
            UiSlot(x, slot.y, child.width, child.height).also {
                x += child.width + plan.betweenSpacePx
            }
        }
        context.createRow(
            slot = slot,
            gap = plan.betweenSpacePx,
            horizontalArrangement = effectiveArrangement,
            testTag = testTag ?: modifier.testTag,
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            overlayOnly = emitsToOverlay,
            plannedSlots = arrangedSlots
        )
    } else {
        childRow(
            slot,
            horizontalArrangement = effectiveArrangement,
            modifier = UiModifier(testTag = testTag ?: modifier.testTag),
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent
        )
    }
    scope.content(slot)
    context.popTextStyle()
    return slot
}
