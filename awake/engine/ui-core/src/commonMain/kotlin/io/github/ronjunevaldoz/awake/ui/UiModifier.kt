// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * A widget's sizing intent -- replaces the undocumented "pass `0f` and it silently fills the
 * enclosing scope's configured width/height" convention that used to live inside
 * [ColumnScope]/[RowScope]'s own `claimSlot`. [WrapContent] is intentionally reserved for
 * composite containers that can measure their own child content before they claim a real slot
 * (today that means higher-level surfaces such as `panel`). Leaf widgets still resolve to a
 * concrete size immediately.
 */
sealed class Dimension {
    data class Fixed(val dp: Dp) : Dimension()

    /** Fills whatever width ([ColumnScope]/[AbsoluteScope]/[BoxScope]) or height ([RowScope])
     * the enclosing scope is configured with -- the same behavior a `0f` hint silently
     * triggered before, now a real, named, optional choice instead of a sentinel. */
    object FillMax : Dimension()

    /** Sizes a composite container to the space its child content actually uses. */
    object WrapContent : Dimension()
}

/** Preserves the historical "pass `0f` (or negative) and it fills the enclosing scope" call
 * pattern this codebase's own widgets (and sample app) used before [Dimension] existed --
 * public, not file-private to the built-in widget implementations, so a consumer's own custom widget (e.g. `Gauge.kt`)
 * gets the exact same convenience a built-in widget does, matching this module's "no
 * capability gap versus a built-in widget" guarantee. */
fun Float.toDimension(): Dimension = if (this > 0f) Dimension.Fixed(this.px) else Dimension.FillMax

/**
 * Per-widget-call structural override only -- width/height are layout concerns, while fill,
 * shape, border, text scale, and padding now belong to [Style]. That keeps this type closer
 * to real Compose's "modifier is structure/behavior, style is visuals" split, which matters
 * once consumer-authored widgets and composite containers start reusing the same style stack
 * as built-ins.
 */
data class UiModifier(
    val width: Dimension? = null,
    val height: Dimension? = null,
    val alignment: UiAlignment? = null,
    val offsetX: Dp = UiShape.none,
    val offsetY: Dp = UiShape.none,
    val insets: UiInsets = UiInsets.Zero
)

fun UiModifier.width(dp: Dp): UiModifier = copy(width = Dimension.Fixed(dp))
fun UiModifier.height(dp: Dp): UiModifier = copy(height = Dimension.Fixed(dp))
fun UiModifier.size(width: Dp, height: Dp): UiModifier = copy(width = Dimension.Fixed(width), height = Dimension.Fixed(height))
fun UiModifier.fillMaxWidth(): UiModifier = copy(width = Dimension.FillMax)
fun UiModifier.fillMaxHeight(): UiModifier = copy(height = Dimension.FillMax)
fun UiModifier.align(alignment: UiAlignment): UiModifier = copy(alignment = alignment)
fun UiModifier.offset(x: Dp = UiShape.none, y: Dp = UiShape.none): UiModifier = copy(offsetX = x, offsetY = y)
fun UiModifier.padding(all: Dp): UiModifier = copy(insets = UiInsets(all))
fun UiModifier.padding(horizontal: Dp, vertical: Dp): UiModifier = copy(insets = UiInsets(horizontal, vertical))
fun UiModifier.padding(start: Dp, top: Dp, end: Dp, bottom: Dp): UiModifier =
    copy(insets = UiInsets(start, top, end, bottom))
