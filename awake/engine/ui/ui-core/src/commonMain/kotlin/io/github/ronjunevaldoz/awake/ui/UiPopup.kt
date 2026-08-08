// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.place
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.frameBounds
import io.github.ronjunevaldoz.awake.ui.scope.isMeasuring
import io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent
import io.github.ronjunevaldoz.awake.ui.scope.pointerDown

data class UiPopupSize(
    val width: Float,
    val height: Float,
)

data class UiPopupProperties(
    val dismissOnClickOutside: Boolean = true,
    val clippingEnabled: Boolean = true,
)

data class UiPopupResult(
    val slot: UiBounds?,
    val dismissed: Boolean,
)

fun interface UiPopupPositionProvider {
    fun calculatePosition(
        anchorBounds: UiBounds,
        windowBounds: UiBounds,
        popupContentSize: UiPopupSize,
    ): UiBounds
}

object UiPopupDefaults {
    fun aligned(
        anchorAlignment: UiAlignment = UiAlignment.BottomStart,
        popupAlignment: UiAlignment = UiAlignment.TopStart,
        offsetX: Dp = UiShape.none,
        offsetY: Dp = UiShape.none,
    ): UiPopupPositionProvider =
        UiPopupPositionProvider { anchorBounds, windowBounds, popupContentSize ->
            placePopupRelativeToAnchor(
                anchorBounds = anchorBounds,
                windowBounds = windowBounds,
                popupSize = popupContentSize,
                anchorAlignment = anchorAlignment,
                popupAlignment = popupAlignment,
                offsetX = offsetX.toPx(),
                offsetY = offsetY.toPx(),
            )
        }

    fun dropdown(
        offsetX: Dp = UiShape.none,
        offsetY: Dp = UiShape.none,
    ): UiPopupPositionProvider = aligned(
        anchorAlignment = UiAlignment.BottomStart,
        popupAlignment = UiAlignment.TopStart,
        offsetX = offsetX,
        offsetY = offsetY,
    )

    fun centered(): UiPopupPositionProvider =
        UiPopupPositionProvider { _, windowBounds, popupContentSize ->
            windowBounds.place(
                width = popupContentSize.width,
                height = popupContentSize.height,
                alignment = UiAlignment.Center,
            )
        }

    // Real Radix/shadcn Popover default: side="bottom", align="center", sideOffset=4 -- the
    // content is centered under its trigger with a small gap, unlike [dropdown]'s
    // start-aligned, flush-against-the-trigger placement.
    fun popover(
        offsetX: Dp = UiShape.none,
        offsetY: Dp = 4f.dp,
    ): UiPopupPositionProvider = aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetX = offsetX,
        offsetY = offsetY,
    )
}

fun UiScope.popup(
    anchorSlot: UiBounds,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    id: String? = null,
    fadeDurationMs: Float = 150f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult {
    // Real AnimatedVisibility-equivalent (see UiAnimatedVisibility.kt) instead of the previous
    // bare `if (!expanded) return` instant-unmount: keep computing position/content -- wrapped in
    // a fading graphics-layer alpha -- until the exit tween settles at zero, so show/hide fades
    // instead of snapping. stateId falls back through [id]/[modifier.testTag] so callers that
    // already pass a stable testTag (or the new [id]) get correctly independent fade state per
    // popup instance instead of colliding on one shared, unlabeled key.
    val stateId = id ?: modifier.testTag ?: "popup"
    val alpha = animateFloatTween(
        id = "__popup_alpha__$stateId",
        target = if (expanded) 1f else 0f,
        initial = if (expanded) 1f else 0f,
        durationMs = fadeDurationMs,
    )
    val visuallyActive = expanded || alpha > 0.001f
    if (!visuallyActive) {
        return UiPopupResult(slot = null, dismissed = false)
    }

    val windowBounds = frameBounds()
    val availableWidth = when (width) {
        is Dimension.Fixed -> width.dp.toPx()
        Dimension.FillMax, Dimension.WrapContent -> windowBounds.width
    }.coerceAtLeast(0f)
    val insets = modifier.insets
    val gap = verticalArrangement.baseSpacingPx()

    val measured = if (width == Dimension.WrapContent || height == Dimension.WrapContent) {
        measureColumnContent(
            width = (availableWidth - insets.horizontalPx()).coerceAtLeast(0f),
            gap = gap,
            insets = insets,
            content = content,
        )
    } else {
        null
    }

    val popupWidth = resolvePopupDimension(
        width,
        measured?.width?.plus(insets.horizontalPx()),
        windowBounds.width,
    )
    val popupHeight = resolvePopupDimension(
        height,
        measured?.height?.plus(insets.verticalPx()),
        windowBounds.height,
    )
    val resolvedSize = UiPopupSize(popupWidth, popupHeight)
    val anchorBoundsSlot = anchorSlot
    val placedSlot =
        positionProvider.calculatePosition(anchorBoundsSlot, windowBounds, resolvedSize)
    val popupSlot = if (properties.clippingEnabled) {
        placedSlot.clampWithin(windowBounds)
    } else {
        placedSlot
    }

    // Outside-click dismissal only makes sense while genuinely expanded -- during the exit fade
    // window (expanded already false, still visuallyActive) the caller already decided to close
    // it, so this must not re-report a dismiss every frame of the fade.
    val dismissed = expanded &&
        properties.dismissOnClickOutside &&
        pointerDown() &&
        !hitTest(anchorBoundsSlot) &&
        !hitTest(popupSlot)

    if (dismissed) {
        return UiPopupResult(slot = popupSlot, dismissed = true)
    }

    if (isMeasuring()) {
        return UiPopupResult(slot = popupSlot, dismissed = false)
    }

    withGraphicsLayerAlpha(alpha) {
        context.createColumn(
            slot = popupSlot,
            insets = insets,
            verticalArrangement = verticalArrangement,
            testTag = modifier.testTag,
            overlayOnly = true,
        ).content(popupSlot)
    }

    return UiPopupResult(slot = popupSlot, dismissed = false)
}

private fun resolvePopupDimension(
    requested: Dimension,
    measured: Float?,
    available: Float,
): Float = when (requested) {
    is Dimension.Fixed -> requested.dp.toPx()
    Dimension.FillMax -> available
    Dimension.WrapContent -> (measured ?: 0f).coerceAtLeast(0f)
}

/**
 * Places [popupSize] relative to [anchorBounds] using [anchorAlignment]/[popupAlignment], then
 * -- matching real shadcn/Radix positioning (see `ShadcnPopupPositionProvider` in the
 * shadcn-compose reference) -- flips to the opposite side when the preferred side doesn't have
 * enough room *and* the opposite side actually has more, and finally clamps the result so it
 * never renders outside [windowBounds].
 */
private fun placePopupRelativeToAnchor(
    anchorBounds: UiBounds,
    windowBounds: UiBounds,
    popupSize: UiPopupSize,
    anchorAlignment: UiAlignment,
    popupAlignment: UiAlignment,
    offsetX: Float,
    offsetY: Float,
): UiBounds {
    var resolvedAnchorAlignment = anchorAlignment
    var resolvedPopupAlignment = popupAlignment

    // Vertical stacking (anchor Bottom + popup Top == below anchor, anchor Top + popup Bottom ==
    // above anchor) is flippable; Center/Center pairs (e.g. overlay centering) are not.
    val anchorV = anchorAlignment.verticalSide()
    val popupV = popupAlignment.verticalSide()
    if (anchorV != VerticalSide.Center && popupV != VerticalSide.Center && anchorV != popupV) {
        val placedBelow = anchorV == VerticalSide.Bottom
        val bounds = placePopupRelativeToAnchor(
            anchorBounds,
            popupSize,
            resolvedAnchorAlignment,
            resolvedPopupAlignment,
            offsetX,
            offsetY,
        )
        val overflowsBottom = bounds.y + bounds.height > windowBounds.y + windowBounds.height
        val overflowsTop = bounds.y < windowBounds.y
        val spaceBelow =
            (windowBounds.y + windowBounds.height) - (anchorBounds.y + anchorBounds.height)
        val spaceAbove = anchorBounds.y - windowBounds.y
        if (placedBelow && overflowsBottom && spaceAbove > spaceBelow) {
            resolvedAnchorAlignment = anchorAlignment.flipVertical()
            resolvedPopupAlignment = popupAlignment.flipVertical()
        } else if (!placedBelow && overflowsTop && spaceBelow > spaceAbove) {
            resolvedAnchorAlignment = anchorAlignment.flipVertical()
            resolvedPopupAlignment = popupAlignment.flipVertical()
        }
    }

    // Horizontal stacking (anchor End + popup Start == to the right, anchor Start + popup End ==
    // to the left) is flippable the same way.
    val anchorH = anchorAlignment.horizontalSide()
    val popupH = popupAlignment.horizontalSide()
    if (anchorH != HorizontalSide.Center && popupH != HorizontalSide.Center && anchorH != popupH) {
        val placedEnd = anchorH == HorizontalSide.End
        val bounds = placePopupRelativeToAnchor(
            anchorBounds,
            popupSize,
            resolvedAnchorAlignment,
            resolvedPopupAlignment,
            offsetX,
            offsetY,
        )
        val overflowsEnd = bounds.x + bounds.width > windowBounds.x + windowBounds.width
        val overflowsStart = bounds.x < windowBounds.x
        val spaceEnd = (windowBounds.x + windowBounds.width) - (anchorBounds.x + anchorBounds.width)
        val spaceStart = anchorBounds.x - windowBounds.x
        if (placedEnd && overflowsEnd && spaceStart > spaceEnd) {
            resolvedAnchorAlignment = resolvedAnchorAlignment.flipHorizontal()
            resolvedPopupAlignment = resolvedPopupAlignment.flipHorizontal()
        } else if (!placedEnd && overflowsStart && spaceEnd > spaceStart) {
            resolvedAnchorAlignment = resolvedAnchorAlignment.flipHorizontal()
            resolvedPopupAlignment = resolvedPopupAlignment.flipHorizontal()
        }
    }

    val placed = placePopupRelativeToAnchor(
        anchorBounds,
        popupSize,
        resolvedAnchorAlignment,
        resolvedPopupAlignment,
        offsetX,
        offsetY,
    )
    return placed.clampWithin(windowBounds)
}

private fun placePopupRelativeToAnchor(
    anchorBounds: UiBounds,
    popupSize: UiPopupSize,
    anchorAlignment: UiAlignment,
    popupAlignment: UiAlignment,
    offsetX: Float,
    offsetY: Float,
): UiBounds {
    val anchorPoint = anchorBounds.alignmentPoint(anchorAlignment)
    val popupPoint = popupSize.alignmentOffset(popupAlignment)
    return UiBounds(
        x = anchorPoint.first - popupPoint.first + offsetX,
        y = anchorPoint.second - popupPoint.second + offsetY,
        width = popupSize.width,
        height = popupSize.height,
    )
}

private enum class VerticalSide { Top, Center, Bottom }
private enum class HorizontalSide { Start, Center, End }

private fun UiAlignment.verticalSide(): VerticalSide = when (this) {
    UiAlignment.TopStart, UiAlignment.TopCenter, UiAlignment.TopEnd -> VerticalSide.Top
    UiAlignment.CenterStart, UiAlignment.Center, UiAlignment.CenterEnd -> VerticalSide.Center
    UiAlignment.BottomStart, UiAlignment.BottomCenter, UiAlignment.BottomEnd -> VerticalSide.Bottom
}

private fun UiAlignment.horizontalSide(): HorizontalSide = when (this) {
    UiAlignment.TopStart, UiAlignment.CenterStart, UiAlignment.BottomStart -> HorizontalSide.Start
    UiAlignment.TopCenter, UiAlignment.Center, UiAlignment.BottomCenter -> HorizontalSide.Center
    UiAlignment.TopEnd, UiAlignment.CenterEnd, UiAlignment.BottomEnd -> HorizontalSide.End
}

private fun UiAlignment.flipVertical(): UiAlignment = when (this) {
    UiAlignment.TopStart -> UiAlignment.BottomStart
    UiAlignment.TopCenter -> UiAlignment.BottomCenter
    UiAlignment.TopEnd -> UiAlignment.BottomEnd
    UiAlignment.BottomStart -> UiAlignment.TopStart
    UiAlignment.BottomCenter -> UiAlignment.TopCenter
    UiAlignment.BottomEnd -> UiAlignment.TopEnd
    else -> this
}

private fun UiAlignment.flipHorizontal(): UiAlignment = when (this) {
    UiAlignment.TopStart -> UiAlignment.TopEnd
    UiAlignment.CenterStart -> UiAlignment.CenterEnd
    UiAlignment.BottomStart -> UiAlignment.BottomEnd
    UiAlignment.TopEnd -> UiAlignment.TopStart
    UiAlignment.CenterEnd -> UiAlignment.CenterStart
    UiAlignment.BottomEnd -> UiAlignment.BottomStart
    else -> this
}

private fun UiBounds.alignmentPoint(alignment: UiAlignment): Pair<Float, Float> = when (alignment) {
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

private fun UiPopupSize.alignmentOffset(alignment: UiAlignment): Pair<Float, Float> =
    when (alignment) {
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

private fun UiBounds.clampWithin(bounds: UiBounds): UiBounds {
    // coerceAtLeast(bounds.x/.y) guards the case where the popup is wider/taller than the
    // window itself (max would fall below min and coerceIn would throw) -- pin to the window's
    // near edge instead of crashing.
    val clampedX = x.coerceIn(bounds.x, (bounds.x + bounds.width - width).coerceAtLeast(bounds.x))
    val clampedY = y.coerceIn(bounds.y, (bounds.y + bounds.height - height).coerceAtLeast(bounds.y))
    return UiBounds(clampedX, clampedY, width, height)
}
