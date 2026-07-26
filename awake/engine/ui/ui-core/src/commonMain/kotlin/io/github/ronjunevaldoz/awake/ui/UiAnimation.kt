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
    // WrapContent/scroll trial-measurement passes now share the real state store (see
    // UiContextMeasureState.createMeasureContext) so stateful branches measure against the true
    // current value. animateFloat's step is a *side effect* though -- it advances the stored
    // value by frameDeltaSecondsInternal() on every call -- so letting a trial pass step it too
    // would double-advance the animation (once for the trial re-execution, once for the real
    // pass) each real frame. Guard it like every other side-effecting UiContext operation.
    if (isMeasuringInternal()) return current
    val next = animateFloatStep(current, target, frameDeltaSecondsInternal(), responsiveness, snapDistance)
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

/**
 * Fixed-duration, [Easing]-shaped tween -- distinct from the spring-style [animateFloat] above,
 * which has no fixed end time and just chases [target] forever. A caller feeds a stable [id] and
 * a target each frame; the animation runs for [durationMs] milliseconds shaped by [easing], the
 * same `tween(durationMillis, easing)` concept as Compose's `AnimationSpec`. Retargeting mid-tween
 * (changing [target] before the previous tween finished) restarts the duration from the current
 * animated value, matching Compose's `animateFloatAsState` behavior, rather than jumping back to
 * [initial].
 */
fun UiContext.animateFloatTween(
    id: String,
    target: Float,
    initial: Float = target,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing
): Float {
    val state = widgetStateInternal("__tween__$id")
    val currentValue = state.get("value", initial)
    val startValue = state.get("start", initial)
    val storedTarget = state.get("target", target)
    val elapsedMs = state.get("elapsed", 0f)

    // Same guard as animateFloat above: this step is a side effect, so trial-measurement passes
    // (which share the real state store, see UiContextMeasureState.createMeasureContext) must not
    // advance it a second time on top of the real frame's pass.
    if (isMeasuringInternal()) return currentValue

    val retargeted = target != storedTarget
    val effectiveStart = if (retargeted) currentValue else startValue
    val effectiveElapsed = if (retargeted) 0f else elapsedMs
    val nextElapsed = effectiveElapsed + frameDeltaSecondsInternal() * 1000f

    val next = animateFloatTweenStep(effectiveStart, target, nextElapsed, durationMs, easing)

    state.set("start", effectiveStart)
    state.set("target", target)
    state.set("elapsed", nextElapsed)
    state.set("value", next)
    return next
}

fun UiScope.animateFloatTween(
    id: String,
    target: Float,
    initial: Float = target,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing
): Float = context.animateFloatTween(id, target, initial, durationMs, easing)

internal fun animateFloatTweenStep(
    startValue: Float,
    target: Float,
    elapsedMs: Float,
    durationMs: Float,
    easing: Easing
): Float {
    if (durationMs <= 0f) return target
    val fraction = (elapsedMs / durationMs).coerceIn(0f, 1f)
    val eased = easing.transform(fraction)
    return startValue + (target - startValue) * eased
}
