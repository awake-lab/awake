// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext

/**
 * `updateTransition`-equivalent, scoped down to what's honestly buildable as a thin layer over
 * [animateFloatTween] rather than a new state-machine type: a stable [id]-keyed 0f-1f progress
 * value that moves from 0f (the first [targetState] this id ever saw) to 1f (any other
 * [targetState]) and back, sharing one lifecycle for however many properties a caller derives
 * from it.
 *
 * Unlike Compose's `updateTransition`, this does not offer per-property child calls
 * (`transition.animateFloat { ... }`) that each register their own spec against the same
 * coordinator -- that ergonomics layer needs real multi-child bookkeeping this id-per-value
 * state store doesn't cleanly support yet. Instead: call this once per frame per [id], then
 * derive as many properties as needed from the single returned progress with plain lerp math
 * (`from + (to - from) * progress`), the same way [animatedVisibility] derives graphics-layer
 * alpha from a single tween today. Calling this more than once per frame with the same [id]
 * double-steps the underlying tween, same caveat as calling [animateFloatTween] twice per frame
 * with the same id -- not a new failure mode, just inherited.
 *
 * No-ops during a trial-measurement pass "for free": this function has no side-effecting state
 * of its own (the first-seen-state anchor is written via [io.github.ronjunevaldoz.awake.ui.WidgetState]'s
 * `getOrPut`, so re-reading it during a trial pass never changes it), and all per-frame stepping
 * is delegated to [animateFloatTween], which already carries the `isMeasuringInternal()` guard
 * documented on it.
 */
fun UiContext.rememberTransition(
    id: String,
    targetState: Any?,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing
): Float {
    val state = widgetStateInternal("__transition_anchor__$id")
    val anchorState = state.get("anchor", targetState)
    val target = if (targetState == anchorState) 0f else 1f
    return animateFloatTween(id = "__transition__$id", target = target, initial = target, durationMs = durationMs, easing = easing)
}

fun UiScope.rememberTransition(
    id: String,
    targetState: Any?,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing
): Float = context.rememberTransition(id, targetState, durationMs, easing)
