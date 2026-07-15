// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

@AwakeUiDsl
class UiColumnDslScope internal constructor(
    private val columnScope: ColumnScope
) : UiDslScope(columnScope) {

    fun spacer(height: Float) {
        columnScope.claimSlot(Dimension.FillMax, height.toDimension())
    }

    fun panel(
        id: String,
        height: Dimension,
        width: Dimension = Dimension.FillMax,
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
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        clipContent = clipContent
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }

    fun row(
        height: Float,
        width: Dimension = Dimension.FillMax,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        content: UiRowDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.claimSlot(modifier.width ?: width, modifier.height ?: height.toDimension())
        UiRowDslScope(context.row(slot, font, theme, gap, textScale)).content(slot)
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
        UiColumnDslScope(context.column(slot, font, theme, gap, textScale, insets)).content(slot)
        return slot
    }

    fun propertyRow(
        label: String,
        height: Float,
        labelWidth: Dp = 64f.dp,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = columnScope.propertyRow(label, height, labelWidth)
        UiAbsoluteDslScope(context.absolute(slot, font, theme, textScale)).content(slot)
        return slot
    }

    fun propertyCheckbox(
        id: String,
        checked: Boolean,
        label: String,
        height: Float,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        boxSize: Dp = 16f.dp
    ): Boolean = columnScope.propertyCheckbox(id, checked, label, height, modifier, style, boxSize)
}

@AwakeUiDsl
class UiRowDslScope internal constructor(
    private val rowScope: RowScope
) : UiDslScope(rowScope) {

    fun spacer(width: Float) {
        rowScope.claimSlot(width.toDimension(), Dimension.FillMax)
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
        height: Float,
        gap: Float = UiSpacing.sm.toPx(),
        modifier: UiModifier = UiModifier(),
        content: UiRowDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val slot = absoluteScope.claimSlot(modifier.width ?: width, modifier.height ?: height.toDimension())
        UiRowDslScope(context.row(slot, font, theme, gap, textScale)).content(slot)
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
        UiColumnDslScope(context.column(slot, font, theme, gap, textScale, insets)).content(slot)
        return slot
    }
}
