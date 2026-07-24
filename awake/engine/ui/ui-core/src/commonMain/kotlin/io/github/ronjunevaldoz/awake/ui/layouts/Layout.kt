// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Everything a [io.github.ronjunevaldoz.awake.ui.UiScope] needs except `claimSlot` -- shared once here instead of repeated per
 * layout strategy. Not part of the public widget-authoring surface (that's [io.github.ronjunevaldoz.awake.ui.UiScope]); a
 * consumer writing a custom *layout* strategy (not just a custom widget) extends this the
 * same way [ColumnScope]/[AbsoluteScope] do.
 */
abstract class AbstractUiScope(
    final override val context: UiContext,
    private val emitToOverlay: Boolean = false
) : io.github.ronjunevaldoz.awake.ui.UiScope {
    final override val emitsToOverlay: Boolean = emitToOverlay
    final override fun hitTest(slot: UiSlot) =
        context.hitTestInternal(slot)

    final override fun isActive(id: String) = context.isActiveInternal(id)
    final override fun tryClaimActive(id: String, hovered: Boolean) =
        context.tryClaimActiveInternal(id, hovered)

    final override fun releaseActiveIfMatches(id: String) =
        context.releaseActiveIfMatchesInternal(id)

    final override fun emit(primitive: io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive) =
        if (emitToOverlay) context.emitOverlayInternal(primitive) else context.emitInternal(
            primitive
        )

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
