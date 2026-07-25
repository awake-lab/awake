// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.px

/**
 * A widget's sizing intent -- replaces the undocumented "pass `0f` and it silently fills the
 * enclosing scope's configured width/height" convention that used to live inside
 * [io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope]/[io.github.ronjunevaldoz.awake.ui.layouts.RowScope]'s own `claimSlot`. [WrapContent] is intentionally reserved for
 * composite containers that can measure their own child content before they claim a real slot
 * (today that means higher-level surfaces such as `panel`). Leaf widgets still resolve to a
 * concrete size immediately.
 */
sealed class Dimension {
    data class Fixed(val dp: Dp) : Dimension()

    /** Fills whatever width ([io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope]/[io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope]/[io.github.ronjunevaldoz.awake.ui.layouts.BoxScope]) or height ([io.github.ronjunevaldoz.awake.ui.layouts.RowScope])
     * the enclosing scope is configured with -- the same behavior a `0f` hint silently
     * triggered before, now a real, named, optional choice instead of a sentinel. */
    object FillMax : Dimension()

    /** Sizes a composite container to the space its child content actually uses. */
    object WrapContent : Dimension()
}

/**
 * Proportional main-axis sizing for a [io.github.ronjunevaldoz.awake.ui.layouts.RowScope]/[io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope]
 * child -- mirrors Jetpack Compose's `Modifier.weight(weight, fill)`. [weight] is only
 * meaningful relative to sibling weights within the same row/column; it has no effect on
 * [io.github.ronjunevaldoz.awake.ui.layouts.BoxScope]/[io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope]. [fill] mirrors Compose: `true`
 * (default) forces the child to occupy its full proportional share; `false` caps the child's
 * own requested main-axis size at that share instead of forcing it to expand.
 *
 * Deliberately not a [Dimension] variant -- [io.github.ronjunevaldoz.awake.ui.modifier.UiModifier.weight] is a single
 * axis-agnostic builder function (Row/Column-scoped `Modifier.weight` would collide with the
 * same compiler chained-call bug documented on [io.github.ronjunevaldoz.awake.ui.modifier.UiModifier]), so which axis
 * (width for Row, height for Column) it applies to is resolved by the row/column implementation,
 * not encoded in the value itself.
 */
data class LayoutWeight(val weight: Float, val fill: Boolean = true)

/** Preserves the historical "pass `0f` (or negative) and it fills the enclosing scope" call
 * pattern this codebase's own widgets (and sample app) used before [Dimension] existed --
 * public, not file-private to the built-in widget implementations, so a consumer's own custom widget (e.g. `Gauge.kt`)
 * gets the exact same convenience a built-in widget does, matching this module's "no
 * capability gap versus a built-in widget" guarantee. */
fun Float.toDimension(): Dimension = if (this > 0f) Dimension.Fixed(this.px) else Dimension.FillMax
fun Dp.toDimension(): Dimension = if (value > 0f) Dimension.Fixed(this) else Dimension.FillMax
