// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.*

/**
 * The full set of primitives any widget -- built-in or consumer-defined -- is built from.
 * Nothing here knows about buttons, toggles, or any specific widget shape; the library's own
 * button/toggle/slider/dropdown are just the library's own extension functions written
 * against this same public surface. A consumer writes a custom widget the identical way:
 * `fun UiScope.myWidget(...) { val slot = claimSlot(...); ... }` -- no library change, no
 * capability gap versus a built-in widget.
 */
@AwakeUiDsl
interface UiScope {
    /**
     * Whether this scope's own [emit] routes to the overlay layer (painted after every
     * regular primitive this frame, regardless of call order -- see
     * [io.github.ronjunevaldoz.awake.ui.context.UiContext.endFrame]/[emitOverlay]). A composite widget that opens a **new** nested
     * scope to draw part of its own content must pass this through explicitly.
     */
    val emitsToOverlay: Boolean

    /**
     * Direct reference to the owning context -- mirrors kool-engine's `UiScope.surface`. Lets
     * a composite widget build a nested scope from the same public factories every top-level
     * caller already uses, instead of a bespoke nesting primitive.
     */
    val context: UiContext

    /**
     * Reserves the next layout position for a widget of the given size and returns its
     * resolved screen-space rect. [weight] is only honored by [io.github.ronjunevaldoz.awake.ui.layouts.RowScope]/
     * [io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope] (see [io.github.ronjunevaldoz.awake.ui.layout.LayoutWeight]); every other scope ignores it.
     */
    fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight? = null): UiBounds

    fun hitTest(slot: UiBounds): Boolean
    fun isActive(id: String): Boolean
    fun tryClaimActive(id: String, hovered: Boolean)
    fun releaseActiveIfMatches(id: String)

    /** Normal, in-order draw primitive -- painted in call order. */
    fun emit(primitive: UiDrawPrimitive)

    /**
     * Always painted after every [emit]-ed primitive this frame, regardless of call order --
     * see [UiContext.endFrame].
     */
    fun emitOverlay(primitive: UiDrawPrimitive)

    fun widgetState(id: String): WidgetState
}
