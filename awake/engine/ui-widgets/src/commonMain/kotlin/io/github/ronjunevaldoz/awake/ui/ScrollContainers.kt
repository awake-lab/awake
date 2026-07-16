// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input

data class UiScrollPanelResult(
    val slot: UiSlot,
    val viewport: UiSlot,
    val contentHeight: Float,
    val thumb: UiScrollThumb?
)

fun UiScope.scrollPanel(
    id: String,
    width: Dimension,
    height: Dimension,
    state: UiScrollState,
    gap: Float = UiSpacing.sm.toPx(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    scrollSpeed: Float = 32f,
    scrollbarWidth: Dp = 6f.dp,
    scrollbarGap: Dp = 8f.dp,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiScrollPanelResult {
    val requestedWidth = modifier.width ?: width
    val requestedHeight = modifier.height ?: height
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.panel then Style {
            shape(radius)
            borderWidth(borderWidth)
        }
    )
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val scrollbarWidthPx = scrollbarWidth.toPx().coerceAtLeast(0f)
    val scrollbarGapPx = scrollbarGap.toPx().coerceAtLeast(0f)
    val scrollbarReservePx = if (scrollbarWidthPx > 0f) scrollbarWidthPx + scrollbarGapPx else 0f

    fun availableOuterWidth(): Float = when (requestedWidth) {
        is Dimension.Fixed -> requestedWidth.dp.toPx()
        Dimension.FillMax -> fillWidthOrNull() ?: 0f
        Dimension.WrapContent -> (fillWidthOrNull() ?: 4096f)
    }

    val measured = context.measureColumnContent(
        width = (availableOuterWidth() - paddingWidth - scrollbarReservePx).coerceAtLeast(0f),
        font = font,
        theme = theme,
        gap = gap,
        textScale = resolved.textScale,
        content = content
    )

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((measured.width + paddingWidth + scrollbarReservePx).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((measured.height + paddingHeight).px)
        else -> requestedHeight
    }

    val slot = claimModifiedSlot(
        defaultWidth = resolvedWidth,
        defaultHeight = resolvedHeight,
        modifier = modifier
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: TransparentColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )

    val innerSlot = slot.inset(resolved.contentPadding)
    val viewport = UiSlot(
        x = innerSlot.x,
        y = innerSlot.y,
        width = (innerSlot.width - scrollbarReservePx).coerceAtLeast(0f),
        height = innerSlot.height.coerceAtLeast(0f)
    )

    state.update(viewportHeight = viewport.height, contentHeight = measured.height)
    if (state.canScroll && hitTest(slot)) {
        val scrollDelta = Input.consumeScrollDeltaY()
        if (scrollDelta != 0f) {
            state.scrollBy(-scrollDelta * scrollSpeed)
        }
    }

    val contentScope = context.column(
        x = viewport.x,
        y = viewport.y - state.offsetY,
        width = viewport.width,
        font = font,
        theme = theme,
        gap = gap,
        textScale = resolved.textScale
    )
    clip(viewport) {
        contentScope.content(viewport)
    }

    val trackSlot = UiSlot(
        x = innerSlot.x + innerSlot.width - scrollbarWidthPx,
        y = innerSlot.y,
        width = scrollbarWidthPx,
        height = innerSlot.height
    )
    val thumb = verticalScrollThumb(trackSlot, state)
    if (thumb != null) {
        emitFillAndBorder(
            slot = thumb.track,
            fillColor = theme.tokens.muted.withAlpha(0.4f),
            radiusPx = scrollbarWidthPx / 2f,
            borderWidth = UiShape.none,
            borderColor = TransparentColor
        )
        emitFillAndBorder(
            slot = thumb.thumb,
            fillColor = theme.tokens.accent,
            radiusPx = scrollbarWidthPx / 2f,
            borderWidth = UiShape.none,
            borderColor = TransparentColor
        )
    }

    return UiScrollPanelResult(
        slot = slot,
        viewport = viewport,
        contentHeight = measured.height,
        thumb = thumb
    )
}
