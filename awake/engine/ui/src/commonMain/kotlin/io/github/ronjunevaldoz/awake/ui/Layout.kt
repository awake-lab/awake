// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

/**
 * Everything a [UiScope] needs except `claimSlot` -- shared once here instead of repeated per
 * layout strategy. Not part of the public widget-authoring surface (that's [UiScope]); a
 * consumer writing a custom *layout* strategy (not just a custom widget) extends this the
 * same way [ColumnScope]/[AbsoluteScope] do.
 */
abstract class AbstractUiScope(
    private val context: UiContext,
    final override val font: BitmapFont?,
    final override val theme: UiTheme
) : UiScope {
    final override fun hitTest(slot: UiSlot) = context.hitTestInternal(slot)
    final override fun isActive(id: String) = context.isActiveInternal(id)
    final override fun tryClaimActive(id: String, hovered: Boolean) = context.tryClaimActiveInternal(id, hovered)
    final override fun releaseActiveIfMatches(id: String) = context.releaseActiveIfMatchesInternal(id)
    final override fun emit(primitive: UiDrawPrimitive) = context.emitInternal(primitive)
    final override fun emitOverlay(primitive: UiDrawPrimitive) = context.emitOverlayInternal(primitive)
    final override fun widgetState(id: String) = context.widgetStateInternal(id)
}

/**
 * Vertical auto-stacking layout -- replaces every hand-written `PANEL_ROW_*_Y` constant a
 * consumer used to maintain by hand. Each [claimSlot] call reserves the next row and advances
 * the cursor by that row's height plus [gap].
 */
class ColumnScope internal constructor(
    context: UiContext,
    font: BitmapFont?,
    theme: UiTheme,
    private val x: Float,
    startY: Float,
    private val width: Float,
    private val gap: Float
) : AbstractUiScope(context, font, theme) {
    var cursorY: Float = startY
        private set

    override fun claimSlot(width: Float, height: Float): UiSlot {
        val slot = UiSlot(x, cursorY, width.takeIf { it > 0f } ?: this.width, height)
        cursorY += height + gap
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
    font: BitmapFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float
) : AbstractUiScope(context, font, theme) {
    override fun claimSlot(width: Float, height: Float): UiSlot = UiSlot(x, y, width, height)
}

/**
 * Horizontal counterpart to [ColumnScope] -- advances an X cursor instead of Y. Each
 * [claimSlot] call reserves the next column and advances the cursor by that column's width
 * plus [gap].
 */
class RowScope internal constructor(
    context: UiContext,
    font: BitmapFont?,
    theme: UiTheme,
    startX: Float,
    private val y: Float,
    private val height: Float,
    private val gap: Float
) : AbstractUiScope(context, font, theme) {
    var cursorX: Float = startX
        private set

    override fun claimSlot(width: Float, height: Float): UiSlot {
        val slot = UiSlot(cursorX, y, width, height.takeIf { it > 0f } ?: this.height)
        cursorX += width + gap
        return slot
    }
}

/**
 * Every [claimSlot] call returns the same fixed rect -- for a single fixed-position widget,
 * or deliberately overlapping/stacked content (a background quad behind a label, etc).
 */
class BoxScope internal constructor(
    context: UiContext,
    font: BitmapFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float
) : AbstractUiScope(context, font, theme) {
    override fun claimSlot(width: Float, height: Float): UiSlot = UiSlot(x, y, this.width, this.height)
}
