// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui


import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

data class UiScrollPanelResult(
    val slot: UiSlot,
    val viewport: UiSlot,
    val contentWidth: Float,
    val contentHeight: Float,
    val verticalThumb: UiScrollThumb?,
    val horizontalThumb: UiScrollThumb?
)

/**
 * Enhanced dual-axis scroll container.
 * Sizing, scrolling state, and configuration are extracted from [modifier].
 * Visuals are resolved from [style].
 */
fun UiScope.scrollPanel(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    verticalArrangement: Arrangement = defaultArrangement(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiScrollPanelResult {
    val state = requireNotNull(modifier.scrollState) { "scrollPanel requires a scrollState on the modifier" }
    val config = modifier.scrollConfig
    val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    val containerLabel = modifier.testTag ?: id

    val currentTheme = theme
    val resolved = resolveStyle(
        style = style then (modifier.styleable ?: Style.Empty),
        defaults = currentTheme.components.surface
    )
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val scrollbarWidthPx = config.width.toPx().coerceAtLeast(0f)
    val scrollbarGapPx = config.gap.toPx().coerceAtLeast(0f)
    val scrollbarReservePx = if (scrollbarWidthPx > 0f) scrollbarWidthPx + scrollbarGapPx else 0f
    val gap = verticalArrangement.baseSpacingPx()

    fun requireBoundedAxis(axis: String): Float {
        val isBounded = when (axis) {
            "width" -> hasBoundedFillWidth()
            "height" -> hasBoundedFillHeight()
            else -> false
        }
        val value = when (axis) {
            "width" -> fillWidthOrNull()
            "height" -> fillHeightOrNull()
            else -> null
        }
        return if (isBounded && value != null) value else error(
            "Scrollable container '$containerLabel' requested $axis=FillMax under unbounded parent ${debugScopeLabel()}. " +
                "FillMax scroll viewports require a bounded parent $axis. " +
                "A WrapContent ancestor usually means the parent chain never established one."
        )
    }

    fun availableOuterWidth(): Float = when (requestedWidth) {
        is Dimension.Fixed -> requestedWidth.dp.toPx()
        Dimension.FillMax -> requireBoundedAxis(axis = "width")
        Dimension.WrapContent -> (fillWidthOrNull() ?: 4096f)
    }

    val maxInnerWidth = (availableOuterWidth() - paddingWidth).coerceAtLeast(0f)

    // Phase 1: Measure content assuming no vertical scrollbar
    val initialMeasure = measureColumnContent(
        width = maxInnerWidth,
        gap = gap,
        content = content
    )

    // Check if vertical scroll is needed
    val containerHeight = when (requestedHeight) {
        is Dimension.Fixed -> (requestedHeight.dp.toPx() - paddingHeight).coerceAtLeast(0f)
        Dimension.FillMax -> (requireBoundedAxis(axis = "height") - paddingHeight).coerceAtLeast(0f)
        else -> initialMeasure.height // WrapContent
    }

    val verticalNeeded = when (config.verticalVisibility) {
        UiScrollbarVisibility.Always -> true
        UiScrollbarVisibility.Never -> false
        UiScrollbarVisibility.Auto -> initialMeasure.height > containerHeight
    }
    var measured = initialMeasure

    // Phase 2: If vertical needed, re-measure with narrowed width for the scrollbar
    if (verticalNeeded && scrollbarReservePx > 0f) {
        measured = measureColumnContent(
            width = (maxInnerWidth - scrollbarReservePx).coerceAtLeast(0f),
            gap = gap,
            content = content
        )
    }

    // Check horizontal after potential narrowing
    val containerWidth = when (requestedWidth) {
        is Dimension.Fixed -> (requestedWidth.dp.toPx() - paddingWidth).coerceAtLeast(0f)
        Dimension.FillMax -> (requireBoundedAxis(axis = "width") - paddingWidth).coerceAtLeast(0f)
        else -> initialMeasure.width // WrapContent
    }
    val horizontalNeeded = when (config.horizontalVisibility) {
        UiScrollbarVisibility.Always -> true
        UiScrollbarVisibility.Never -> false
        UiScrollbarVisibility.Auto -> measured.width > containerWidth
    }

    val vScrollReservePx = if (verticalNeeded) scrollbarReservePx else 0f
    val hScrollReservePx = if (horizontalNeeded) scrollbarReservePx else 0f

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((measured.width + paddingWidth + vScrollReservePx).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((measured.height + paddingHeight + hScrollReservePx).px)
        else -> requestedHeight
    }

    val slot = claimModifiedSlot(modifier.withSizeFallback(resolvedWidth, resolvedHeight))
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: currentTheme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )

    val innerSlot = slot.inset(resolved.contentPadding)
    val viewport = UiSlot(
        x = innerSlot.x,
        y = innerSlot.y,
        width = (innerSlot.width - vScrollReservePx).coerceAtLeast(0f),
        height = (innerSlot.height - hScrollReservePx).coerceAtLeast(0f)
    )
    recordSemantic(
        role = UiSemanticRole.ScrollPanel,
        id = id,
        bounds = slot.toBounds(),
        contentBounds = viewport.toBounds(),
        clippedBounds = viewport.toBounds()
    )

    state.update(
        viewportWidth = viewport.width,
        viewportHeight = viewport.height,
        contentWidth = measured.width,
        contentHeight = measured.height
    )

    if (hitTest(slot)) {
        onOverScrollable()

        val scrollDeltaY = context.inputState.scrollDeltaY
        val scrollDeltaX = context.inputState.scrollDeltaX

        if (state.canScrollY && scrollDeltaY != 0f) {
            state.scrollBy(deltaY = -scrollDeltaY * config.scrollSpeed)
            onScrollConsumed()
        }
        if (state.canScrollX && scrollDeltaX != 0f) {
            state.scrollBy(deltaX = -scrollDeltaX * config.scrollSpeed)
            onScrollConsumed()
        }
    }

    val contentScope = childColumn(
        slot = UiSlot(
            viewport.x - state.offsetX,
            viewport.y - state.offsetY,
            viewport.width,
            viewport.height
        ),
        verticalArrangement = verticalArrangement,
        modifier = UiModifier(testTag = containerLabel),
    )
    clip(viewport) {
        contentScope.content(viewport)
    }

    // Vertical Scrollbar
    val vThumb = if (verticalNeeded && config.verticalVisibility != UiScrollbarVisibility.Never) {
        val vTrackSlot = UiSlot(
            x = innerSlot.x + innerSlot.width - scrollbarWidthPx,
            y = innerSlot.y,
            width = scrollbarWidthPx,
            height = viewport.height
        )
        verticalScrollThumb(vTrackSlot, state)?.also { thumb ->
            val custom = config.verticalScrollbar
            if (custom != null) {
                childAbsolute(thumb.track).custom(thumb)
            } else {
                emitFillAndBorder(
                    slot = thumb.track,
                    fillColor = currentTheme.tokens.muted.withAlpha(0.4f),
                    radiusPx = scrollbarWidthPx / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent
                )
                emitFillAndBorder(
                    slot = thumb.thumb,
                    fillColor = currentTheme.tokens.primary,
                    radiusPx = scrollbarWidthPx / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent
                )
            }
        }
    } else null

    // Horizontal Scrollbar
    val hThumb = if (horizontalNeeded && config.horizontalVisibility != UiScrollbarVisibility.Never) {
        val hTrackSlot = UiSlot(
            x = innerSlot.x,
            y = innerSlot.y + innerSlot.height - scrollbarWidthPx,
            width = viewport.width,
            height = scrollbarWidthPx
        )
        horizontalScrollThumb(hTrackSlot, state)?.also { thumb ->
            val custom = config.horizontalScrollbar
            if (custom != null) {
                childAbsolute(thumb.track).custom(thumb)
            } else {
                emitFillAndBorder(
                    slot = thumb.track,
                    fillColor = currentTheme.tokens.muted.withAlpha(0.4f),
                    radiusPx = scrollbarWidthPx / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent
                )
                emitFillAndBorder(
                    slot = thumb.thumb,
                    fillColor = currentTheme.tokens.primary,
                    radiusPx = scrollbarWidthPx / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent
                )
            }
        }
    } else null

    return UiScrollPanelResult(
        slot = slot,
        viewport = viewport,
        contentWidth = measured.width,
        contentHeight = measured.height,
        verticalThumb = vThumb,
        horizontalThumb = hThumb
    )
}
