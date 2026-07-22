// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import kotlin.math.abs
import kotlin.math.exp

/**
 * Tiny immediate-mode animation helper. A caller feeds a stable [id] and a target each frame;
 * the helper keeps the animated value inside [WidgetState] and eases toward the target using
 * the frame delta supplied to [io.github.ronjunevaldoz.awake.ui.context.UiContext.beginFrame].
 */
fun UiContext.animateFloat(
    id: String,
    target: Float,
    initial: Float = target,
    responsiveness: Float = 12f,
    snapDistance: Float = 0.001f
): Float {
    val state = widgetStateInternal("__animation__$id")
    val current = state.get("value", initial)
    val next = animateFloatStep(current, target, frameDeltaSeconds(), responsiveness, snapDistance)
    state.set("value", next)
    return next
}

fun UiScope.animateFloat(
    id: String,
    target: Float,
    initial: Float = target,
    responsiveness: Float = 12f,
    snapDistance: Float = 0.001f
): Float = context.animateFloat(id, target, initial, responsiveness, snapDistance)

internal fun animateFloatStep(
    current: Float,
    target: Float,
    deltaSeconds: Float,
    responsiveness: Float,
    snapDistance: Float = 0.001f
): Float {
    if (responsiveness <= 0f || deltaSeconds <= 0f) {
        return target
    }
    val t = 1f - exp(-responsiveness * deltaSeconds)
    val next = current + (target - current) * t
    return if (abs(target - next) <= snapDistance) target else next
}
