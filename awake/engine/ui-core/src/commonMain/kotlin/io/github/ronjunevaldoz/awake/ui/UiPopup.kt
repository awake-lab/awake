// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement


data class UiPopupSize(
    val width: Float,
    val height: Float
)

data class UiPopupProperties(
    val dismissOnClickOutside: Boolean = true,
    val clippingEnabled: Boolean = true
)

data class UiPopupResult(
    val slot: UiSlot?,
    val dismissed: Boolean
)

fun interface UiPopupPositionProvider {
    fun calculatePosition(
        anchorBounds: UiSlot,
        windowBounds: UiSlot,
        popupContentSize: UiPopupSize
    ): UiSlot
}

object UiPopupDefaults {
    fun aligned(
        anchorAlignment: UiAlignment = UiAlignment.BottomStart,
        popupAlignment: UiAlignment = UiAlignment.TopStart,
        offsetX: Dp = UiShape.none,
        offsetY: Dp = UiShape.none
    ): UiPopupPositionProvider = UiPopupPositionProvider { anchorBounds, _, popupContentSize ->
        placePopupRelativeToAnchor(
            anchorBounds = anchorBounds,
            popupSize = popupContentSize,
            anchorAlignment = anchorAlignment,
            popupAlignment = popupAlignment,
            offsetX = offsetX.toPx(),
            offsetY = offsetY.toPx()
        )
    }

    fun dropdown(
        offsetX: Dp = UiShape.none,
        offsetY: Dp = UiShape.none
    ): UiPopupPositionProvider = aligned(
        anchorAlignment = UiAlignment.BottomStart,
        popupAlignment = UiAlignment.TopStart,
        offsetX = offsetX,
        offsetY = offsetY
    )

    fun centered(): UiPopupPositionProvider = UiPopupPositionProvider { _, windowBounds, popupContentSize ->
        windowBounds.place(
            width = popupContentSize.width,
            height = popupContentSize.height,
            alignment = UiAlignment.Center
        )
    }
}

fun UiScope.popup(
    anchorSlot: UiSlot,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiPopupResult {
    if (!expanded) {
        return UiPopupResult(slot = null, dismissed = false)
    }

    val windowBounds = context.frameBounds()
    val availableWidth = when (width) {
        is Dimension.Fixed -> width.dp.toPx()
        Dimension.FillMax, Dimension.WrapContent -> windowBounds.width
    }.coerceAtLeast(0f)
    val insets = modifier.insets
    val gap = verticalArrangement.baseSpacingPx()

    val measured = if (width == Dimension.WrapContent || height == Dimension.WrapContent) {
        context.measureColumnContent(
            width = (availableWidth - insets.horizontalPx()).coerceAtLeast(0f),
            gap = gap,
            insets = insets,
            content = content
        )
    } else {
        null
    }

    val popupWidth = resolvePopupDimension(width, measured?.width?.plus(insets.horizontalPx()), windowBounds.width)
    val popupHeight = resolvePopupDimension(height, measured?.height?.plus(insets.verticalPx()), windowBounds.height)
    val resolvedSize = UiPopupSize(popupWidth, popupHeight)
    val placedSlot = positionProvider.calculatePosition(anchorSlot, windowBounds, resolvedSize)
    val popupSlot = if (properties.clippingEnabled) {
        placedSlot.clampWithin(windowBounds)
    } else {
        placedSlot
    }

    val dismissed = properties.dismissOnClickOutside &&
        context.pointerDown() &&
        !hitTest(anchorSlot) &&
        !hitTest(popupSlot)

    if (dismissed) {
        return UiPopupResult(slot = popupSlot, dismissed = true)
    }

    if (context.isMeasuring()) {
        return UiPopupResult(slot = popupSlot, dismissed = false)
    }

    context.createColumn(
        slot = popupSlot,
        gap = gap,
        insets = insets,
        verticalArrangement = verticalArrangement,
        testTag = modifier.testTag,
        overlayOnly = true
    ).content(popupSlot)

    return UiPopupResult(slot = popupSlot, dismissed = false)
}

private fun resolvePopupDimension(
    requested: Dimension,
    measured: Float?,
    available: Float
): Float = when (requested) {
    is Dimension.Fixed -> requested.dp.toPx()
    Dimension.FillMax -> available
    Dimension.WrapContent -> (measured ?: 0f).coerceAtLeast(0f)
}

private fun placePopupRelativeToAnchor(
    anchorBounds: UiSlot,
    popupSize: UiPopupSize,
    anchorAlignment: UiAlignment,
    popupAlignment: UiAlignment,
    offsetX: Float,
    offsetY: Float
): UiSlot {
    val anchorPoint = anchorBounds.alignmentPoint(anchorAlignment)
    val popupPoint = popupSize.alignmentOffset(popupAlignment)
    return UiSlot(
        x = anchorPoint.first - popupPoint.first + offsetX,
        y = anchorPoint.second - popupPoint.second + offsetY,
        width = popupSize.width,
        height = popupSize.height
    )
}

private fun UiSlot.alignmentPoint(alignment: UiAlignment): Pair<Float, Float> = when (alignment) {
    UiAlignment.TopStart -> x to y
    UiAlignment.TopCenter -> x + width / 2f to y
    UiAlignment.TopEnd -> x + width to y
    UiAlignment.CenterStart -> x to y + height / 2f
    UiAlignment.Center -> x + width / 2f to y + height / 2f
    UiAlignment.CenterEnd -> x + width to y + height / 2f
    UiAlignment.BottomStart -> x to y + height
    UiAlignment.BottomCenter -> x + width / 2f to y + height
    UiAlignment.BottomEnd -> x + width to y + height
}

private fun UiPopupSize.alignmentOffset(alignment: UiAlignment): Pair<Float, Float> = when (alignment) {
    UiAlignment.TopStart -> 0f to 0f
    UiAlignment.TopCenter -> width / 2f to 0f
    UiAlignment.TopEnd -> width to 0f
    UiAlignment.CenterStart -> 0f to height / 2f
    UiAlignment.Center -> width / 2f to height / 2f
    UiAlignment.CenterEnd -> width to height / 2f
    UiAlignment.BottomStart -> 0f to height
    UiAlignment.BottomCenter -> width / 2f to height
    UiAlignment.BottomEnd -> width to height
}

private fun UiSlot.clampWithin(bounds: UiSlot): UiSlot {
    val clampedX = x.coerceIn(bounds.x, bounds.x + bounds.width - width)
    val clampedY = y.coerceIn(bounds.y, bounds.y + bounds.height - height)
    return UiSlot(clampedX, clampedY, width, height)
}
