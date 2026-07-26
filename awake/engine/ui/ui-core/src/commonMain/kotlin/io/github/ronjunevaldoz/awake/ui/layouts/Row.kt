// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent
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
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun ColumnScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    modifier: UiModifier = Modifier,
    content: RowScope.(slot: UiSlot) -> Unit
): UiSlot {
    val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
    // Height is this row's main axis when it's hosted in a column -- a weight()-tagged row must
    // default to FillMax here (deferred to the column's own weight-distribution pass), not
    // WrapContent, the same reasoning as RowScope.column()'s width fallback above.
    val requestedHeight = modifier.heightDimension
        ?: (if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent)
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
        verticalAlignment = verticalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        // See the matching comment in resolveMeasuredColumn()/UiScope.column() -- reuse this
        // WrapContent trial's already-known weighted-child answer instead of paying for a
        // second, identical trial of [content] inside UiScope.row()'s own unconditional pass.
        precomputedMeasured = measured,
        content = content
    )
}

fun RowScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
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
        verticalAlignment = verticalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        // See the matching comment in resolveMeasuredColumn()/UiScope.column() -- reuse this
        // WrapContent trial's already-known weighted-child answer instead of paying for a
        // second, identical trial of [content] inside UiScope.row()'s own unconditional pass.
        precomputedMeasured = measured,
        content = content
    )
}

fun AbsoluteScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
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
        verticalAlignment = verticalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        // See the matching comment in resolveMeasuredColumn()/UiScope.column() -- reuse this
        // WrapContent trial's already-known weighted-child answer instead of paying for a
        // second, identical trial of [content] inside UiScope.row()'s own unconditional pass.
        precomputedMeasured = measured,
        content = content
    )
}

fun BoxScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
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
        verticalAlignment = verticalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        // See the matching comment in resolveMeasuredColumn()/UiScope.column() -- reuse this
        // WrapContent trial's already-known weighted-child answer instead of paying for a
        // second, identical trial of [content] inside UiScope.row()'s own unconditional pass.
        precomputedMeasured = measured,
        content = content
    )
}


fun UiScope.row(
    horizontalArrangement: Arrangement = defaultArrangement(),
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    testTag: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Set only by the ColumnScope/RowScope/AbsoluteScope/BoxScope row() wrappers above, which
    // already ran a full trial of [content] to size a WrapContent row -- see the call site
    // comments there. Every other caller leaves this null and pays the same unconditional trial
    // this function has always run.
    precomputedMeasured: UiMeasuredContent? = null,
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
    // ponytail: a plain Arrangement.Start/SpacedBy row with no weighted children still takes
    // the original fast, single-pass childRow() path below (byte-for-byte, zero regression
    // risk) -- but weight() needs every sibling's width known upfront to divide remaining
    // space, and there's no way to know a row *has* a weighted child without evaluating its
    // content once, so every row now pays for one throwaway trial measurement (discarded
    // below when it turns out nobody used weight() or the arrangement doesn't need it either),
    // UNLESS [precomputedMeasured] was already supplied (the ColumnScope/RowScope/etc. row()
    // wrapper above already ran an equivalent trial to size a WrapContent row) -- weighted-ness
    // is structural (which children called weight()), not dependent on the width/height bound a
    // trial measured against, so it's always safe to reuse for this check alone.
    val hasWeightedChild = (precomputedMeasured ?: run {
        context.measureRowContent(
            height = slot.height,
            gap = effectiveArrangement.baseSpacingPx(),
            width = slot.width,
            content = content
        )
    }).weights.any { it != null }
    val scope = if (effectiveArrangement.requiresMeasuredDistribution() || hasWeightedChild) {
        // The plannedSlots branch below positions children using real, final pixel sizes --
        // unlike the hasWeightedChild check above, this genuinely needs a trial at this row's
        // actual resolved [slot] (not the wrapper's provisional available-size bound), so always
        // re-measure here regardless of whether a precomputed trial was supplied.
        val measured = context.measureRowContent(
            height = slot.height,
            // Real gap, not 0 -- a FillMax child's own trial width must already account for the
            // gap before its next sibling, or it silently overclaims the space that sibling's
            // gap will later eat into (only matters for SpacedBy: every other arrangement's own
            // spacing comes from plan()'s free-space division below, not this base gap).
            gap = effectiveArrangement.baseSpacingPx(),
            width = slot.width,
            content = content
        )
        val childWidths = resolveWeightedMainAxis(
            measuredSizes = measured.slots.map { it.width },
            weights = measured.weights,
            containerSize = slot.width,
            gap = effectiveArrangement.baseSpacingPx()
        )
        val occupiedWidth = childWidths.sum() + effectiveArrangement.baseSpacingPx() * (childWidths.size - 1).coerceAtLeast(0)
        val plan = effectiveArrangement.plan(slot.width, childWidths.size, occupiedWidth)
        var x = slot.x + plan.leadingSpacePx
        val arrangedSlots = childWidths.mapIndexed { index, width ->
            UiSlot(x, slot.y, width, measured.slots[index].height).also {
                x += width + plan.betweenSpacePx
            }
        }
        context.createRow(
            slot = slot,
            // See the matching comment in UiScope.column() -- gap is unused whenever
            // plannedSlots is supplied (this branch always supplies it), so it's no longer
            // threaded here either.
            horizontalArrangement = effectiveArrangement,
            testTag = testTag ?: modifier.testTag,
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            overlayOnly = emitsToOverlay,
            plannedSlots = arrangedSlots,
            verticalAlignment = verticalAlignment
        )
    } else {
        childRow(
            slot,
            horizontalArrangement = effectiveArrangement,
            modifier = UiModifier(testTag = testTag ?: modifier.testTag),
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            verticalAlignment = verticalAlignment
        )
    }
    // This row's own direct-child claims were already recorded above (via the `measured`
    // trial); rendering this row's real content now would re-claim (harmlessly, since
    // childRow()/the plannedSlots branch don't re-measure) but -- critically -- also render any
    // composite child's *own* grandchildren through this same context. Suppress recording for
    // that window so a weighted sibling's WrapContent content doesn't corrupt this row's own
    // already-computed measured.slots/weights (see UiContext.withMeasuredRecordingSuppressed).
    context.withMeasuredRecordingSuppressed { scope.content(slot) }
    context.popTextStyle()
    return slot
}
