// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont

fun ColumnScope.spacer(modifier: UiModifier) {
    claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
}

fun RowScope.spacer(modifier: UiModifier) {
    claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
}

fun ColumnScope.panel(
    id: String,
    width: Dimension = Dimension.FillMax,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).panel(
    id = id,
    width = modifier.width ?: width,
    height = modifier.height ?: height,
    gap = gap,
    radius = radius,
    borderWidth = borderWidth,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun RowScope.panel(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).panel(
    id = id,
    width = modifier.width ?: width,
    height = modifier.height ?: height,
    gap = gap,
    radius = radius,
    borderWidth = borderWidth,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun AbsoluteScope.panel(
    id: String,
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).panel(
    id = id,
    width = modifier.width ?: width,
    height = modifier.height ?: height,
    gap = gap,
    radius = radius,
    borderWidth = borderWidth,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun BoxScope.panel(
    id: String,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = (this as UiScope).panel(
    id = id,
    width = modifier.width ?: width,
    height = modifier.height ?: height,
    gap = gap,
    radius = radius,
    borderWidth = borderWidth,
    style = style,
    modifier = modifier,
    clipContent = clipContent,
    content = content
)

fun ColumnScope.row(
    height: Dp,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot = row(height.toDimension(), width, gap, modifier, content)

fun ColumnScope.row(
    height: Dimension,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableHeight = when (requestedHeight) {
            is Dimension.Fixed -> requestedHeight.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> 4096f
        }
        context.measureRowContent(
            height = availableHeight,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            content = content
        )
    } else {
        null
    }
    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childRow(slot, gap = gap).content(slot)
    return slot
}

fun ColumnScope.column(
    height: Dimension,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
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
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childColumn(slot, gap = gap, insets = insets).content(slot)
    return slot
}

fun RowScope.row(
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableHeight = when (requestedHeight) {
            is Dimension.Fixed -> requestedHeight.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
        }
        context.measureRowContent(
            height = availableHeight,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            content = content
        )
    } else {
        null
    }
    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childRow(slot, gap = gap).content(slot)
    return slot
}

fun RowScope.column(
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> 4096f
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
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childColumn(slot, gap = gap, insets = insets).content(slot)
    return slot
}

fun AbsoluteScope.row(
    width: Dimension,
    height: Dp,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot = row(width, height.toDimension(), gap, modifier, content)

fun AbsoluteScope.row(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableHeight = when (requestedHeight) {
            is Dimension.Fixed -> requestedHeight.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> 4096f
        }
        context.measureRowContent(
            height = availableHeight,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            content = content
        )
    } else {
        null
    }
    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childRow(slot, gap = gap).content(slot)
    return slot
}

fun AbsoluteScope.column(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> 4096f
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
    val slot = claimSlot(resolvedWidth, resolvedHeight)
    childColumn(slot, gap = gap, insets = insets).content(slot)
    return slot
}

fun BoxScope.row(
    width: Dimension,
    height: Dp,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot = row(width, height.toDimension(), gap, modifier, content)

fun BoxScope.row(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableHeight = when (requestedHeight) {
            is Dimension.Fixed -> requestedHeight.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> fillHeightOrNull() ?: 4096f
        }
        context.measureRowContent(
            height = availableHeight,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            content = content
        )
    } else {
        null
    }
    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).height).px)
        else -> requestedHeight
    }
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    childRow(slot, gap = gap).content(slot)
    return slot
}

fun BoxScope.column(
    width: Dimension,
    height: Dimension,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent -> fillWidthOrNull() ?: 0f
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
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    childColumn(slot, gap = gap, insets = insets).content(slot)
    return slot
}

fun BoxScope.box(
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.FillMax,
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(modifier.width ?: width, modifier.height ?: height, modifier)
    childBox(slot, contentAlignment = contentAlignment).content(slot)
    return slot
}

fun UiContext.measureDslColumnContent(
    width: Float,
    font: UiFont?,
    theme: UiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiMeasuredContent = measureColumnContent(
    width = width,
    font = font,
    theme = theme,
    gap = gap,
    textScale = textScale,
    insets = insets,
    content = content
)

fun UiContext.measureDslRowContent(
    height: Float,
    font: UiFont?,
    theme: UiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets.Zero,
    content: RowScope.(slot: UiSlot) -> Unit
): UiMeasuredContent = measureRowContent(
    height = height,
    font = font,
    theme = theme,
    gap = gap,
    textScale = textScale,
    insets = insets,
    content = content
)
