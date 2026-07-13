// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * A widget's sizing intent -- replaces the undocumented "pass `0f` and it silently fills the
 * enclosing scope's configured width/height" convention that used to live inside
 * [ColumnScope]/[RowScope]'s own `claimSlot`. [WrapContent] (size-to-content) deliberately
 * does not exist here: this immediate-mode architecture claims a slot in one pass, and sizing
 * to content would need a separate measure pass before the draw pass, which doesn't exist.
 * `text()`'s own char-count * cellSize math is this codebase's existing workaround for the one
 * case (labels) that actually needs it -- not generalized here.
 */
sealed class Dimension {
    data class Fixed(val dp: Dp) : Dimension()

    /** Fills whatever width ([ColumnScope]/[AbsoluteScope]/[BoxScope]) or height ([RowScope])
     * the enclosing scope is configured with -- the same behavior a `0f` hint silently
     * triggered before, now a real, named, optional choice instead of a sentinel. */
    object FillMax : Dimension()
}

/** Preserves the historical "pass `0f` (or negative) and it fills the enclosing scope" call
 * pattern this codebase's own widgets (and sample app) used before [Dimension] existed --
 * public, not file-private to `Widgets.kt`, so a consumer's own custom widget (e.g. `Gauge.kt`)
 * gets the exact same convenience a built-in widget does, matching this module's "no
 * capability gap versus a built-in widget" guarantee. */
fun Float.toDimension(): Dimension = if (this > 0f) Dimension.Fixed(this.px) else Dimension.FillMax

/**
 * Per-widget-call size override -- `null` means "use whatever the widget's own required
 * width/height param provides". Deliberately minimal: `align`/`padding`/`background` are real
 * extension points this shape supports, but nothing in this repo needs them yet.
 */
data class UiModifier(val width: Dimension? = null, val height: Dimension? = null)

fun UiModifier.width(dp: Dp): UiModifier = copy(width = Dimension.Fixed(dp))
fun UiModifier.height(dp: Dp): UiModifier = copy(height = Dimension.Fixed(dp))
fun UiModifier.size(width: Dp, height: Dp): UiModifier = copy(width = Dimension.Fixed(width), height = Dimension.Fixed(height))
fun UiModifier.fillMaxWidth(): UiModifier = copy(width = Dimension.FillMax)
fun UiModifier.fillMaxHeight(): UiModifier = copy(height = Dimension.FillMax)
