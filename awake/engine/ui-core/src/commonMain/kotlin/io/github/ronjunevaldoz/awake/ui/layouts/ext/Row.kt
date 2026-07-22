// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.fillHeightOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toPx

fun ColumnScope.row(
    height: Dimension,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> 4096f
            }
            context.measureRowContent(
                availableHeight,
                gap,
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
    return rawRow(
        width = resolvedWidth,
        height = resolvedHeight,
        gap = gap,
        modifier = modifier,
        style = effectiveStyle,
        content = content
    )
}

fun ColumnScope.row(
    height: Dp,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot = row(height.toDimension(), width, gap, modifier, content)

fun RowScope.row(
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
            }
            context.measureRowContent(
                availableHeight,
                gap,
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
    return rawRow(
        width = resolvedWidth,
        height = resolvedHeight,
        gap = gap,
        modifier = modifier,
        style = effectiveStyle,
        content = content
    )
}

fun AbsoluteScope.row(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> 4096f
            }
            context.measureRowContent(
                availableHeight,
                gap,
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
    return rawRow(
        width = resolvedWidth,
        height = resolvedHeight,
        gap = gap,
        modifier = modifier,
        style = effectiveStyle,
        content = content
    )
}

fun BoxScope.row(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured =
        if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableHeight = when (requestedHeight) {
                is Dimension.Fixed -> requestedHeight.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
            }
            context.measureRowContent(
                availableHeight,
                gap,
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
    return rawRow(
        width = resolvedWidth,
        height = resolvedHeight,
        gap = gap,
        modifier = modifier,
        style = effectiveStyle,
        content = content
    )
}


inline fun UiScope.rawRow(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.WrapContent,
    gap: Float = UiSpacing.sm.toPx(),
    testTag: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(width, height, modifier)
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )
    val textStyle = (style then (modifier.styleable ?: Style.Empty)).resolve(styleState, context.currentTextStyle).textStyle

    context.pushTextStyle(textStyle)
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val scope = childRow(
        slot,
        gap = gap,
        testTag = testTag ?: modifier.testTag,
        hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
        hasBoundedFillHeight = requestedHeight != Dimension.WrapContent
    )
    scope.content(slot)
    context.popTextStyle()
    return slot
}
