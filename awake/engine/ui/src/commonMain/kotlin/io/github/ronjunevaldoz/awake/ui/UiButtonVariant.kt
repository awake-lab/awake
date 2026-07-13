// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * shadcn/ui's button variant vocabulary, scoped down to what a fill/border decision needs --
 * [Filled] (the only variant that existed before this) always paints its resolved [UiStyle]
 * color; [Outline]/[Ghost] paint no fill at rest, only on hover/active (so an idle Outline/
 * Ghost button reads as "just a border" / "just a label" the way shadcn's own CSS variants
 * do), and [Outline] additionally always draws a `theme.tokens.border` stroke regardless of
 * hover state. [buttonSlot] is the single place that interprets this -- a consumer widget
 * built on the same primitives ([UiScope.claimSlot]/[UiScope.emit]/[UiScope.border]) can
 * define its own variant vocabulary instead of this one; nothing else in the library assumes
 * these three exist.
 */
enum class UiButtonVariant {
    Filled,
    Outline,
    Ghost
}

/** Fully transparent -- [UiButtonVariant.Outline]/[Ghost]'s "no fill at rest" state. A zero-
 * alpha [UiDrawPrimitive.Quad]/[UiDrawPrimitive.RoundedQuad] is skipped entirely by
 * [buttonSlot] rather than emitted (no backend does alpha-culling itself), so this constant
 * only matters as the "should I skip the fill" check, never actually reaches a primitive. */
private val TRANSPARENT = floatArrayOf(0f, 0f, 0f, 0f)

internal fun UiButtonVariant.resolveFill(baseColor: FloatArray, hovered: Boolean, active: Boolean): FloatArray =
    when (this) {
        UiButtonVariant.Filled -> baseColor
        UiButtonVariant.Outline, UiButtonVariant.Ghost -> if (hovered || active) baseColor else TRANSPARENT
    }
