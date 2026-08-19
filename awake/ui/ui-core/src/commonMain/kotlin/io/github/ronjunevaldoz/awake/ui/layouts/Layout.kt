// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Everything a [io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope] needs except `claimSlot` -- shared once here instead of repeated per
 * layout strategy. Not part of the public widget-authoring surface (that's [io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope]); a
 * consumer writing a custom *layout* strategy (not just a custom widget) extends this the
 * same way [ColumnScope]/[AbsoluteScope] do.
 */
abstract class AbstractUiScope(
    final override val context: UiContext,
    private val emitToOverlay: Boolean = false,
) : io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope,
    io.github.ronjunevaldoz.awake.ui.UiPrimitiveEmitter {
    final override val emitsToOverlay: Boolean = emitToOverlay
    final override fun hitTest(slot: UiBounds) =
        context.hitTestInternal(slot)

    final override fun isActive(id: String) = context.isActiveInternal(id)
    final override fun tryClaimActive(id: String, hovered: Boolean) =
        context.tryClaimActiveInternal(id, hovered)

    final override fun releaseActiveIfMatches(id: String) =
        context.releaseActiveIfMatchesInternal(id)

    final override fun emit(primitive: io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive) =
        if (emitToOverlay) {
            context.emitOverlayInternal(primitive)
        } else {
            context.emitInternal(
                primitive,
            )
        }

    final override fun emitOverlay(primitive: io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive) =
        context.emitOverlayInternal(primitive)

    final override fun widgetState(id: String) = context.widgetStateInternal(id)
}

/** Resolves a [Dimension] to a raw pixel value against a scope's own configured size --
 * shared by every concrete scope's `claimSlot` below instead of repeating the `when`.
 * [configured] is lazy: a scope with no meaningful "fill" axis (e.g. [RowScope]'s width)
 * passes `{ error(...) }`, which must only evaluate if [FillMax][Dimension.FillMax] is
 * actually requested on that axis, not unconditionally as an eager argument would. */
internal inline fun Dimension.resolve(configured: () -> Float): Float =
    when (this) {
        is Dimension.Fixed -> dp.toPx()
        Dimension.FillMax -> configured()
        Dimension.WrapContent -> error("WrapContent must be resolved by a measuring composite before claimSlot()")
    }

internal fun Dimension.resolveAgainst(available: Float): Float =
    when (this) {
        is Dimension.Fixed -> dp.toPx()
        Dimension.FillMax -> available
        Dimension.WrapContent -> error("WrapContent must be resolved before modifier placement")
    }

internal interface FillAwareScope {
    val fillWidth: Float?
    val fillHeight: Float?
    val hasBoundedFillWidth: Boolean
    val hasBoundedFillHeight: Boolean
    val testTag: String?
}

/**
 * Fails when a container is handed a scroll modifier it does not implement.
 *
 * Scrolling is dispatched on `UiModifier.scrollState`, and only `column()` reads it -- `row()` and
 * `box()` accept the same modifier and drop it. Dropping it silently is the worst outcome: the
 * caller sees a container that simply refuses to scroll, with nothing naming why. Until scroll
 * decorates any container, say so at the call site.
 */
internal fun requireScrollableContainer(modifier: UiModifier, container: String) {
    if (modifier.scrollState == null) return
    error(
        "$container was given Modifier.verticalScroll/horizontalScroll, which only column() " +
            "implements -- it would be ignored here and the container would not scroll. Put the " +
            "scrolling column() inside this $container, or scroll the column directly.",
    )
}

/**
 * Final pixel slots for a vertical container's children, with weights resolved -- or null when
 * nothing in [content] claimed a weight.
 *
 * Extracted so `column()` and `surface()` share one implementation. They used to differ by
 * omission: `column()` distributed weights and `surface()` did not, so the same content laid out
 * two different ways depending only on whether the container happened to paint a background. A
 * rule written once per container is a rule that drifts -- the row/column split of exactly this
 * logic is what hid a weighted child collapsing to its content height.
 */
internal fun UiPrimitiveScope.planWeightedColumnSlots(
    slot: UiBounds,
    arrangement: Arrangement,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): List<UiBounds>? {
    val gap = arrangement.baseSpacingPx()
    // Real gap, so a FillMax child's trial height already accounts for the space before its
    // next sibling.
    val measured = measureColumnContent(
        width = slot.width,
        gap = gap,
        height = slot.height,
        content = content,
    )
    if (measured.weights.none { it != null } && !arrangement.requiresMeasuredDistribution()) {
        return null
    }
    val childHeights = resolveWeightedMainAxis(
        measuredSizes = measured.slots.map { it.height },
        weights = measured.weights,
        containerSize = slot.height,
        gap = gap,
        fillsMainAxis = measured.fillsMainAxis,
    )
    val occupied = childHeights.sum() + gap * (childHeights.size - 1).coerceAtLeast(0)
    val plan = arrangement.plan(slot.height, childHeights.size, occupied)
    var y = slot.y + plan.leadingSpacePx
    return childHeights.mapIndexed { index, height ->
        UiBounds(slot.x, y, measured.slots[index].width, height).also {
            y += height + plan.betweenSpacePx
        }
    }
}
