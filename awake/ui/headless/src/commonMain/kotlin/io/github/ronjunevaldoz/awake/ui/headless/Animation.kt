// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Easing
import io.github.ronjunevaldoz.awake.ui.api.LinearEasing
import io.github.ronjunevaldoz.awake.ui.animateFloatTween as primitiveAnimateFloatTween

/** Advances a stable-ID fixed-duration animation through the Headless facade. */
fun UiScope.animateFloatTween(
    id: String,
    target: Float,
    initial: Float = target,
    durationMs: Float = 300f,
    easing: Easing = LinearEasing,
): Float = primitive.primitiveAnimateFloatTween(id, target, initial, durationMs, easing)
