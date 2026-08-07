// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext

/**
 * Pushes [alpha] onto this context's graphics-layer alpha stack (see
 * [UiContext.pushGraphicsLayerAlphaInternal]), runs [block], then pops. Every primitive emitted
 * anywhere inside [block] -- including by nested composite widgets several calls deep -- has its
 * color's alpha channel multiplied by the effective stacked value at [UiContext]'s own
 * primitive-emission choke point, so no individual widget needs to know this layer exists. The
 * real application point for [io.github.ronjunevaldoz.awake.ui.modifier.UiAlphaEffect].
 */
fun <T> UiContext.withGraphicsLayerAlpha(alpha: Float, block: () -> T): T {
    pushGraphicsLayerAlphaInternal(alpha)
    try {
        return block()
    } finally {
        popGraphicsLayerAlphaInternal()
    }
}

fun <T> UiScope.withGraphicsLayerAlpha(alpha: Float, block: () -> T): T =
    context.withGraphicsLayerAlpha(alpha, block)

/**
 * Pushes a [UiPrimitiveTransform] onto this context's graphics-layer scale stack (see
 * [UiContext.pushGraphicsLayerScaleInternal]), runs [block], then pops. Every geometry-carrying
 * primitive emitted anywhere inside [block] carries the transform through to the backend's draw
 * call (see `UiDrawPrimitive.withTransform`). The real application point for
 * [io.github.ronjunevaldoz.awake.ui.modifier.UiScaleEffect] -- scale-only (no rotation), see
 * docs/tasks/2026-08-02-graphicslayer-rotation-scale.md.
 */
fun <T> UiContext.withGraphicsLayerScale(
    scaleX: Float,
    scaleY: Float = scaleX,
    pivotX: Float = 0f,
    pivotY: Float = 0f,
    block: () -> T,
): T {
    pushGraphicsLayerScaleInternal(UiPrimitiveTransform(scaleX, scaleY, pivotX, pivotY))
    try {
        return block()
    } finally {
        popGraphicsLayerScaleInternal()
    }
}

fun <T> UiScope.withGraphicsLayerScale(
    scaleX: Float,
    scaleY: Float = scaleX,
    pivotX: Float = 0f,
    pivotY: Float = 0f,
    block: () -> T,
): T = context.withGraphicsLayerScale(scaleX, scaleY, pivotX, pivotY, block)

/**
 * Real `AnimatedVisibility`-equivalent, alpha-only (matching this engine's current
 * `graphicsLayer` scope -- no enter/exit slide or scale). Unlike a plain `if (visible) content()`
 * (which unmounts instantly the frame [visible] flips false), this keeps calling [content] --
 * wrapped in a fading [withGraphicsLayerAlpha] layer -- until the exit tween actually settles at
 * zero, so a fade-out is a real fade, not a snap.
 *
 * Mirrors [animateFloatTween]'s call shape/state-id convention: a stable [id] keys the animated
 * value in [io.github.ronjunevaldoz.awake.ui.WidgetState], so re-triggering [visible] mid-fade
 * retargets smoothly from the current alpha (the same behavior [animateFloatTween] already
 * guarantees) rather than jumping.
 */
fun UiScope.animatedVisibility(
    id: String,
    visible: Boolean,
    durationMs: Float = 200f,
    easing: Easing = LinearEasing,
    content: UiScope.() -> Unit,
) {
    val alpha = animateFloatTween(
        id = "__animated_visibility__$id",
        target = if (visible) 1f else 0f,
        initial = if (visible) 1f else 0f,
        durationMs = durationMs,
        easing = easing,
    )
    // Keep rendering through the exit fade -- only actually stop calling content() once the tween
    // has settled at zero, so the subtree really unmounts (freeing up layout/hit-test cost)
    // instead of lingering invisible-but-still-composed forever.
    if (visible || alpha > 0.001f) {
        withGraphicsLayerAlpha(alpha) { content() }
    }
}
