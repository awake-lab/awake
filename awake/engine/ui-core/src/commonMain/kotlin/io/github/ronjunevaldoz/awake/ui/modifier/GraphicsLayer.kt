// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

/**
 * A render-decoration hook a base component's draw pass can react to -- the neutral contract
 * equivalent of Jetpack Compose's `Modifier.graphicsLayer`. Effects compose through
 * [UiGraphicsLayer] the same way visual rules compose through [io.github.ronjunevaldoz.awake.ui.style.Style],
 * instead of each effect getting its own dedicated boolean field on [UiModifier].
 *
 * `ui-core` only owns this contract and does not know how to draw any effect; concrete effects
 * (e.g. a shimmer sweep) are shadcn-compose-style extensions layered on top -- see
 * `StyleModifiers.kt`'s `shadcnShimmer` -- and base components in `ui-unstyled` interpret them
 * during their own glyph/fill emission. This must not be confused with `Canvas`/`CanvasScope`,
 * which is an app-level escape hatch, not a base-component implementation path.
 */
interface UiGraphicsEffect

/** The set of [UiGraphicsEffect]s attached to a [UiModifier]. */
data class UiGraphicsLayer(val effects: List<UiGraphicsEffect> = emptyList()) {
    infix fun then(other: UiGraphicsLayer): UiGraphicsLayer = UiGraphicsLayer(effects + other.effects)

    inline fun <reified T : UiGraphicsEffect> has(): Boolean = effects.any { it is T }

    inline fun <reified T : UiGraphicsEffect> without(): UiGraphicsLayer =
        UiGraphicsLayer(effects.filterNot { it is T })

    companion object {
        val Empty = UiGraphicsLayer()
    }
}

/** Attaches [effect] to this modifier's graphics layer, composing with any effects already present. */
fun UiModifier.graphicsLayer(effect: UiGraphicsEffect): UiModifier =
    copy(graphicsLayer = (graphicsLayer ?: UiGraphicsLayer.Empty) then UiGraphicsLayer(listOf(effect)))
