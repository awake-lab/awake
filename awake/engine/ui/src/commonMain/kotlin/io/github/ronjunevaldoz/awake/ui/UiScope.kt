// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

data class UiSlot(val x: Float, val y: Float, val width: Float, val height: Float)

/**
 * The full set of primitives any widget -- built-in or consumer-defined -- is built from.
 * Nothing here knows about buttons, toggles, or any specific widget shape; [Widgets.kt]'s
 * button/toggle/slider/dropdown are just the library's own extension functions written
 * against this same public surface. A consumer writes a custom widget the identical way:
 * `fun UiScope.myWidget(...) { val slot = claimSlot(...); ... }` -- no library change, no
 * capability gap versus a built-in widget.
 */
interface UiScope {
    val font: BitmapFont?

    /**
     * The color/appearance policy in effect for this scope -- see [UiTheme]. Widgets default
     * to `theme.button`/`theme.toggle`/etc when no per-call [UiStyle] override is given, so
     * swapping a whole panel's look is one assignment at the scope's creation, not a change
     * to every widget call site.
     */
    val theme: UiTheme

    /**
     * Reserves the next layout position for a widget of the given size and returns its
     * resolved screen-space rect. What "next position" means is entirely up to the
     * implementing scope -- [ColumnScope] advances a Y cursor, [AbsoluteScope] ignores
     * width/height and returns the exact x/y it was constructed with.
     */
    fun claimSlot(width: Float, height: Float): UiSlot

    fun hitTest(slot: UiSlot): Boolean
    fun isActive(id: String): Boolean
    fun tryClaimActive(id: String, hovered: Boolean)
    fun releaseActiveIfMatches(id: String)

    /** Normal, in-order draw primitive -- painted in call order. */
    fun emit(primitive: UiDrawPrimitive)

    /**
     * Always painted after every [emit]-ed primitive this frame, regardless of call order --
     * see [UiContext.endFrame]. Used by widgets whose content must never be covered by a
     * sibling drawn later in the same frame (e.g. an expanded dropdown's option list).
     */
    fun emitOverlay(primitive: UiDrawPrimitive)

    fun widgetState(id: String): WidgetState
}
