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
    snapDistance: Float = 0.001f,
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
    snapDistance: Float = 0.001f,
): Float = context.animateFloat(id, target, initial, responsiveness, snapDistance)

/** Frame-time spikes (a GC pause, a dropped frame, a window resize) feed a large [deltaSeconds]
 * into the exponential-decay factor below -- e.g. `responsiveness=8`, a normal 60fps frame gives
 * `t≈0.13`, but a single 0.3s stall gives `t≈0.91`, jumping the animated value most of the way
 * to [target] in one step, which reads as a visible snap. Real game loops clamp per-step delta
 * for exactly this reason; every test in this codebase drives a fixed 1/60f delta, so this path
 * was never exercised despite it being the likely explanation for a live-only "snapping"
 * report. Clamping here (not globally in the frame loop) keeps the fix scoped to spring-style
 * animation without touching physics/other real-delta consumers. */
private const val MAX_ANIMATE_FLOAT_DELTA_SECONDS = 1f / 20f

internal fun animateFloatStep(
    current: Float,
    target: Float,
    deltaSeconds: Float,
    responsiveness: Float,
    snapDistance: Float = 0.001f,
): Float {
    if (responsiveness <= 0f || deltaSeconds <= 0f) {
        return target
    }
    val clampedDeltaSeconds = deltaSeconds.coerceAtMost(MAX_ANIMATE_FLOAT_DELTA_SECONDS)
    val t = 1f - exp(-responsiveness * clampedDeltaSeconds)
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
    easing: Easing = LinearEasing,
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
    easing: Easing = LinearEasing,
): Float = context.animateFloatTween(id, target, initial, durationMs, easing)

internal fun animateFloatTweenStep(
    startValue: Float,
    target: Float,
    elapsedMs: Float,
    durationMs: Float,
    easing: Easing,
): Float {
    if (durationMs <= 0f) return target
    val fraction = (elapsedMs / durationMs).coerceIn(0f, 1f)
    val eased = easing.transform(fraction)
    return startValue + (target - startValue) * eased
}

/**
 * How [animateFloatRepeatable] plays each subsequent cycle after the first, matching Compose's
 * `RepeatMode`.
 */
enum class RepeatMode {
    /** Jump back to [animateFloatRepeatable]'s `initialValue` and play forward again. */
    Restart,

    /** Play the previous cycle backward -- a continuous ping-pong with no jump. */
    Reverse,
}

/**
 * Repeat-wrapped [animateFloatTween] -- separates the tween SPEC (`durationMs` + [easing]) from a
 * repeat policy ([repeatMode], [iterations]), the same split as Compose's `repeatable`/
 * `infiniteRepeatable` wrapping a `tween` `AnimationSpec`. `iterations = Int.MAX_VALUE` (the
 * default) means "repeat forever"; there's no dedicated infinite/finite sealed type because a
 * sentinel int is the same one Compose itself uses (`AnimationConstants.Infinite`) and keeps this
 * a single flat parameter instead of an extra type callers have to construct.
 *
 * Unlike [animateFloatTween]'s reactive per-frame stepping, the progress here is computed in
 * closed form from a monotonically increasing elapsed-time accumulator: `cycleIndex` and
 * `cycleElapsedMs` fall out of `elapsedMs / durationMs`, and [RepeatMode.Reverse] mirrors the
 * fraction on odd cycles (`1f - cycleFraction`). Nothing observes the *output* value to decide
 * when to turn around, so there's no one-frame-late flip and no stutter at the turnaround --
 * the bug the naive "watch the fraction, flip target when it settles" approach had.
 */
fun UiContext.animateFloatRepeatable(
    id: String,
    initialValue: Float,
    targetValue: Float,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing,
    repeatMode: RepeatMode = RepeatMode.Restart,
    iterations: Int = Int.MAX_VALUE,
): Float {
    val state = widgetStateInternal("__repeatable__$id")
    val elapsedMs = state.get("elapsed", 0f)

    // Same guard as animateFloat/animateFloatTween above: advancing the elapsed-time accumulator
    // is a side effect, so a trial-measurement pass (sharing the real state store, see
    // UiContextMeasureState.createMeasureContext) must not advance it a second time on top of the
    // real frame's pass -- this exact bug class was already found and fixed for shimmer (85f14821).
    if (isMeasuringInternal()) {
        return animateFloatRepeatableStep(
            initialValue,
            targetValue,
            elapsedMs,
            durationMs,
            easing,
            repeatMode,
            iterations,
        )
    }

    val nextElapsed = elapsedMs + frameDeltaSecondsInternal() * 1000f
    state.set("elapsed", nextElapsed)
    return animateFloatRepeatableStep(
        initialValue,
        targetValue,
        nextElapsed,
        durationMs,
        easing,
        repeatMode,
        iterations,
    )
}

fun UiScope.animateFloatRepeatable(
    id: String,
    initialValue: Float,
    targetValue: Float,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing,
    repeatMode: RepeatMode = RepeatMode.Restart,
    iterations: Int = Int.MAX_VALUE,
): Float = context.animateFloatRepeatable(id, initialValue, targetValue, durationMs, easing, repeatMode, iterations)

internal fun animateFloatRepeatableStep(
    startValue: Float,
    target: Float,
    elapsedMs: Float,
    durationMs: Float,
    easing: Easing,
    repeatMode: RepeatMode,
    iterations: Int,
): Float {
    if (durationMs <= 0f) return target

    val rawCycleIndex = (elapsedMs / durationMs).toInt()
    val finished = iterations != Int.MAX_VALUE && rawCycleIndex >= iterations
    // Once a finite iteration count is exhausted, hold at the last permitted cycle's end value
    // instead of continuing to wrap into cycles that were never allowed to happen.
    val cycleIndex = if (finished) iterations - 1 else rawCycleIndex
    val cycleElapsedMs = if (finished) durationMs else elapsedMs - cycleIndex * durationMs

    val cycleFraction = (cycleElapsedMs / durationMs).coerceIn(0f, 1f)
    val reversed = repeatMode == RepeatMode.Reverse && cycleIndex % 2 == 1
    val rawFraction = if (reversed) 1f - cycleFraction else cycleFraction

    val eased = easing.transform(rawFraction)
    return startValue + (target - startValue) * eased
}
