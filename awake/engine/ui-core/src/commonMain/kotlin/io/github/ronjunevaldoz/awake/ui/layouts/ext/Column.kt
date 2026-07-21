// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scrollPanel
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.verticalPx

/**
 * Unified container logic for [column] and [surface].
 * Handles measurement, scrolling, and visible surface drawing in one place.
 */
internal fun UiScope.smartColumn(
    id: String?,
    width: Dimension,
    height: Dimension,
    gap: Float,
    style: Style,
    modifier: UiModifier,
    insets: UiInsets,
    role: UiSemanticRole = UiSemanticRole.None,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val scrollState = modifier.scrollState

    if (scrollState != null && id != null) {
        return scrollPanel(
            id = id,
            width = requestedWidth,
            height = requestedHeight,
            state = scrollState,
            gap = gap,
            style = style,
            modifier = modifier,
            content = content
        ).slot
    }

    // Resolve visuals: if style has background/border, we use unstyledSurface as the implementation
    val hasVisuals = style.resolve(MutableStyleState(), textScale).let {
        it.background != null || it.borderWidth.toPx() > 0f || it.shape.toPx() > 0f
    }

    if (hasVisuals && id != null) {
        return rawSurface(
            id = id,
            width = requestedWidth,
            height = requestedHeight,
            gap = gap,
            style = style,
            modifier = modifier,
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
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
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
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height + insets.verticalPx()).px)
        else -> requestedHeight
    }

//    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    val rawSlot = rawColumn(
        width = resolvedWidth,
        height = resolvedHeight,
        gap = gap,
        modifier = modifier,
        content = content
    )
    if (role != UiSemanticRole.None && id != null) {
        recordSemantic(role = role, id = id, bounds = rawSlot)
    }
//    childColumn(slot, gap = gap, insets = insets).content(slot)
    return rawSlot
}

fun ColumnScope.column(
    height: Dimension = Dimension.WrapContent,
    width: Dimension = Dimension.FillMax,
    id: String? = null,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(id, width, height, gap, style, modifier, insets, content = content)

fun RowScope.column(
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    id: String? = null,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(id, width, height, gap, style, modifier, insets, content = content)

fun AbsoluteScope.column(
    width: Dimension,
    height: Dimension,
    id: String? = null,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(id, width, height, gap, style, modifier, insets, content = content)

fun BoxScope.column(
    width: Dimension,
    height: Dimension,
    id: String? = null,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).smartColumn(id, width, height, gap, style, modifier, insets, content = content)


inline fun UiScope.rawColumn(
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
