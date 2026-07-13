// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * What a widget's appearance may depend on -- deliberately just the two states every
 * existing widget (button/toggle/slider) already keys its color off of today. Extend with
 * more fields (e.g. `disabled`) only when a real widget needs them.
 */
data class UiWidgetState(val hovered: Boolean, val active: Boolean)

/**
 * A swappable color policy for one widget kind. This is the Style-API seam: any consumer
 * can supply a completely different [UiStyle] with zero change to `Widgets.kt` or the
 * widget call site's logic -- same role Compose's `Style` interface plays relative to a
 * component's modifier chain, scoped down to color (no padding/border/animation state --
 * this UI has no per-frame recomposition to animate across).
 */
fun interface UiStyle {
    fun colorFor(state: UiWidgetState): FloatArray
}
