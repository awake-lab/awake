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

/**
 * Real alpha-compositing effect -- unlike [UiShimmerEffect] (a zero-property marker a widget
 * interprets itself), this actually carries the value a draw pass needs. Applied once, centrally,
 * at [io.github.ronjunevaldoz.awake.ui.context.UiContext]'s own primitive-emission choke point
 * (see `UiContext.emitInternal`/`UiDrawPrimitive.scaledByAlpha`), which multiplies every emitted
 * primitive's color alpha channel by the currently active stacked value -- so no individual widget
 * needs to know this effect exists, unlike shimmer. This is a CPU-side per-primitive alpha
 * pre-multiply, not a real offscreen-buffer-then-blend compositing pass (see
 * [io.github.ronjunevaldoz.awake.ui.context.UiContext.pushGraphicsLayerAlphaInternal]'s doc) --
 * correct for fading a subtree in/out, not for correctly blending overlapping semi-transparent
 * content *within* one faded subtree. Alpha-only: rotation/scale are not implemented (see
 * docs/reference/MIRROR_MAP.md).
 */
data class UiAlphaEffect(val alpha: Float) : UiGraphicsEffect

/** The effective alpha this graphics layer would apply -- the product of every [UiAlphaEffect]
 * present (nested/stacked alpha effects compose by multiplying, matching real compositing), or
 * `1f` (fully opaque, no-op) if none is present. */
val UiGraphicsLayer.effectiveAlpha: Float
    get() = effects.filterIsInstance<UiAlphaEffect>().fold(1f) { acc, effect -> acc * effect.alpha }

/** Attaches a [UiAlphaEffect] with [alpha] (clamped 0f..1f) to this modifier's graphics layer. */
fun UiModifier.alpha(alpha: Float): UiModifier = graphicsLayer(UiAlphaEffect(alpha.coerceIn(0f, 1f)))
