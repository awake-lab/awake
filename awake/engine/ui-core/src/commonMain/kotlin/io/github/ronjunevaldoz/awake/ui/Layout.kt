// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont

/** Named spacing scale, in [Dp] -- not wired into [UiModifier] yet (that grows only when a
 * real padding/margin need shows up), just replaces bare gap literals with a named scale. */
object UiSpacing {
    val xs: Dp = 4f.dp
    val sm: Dp = 8f.dp
    val md: Dp = 16f.dp
    val lg: Dp = 24f.dp
    val xl: Dp = 32f.dp
}

/**
 * Everything a [UiScope] needs except `claimSlot` -- shared once here instead of repeated per
 * layout strategy. Not part of the public widget-authoring surface (that's [UiScope]); a
 * consumer writing a custom *layout* strategy (not just a custom widget) extends this the
 * same way [ColumnScope]/[AbsoluteScope] do.
 */
abstract class AbstractUiScope(
    final override val context: UiContext,
    final override val font: UiFont?,
    final override val theme: UiTheme,
    final override val textScale: Float = 1f,
    private val emitToOverlay: Boolean = false
) : UiScope {
    final override fun hitTest(slot: UiSlot) = context.hitTestInternal(slot)
    final override fun isActive(id: String) = context.isActiveInternal(id)
    final override fun tryClaimActive(id: String, hovered: Boolean) = context.tryClaimActiveInternal(id, hovered)
    final override fun releaseActiveIfMatches(id: String) = context.releaseActiveIfMatchesInternal(id)
    final override fun emit(primitive: UiDrawPrimitive) =
        if (emitToOverlay) context.emitOverlayInternal(primitive) else context.emitInternal(primitive)
    final override fun emitOverlay(primitive: UiDrawPrimitive) = context.emitOverlayInternal(primitive)
    final override fun widgetState(id: String) = context.widgetStateInternal(id)
}

/** Resolves a [Dimension] to a raw pixel value against a scope's own configured size --
 * shared by every concrete scope's `claimSlot` below instead of repeating the `when`.
 * [configured] is lazy: a scope with no meaningful "fill" axis (e.g. [RowScope]'s width)
 * passes `{ error(...) }`, which must only evaluate if [FillMax][Dimension.FillMax] is
 * actually requested on that axis, not unconditionally as an eager argument would. */
private inline fun Dimension.resolve(configured: () -> Float): Float = when (this) {
    is Dimension.Fixed -> dp.toPx()
    Dimension.FillMax -> configured()
    Dimension.WrapContent -> error("WrapContent must be resolved by a measuring composite before claimSlot()")
}

internal fun Dimension.resolveAgainst(available: Float): Float = when (this) {
    is Dimension.Fixed -> dp.toPx()
    Dimension.FillMax -> available
    Dimension.WrapContent -> error("WrapContent must be resolved before modifier placement")
}

internal interface FillAwareScope {
    val fillWidth: Float?
    val fillHeight: Float?
}

/**
 * Vertical auto-stacking layout -- replaces every hand-written `PANEL_ROW_*_Y` constant a
 * consumer used to maintain by hand. Each [claimSlot] call reserves the next row and advances
 * the cursor by that row's height plus [gap].
 */
class ColumnScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    private val x: Float,
    startY: Float,
    private val width: Float,
    private val gap: Float,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = width
    override val fillHeight: Float? = null

    var cursorY: Float = startY
        private set

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot {
        val resolvedWidth = width.resolve { this.width }
        val resolvedHeight = height.resolve { error("FillMax height has no meaning in a ColumnScope") }
        val slot = UiSlot(x, cursorY, resolvedWidth, resolvedHeight)
        cursorY += resolvedHeight + gap
        context.recordMeasuredSlot(slot)
        return slot
    }
}

/**
 * Manual placement at an exact x/y, ignoring whatever width/height a widget requests as a
 * layout hint -- the escape hatch for HUD text or a minimap thumbnail that isn't part of any
 * auto-layout column, still going through the same [UiScope] surface as everything else.
 */
class AbsoluteScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = null
    override val fillHeight: Float? = null

    // Unlike ColumnScope/RowScope, the original claimSlot(width: Float, height: Float) never
    // special-cased any value here -- it passed width/height straight through, and a caller
    // passing 0f (e.g. text()'s own default slot, which doesn't need a real width when not
    // centered) got a harmless zero-width UiSlot back. FillMax has no configured size to
    // resolve against on this scope, so it resolves to 0f -- the same literal passthrough
    // behavior, not a new error mode.
    override fun claimSlot(width: Dimension, height: Dimension): UiSlot =
        UiSlot(x, y, width.resolve { 0f }, height.resolve { 0f }).also(context::recordMeasuredSlot)
}

/**
 * Horizontal counterpart to [ColumnScope] -- advances an X cursor instead of Y. Each
 * [claimSlot] call reserves the next column and advances the cursor by that column's width
 * plus [gap].
 */
class RowScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    startX: Float,
    private val y: Float,
    private val height: Float,
    private val gap: Float,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    var cursorX: Float = startX
        private set

    override val fillWidth: Float? = null
    override val fillHeight: Float? = height

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot {
        val resolvedWidth = width.resolve { error("FillMax width has no meaning in a RowScope") }
        val resolvedHeight = height.resolve { this.height }
        val slot = UiSlot(cursorX, y, resolvedWidth, resolvedHeight)
        cursorX += resolvedWidth + gap
        context.recordMeasuredSlot(slot)
        return slot
    }
}

/**
 * Every [claimSlot] call returns the same fixed rect -- for a single fixed-position widget,
 * or deliberately overlapping/stacked content (a background quad behind a label, etc).
 */
class BoxScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float,
    internal val contentAlignment: UiAlignment = UiAlignment.TopStart,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = this.width
    override val fillHeight: Float? = this.height

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot =
        UiSlot(x, y, this.width, this.height).also(context::recordMeasuredSlot)
}
