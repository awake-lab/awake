// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont

@AwakeUiDsl
class UiColumnDslScope internal constructor(
    private val columnScope: ColumnScope
) : UiDslScope(columnScope) {

    fun spacer(modifier: UiModifier) {
        columnScope.claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
    }

    fun panel(
        id: String,
        height: Dimension,
        width: Dimension = Dimension.FillMax,
        gap: Float = UiSpacing.sm.toPx(),
        radius: Dp = UiShape.md,
        borderWidth: Dp = UiShape.none,
        style: Style = Style.Empty,
        modifier: UiModifier = UiModifier(),
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = columnScope.panel(
        id = id,
        width = modifier.width ?: width,
        height = modifier.height ?: height,
        gap = gap,
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        clipContent = clipContent
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }

    fun row(
        height: Dp,
        width: Dimension = Dimension.FillMax,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        content: UiRowDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.claimSlot(modifier.width ?: width, modifier.height ?: height.toDimension())
        UiRowDslScope(context.row(slot, font, theme, gap, textScale, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun column(
        height: Dimension,
        width: Dimension = Dimension.FillMax,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        insets: UiInsets = UiInsets.Zero,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.claimSlot(modifier.width ?: width, modifier.height ?: height)
        UiColumnDslScope(context.column(slot, font, theme, gap, textScale, insets, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun propertyRow(
        modifier: UiModifier = UiModifier(),
        labelWidth: Dp = 64f.dp,
        labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = columnScope.propertyRow(
        modifier = modifier,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        UiAbsoluteDslScope(context.absolute(slot, font, theme, textScale, overlayOnly = emitsToOverlay)).content(slot)
    }

    fun propertyRow(
        height: Dp,
        labelWidth: Dp = 64f.dp,
        labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = columnScope.propertyRow(
        height = height.toPx(),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        UiAbsoluteDslScope(context.absolute(slot, font, theme, textScale, overlayOnly = emitsToOverlay)).content(slot)
    }

    fun propertyRow(
        label: String,
        modifier: UiModifier = UiModifier(),
        labelWidth: Dp = 64f.dp,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.propertyRow(label, modifier, labelWidth)
        UiAbsoluteDslScope(context.absolute(slot, font, theme, textScale, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun propertyRow(
        label: String,
        height: Dp,
        labelWidth: Dp = 64f.dp,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.propertyRow(label, height.toPx(), labelWidth)
        UiAbsoluteDslScope(context.absolute(slot, font, theme, textScale, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun propertyCheckbox(
        id: String,
        checked: Boolean,
        label: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        boxSize: Dp = 16f.dp
    ): Boolean = columnScope.propertyCheckbox(id, checked, label, modifier, style, boxSize)

    fun propertyCheckbox(
        id: String,
        checked: Boolean,
        label: String,
        height: Dp,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        boxSize: Dp = 16f.dp
    ): Boolean = columnScope.propertyCheckbox(id, checked, label, height.toPx(), modifier, style, boxSize)
}

@AwakeUiDsl
class UiRowDslScope internal constructor(
    private val rowScope: RowScope
) : UiDslScope(rowScope) {

    fun spacer(modifier: UiModifier) {
        rowScope.claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
    }

    fun panel(
        id: String,
        width: Dimension,
        height: Dimension = Dimension.FillMax,
        gap: Float = UiSpacing.sm.toPx(),
        radius: Dp = UiShape.md,
        borderWidth: Dp = UiShape.none,
        style: Style = Style.Empty,
        modifier: UiModifier = UiModifier(),
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = rowScope.panel(
        id = id,
        width = modifier.width ?: width,
        height = modifier.height ?: height,
        gap = gap,
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        clipContent = clipContent
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }
}

@AwakeUiDsl
class UiAbsoluteDslScope internal constructor(
    private val absoluteScope: AbsoluteScope
) : UiDslScope(absoluteScope) {

    fun panel(
        id: String,
        width: Dimension,
        height: Dimension,
        gap: Float = UiSpacing.sm.toPx(),
        radius: Dp = UiShape.md,
        borderWidth: Dp = UiShape.none,
        style: Style = Style.Empty,
        modifier: UiModifier = UiModifier(),
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = absoluteScope.panel(
        id = id,
        width = modifier.width ?: width,
        height = modifier.height ?: height,
        gap = gap,
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        clipContent = clipContent
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }

    fun row(
        width: Dimension,
        height: Dp,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        content: UiRowDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = absoluteScope.claimSlot(modifier.width ?: width, modifier.height ?: height.toDimension())
        UiRowDslScope(context.row(slot, font, theme, gap, textScale, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun column(
        width: Dimension,
        height: Dimension,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        insets: UiInsets = UiInsets.Zero,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = absoluteScope.claimSlot(modifier.width ?: width, modifier.height ?: height)
        UiColumnDslScope(context.column(slot, font, theme, gap, textScale, insets, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }
}

fun UiContext.measureDslColumnContent(
    width: Float,
    font: UiFont?,
    theme: UiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets.Zero,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiMeasuredContent = measureColumnContent(
    width = width,
    font = font,
    theme = theme,
    gap = gap,
    textScale = textScale,
    insets = insets
) { slot ->
    UiColumnDslScope(this).content(slot)
}

@AwakeUiDsl
class UiBoxDslScope internal constructor(
    private val boxScope: BoxScope
) : UiDslScope(boxScope) {

    fun panel(
        id: String,
        width: Dimension = Dimension.WrapContent,
        height: Dimension = Dimension.WrapContent,
        gap: Float = UiSpacing.sm.toPx(),
        radius: Dp = UiShape.md,
        borderWidth: Dp = UiShape.none,
        style: Style = Style.Empty,
        modifier: UiModifier = UiModifier(),
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = boxScope.panel(
        id = id,
        width = modifier.width ?: width,
        height = modifier.height ?: height,
        gap = gap,
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        clipContent = clipContent
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }

    fun row(
        width: Dimension,
        height: Dp,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        content: UiRowDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = boxScope.claimModifiedSlot(modifier.width ?: width, modifier.height ?: height.toDimension(), modifier)
        UiRowDslScope(context.row(slot, font, theme, gap, textScale, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun column(
        width: Dimension,
        height: Dimension,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        insets: UiInsets = UiInsets.Zero,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val requestedWidth = modifier.width ?: width
        val requestedHeight = modifier.height ?: height
        val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
            val availableWidth = when (requestedWidth) {
                is Dimension.Fixed -> requestedWidth.dp.toPx()
                Dimension.FillMax, Dimension.WrapContent -> boxScope.fillWidthOrNull() ?: 0f
            }
            context.measureColumnContent(
                width = (availableWidth - insets.horizontalPx()).coerceAtLeast(0f),
                font = font,
                theme = theme,
                gap = gap,
                textScale = textScale,
                insets = insets
            ) { measureSlot ->
                UiColumnDslScope(this).content(measureSlot)
            }
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
        val slot = boxScope.claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
        UiColumnDslScope(context.column(slot, font, theme, gap, textScale, insets, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }

    fun box(
        width: Dimension = Dimension.FillMax,
        height: Dimension = Dimension.FillMax,
        modifier: UiModifier = UiModifier(),
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        content: UiBoxDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = boxScope.claimModifiedSlot(modifier.width ?: width, modifier.height ?: height, modifier)
        UiBoxDslScope(context.box(slot, font, theme, textScale, contentAlignment = contentAlignment, overlayOnly = emitsToOverlay)).content(slot)
        return slot
    }
}
