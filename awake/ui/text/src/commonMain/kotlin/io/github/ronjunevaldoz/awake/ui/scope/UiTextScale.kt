// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.scope

import kotlin.math.roundToInt

fun pixelPerfectTextScale(requestedScale: Float, step: Float = 0.25f): Float {
    val safeStep = step.takeIf { it.isFinite() && it > 0f } ?: 0.25f
    val snapped = (requestedScale / safeStep).roundToInt()
        .coerceAtLeast((1f / safeStep).roundToInt()) * safeStep
    return snapped.coerceAtLeast(1f)
}
