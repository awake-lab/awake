// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/** Pure value-from-pointer-position math for the built-in `slider`. */
fun sliderValueFromPointerX(pointerX: Float, trackX: Float, trackW: Float, min: Float, max: Float): Float {
    if (trackW <= 0f) return min
    val fraction = ((pointerX - trackX) / trackW).coerceIn(0f, 1f)
    return min + fraction * (max - min)
}
