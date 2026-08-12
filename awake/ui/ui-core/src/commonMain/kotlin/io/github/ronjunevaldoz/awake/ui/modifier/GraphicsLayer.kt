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
 * `StyleModifiers.kt`'s `shadcnShimmer` -- and base components in `ui-headless` interpret them
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

/**
 * Real GPU-applied scale effect -- see `docs/tasks/2026-08-02-graphicslayer-rotation-scale.md`
 * for the full design (scale-only first step of that doc's rotation/scale tier; rotation is
 * explicitly out of scope). Unlike [UiAlphaEffect] (resolved entirely CPU-side by multiplying
 * a color channel every primitive already carries), scale changes geometry, so it can't be
 * folded into `UiDrawPrimitive`'s existing fields the same way -- it survives all the way to
 * the backend as [io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform] on the primitive
 * itself, applied in the vertex shader at draw time (see `ui_quad.vert`/`.wgsl` and siblings).
 * [pivotX]/[pivotY] are in the same pixel-space coordinates as the primitive's own `x`/`y` --
 * scaling happens around that point, not the primitive's own origin, so a pivot at a widget's
 * center (`x + w / 2`, `y + h / 2`) gives the usual "grow from the middle" hover/press effect.
 *
 * Known gap (see design doc): the active clip region does NOT transform with scaled content --
 * clip/scissor rects stay axis-aligned in pre-transform space, so scaled content can visually
 * overflow its own clip rect. Deferred, not a bug.
 */
data class UiScaleEffect(
    val scaleX: Float,
    val scaleY: Float,
    val pivotX: Float = 0f,
    val pivotY: Float = 0f,
) : UiGraphicsEffect

/** Attaches a [UiScaleEffect] to this modifier's graphics layer -- [scaleY] defaults to
 * [scaleX] for the common uniform-scale case. */
fun UiModifier.scale(scaleX: Float, scaleY: Float = scaleX, pivotX: Float = 0f, pivotY: Float = 0f): UiModifier =
    graphicsLayer(UiScaleEffect(scaleX, scaleY, pivotX, pivotY))
