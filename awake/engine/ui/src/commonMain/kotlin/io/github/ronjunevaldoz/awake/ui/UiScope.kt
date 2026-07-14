// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.math.roundToInt

data class UiSlot(val x: Float, val y: Float, val width: Float, val height: Float)

/** Clamps this rect to the region it shares with [other] -- zero-size (not negative) if they
 * don't overlap at all. Pure function, used by [UiContext]'s clip stack to resolve nested
 * `clip { }` calls to a single already-intersected rect before it ever reaches a backend. */
fun UiSlot.intersect(other: UiSlot): UiSlot {
    val left = maxOf(x, other.x)
    val top = maxOf(y, other.y)
    val right = minOf(x + width, other.x + other.width)
    val bottom = minOf(y + height, other.y + other.height)
    return UiSlot(left, top, (right - left).coerceAtLeast(0f), (bottom - top).coerceAtLeast(0f))
}

fun pixelPerfectTextScale(requestedScale: Float): Float = requestedScale.roundToInt().coerceAtLeast(1).toFloat()

fun UiScope.resolvedTextScale(): Float = pixelPerfectTextScale(textScale)

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
     * to `theme.tokens.neutralStyle()` when no per-call [UiStyle] override is given, so
     * swapping a whole panel's look is one assignment at the scope's creation, not a change
     * to every widget call site.
     */
    val theme: UiTheme

    /**
     * Requested multiplier applied to every glyph this scope draws (quad size, pen advance,
     * label-row height). [font]'s atlas stays a fixed 8x8 cell regardless (see
     * [io.github.ronjunevaldoz.awake.ui.font.BitmapFont]'s doc comment: a hand-authored debug
     * font, not a scalable typeface) -- this just draws each glyph's quad bigger
     * (nearest-neighbor blocky scaling, matching the font's own retro-bitmap look), it doesn't
     * re-rasterize anything. Rendering snaps this to the nearest whole-number multiple via
     * [resolvedTextScale] so bitmap glyphs stay pixel-stable instead of shimmering at
     * fractional scales. Defaults to `1f` (today's original, un-scaled size).
     */
    val textScale: Float

    /**
     * Direct reference to the owning context -- mirrors kool-engine's `UiScope.surface`. Lets
     * a composite widget (e.g. [panel]) build a nested scope from the SAME public factories
     * ([UiContext.column]/[row]/[box]/[absolute]) every top-level caller already uses,
     * instead of a bespoke nesting primitive.
     */
    val context: UiContext

    /**
     * Reserves the next layout position for a widget of the given size and returns its
     * resolved screen-space rect. What "next position" means is entirely up to the
     * implementing scope -- [ColumnScope] advances a Y cursor, [AbsoluteScope] ignores
     * width/height and returns the exact x/y it was constructed with.
     */
    fun claimSlot(width: Dimension, height: Dimension): UiSlot

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
