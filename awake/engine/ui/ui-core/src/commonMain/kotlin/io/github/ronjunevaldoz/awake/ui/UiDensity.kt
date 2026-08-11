// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/** Wraps an already-resolved pixel value as a [Dp] that round-trips through [toPx]. */
val Float.px: Dp get() = Dp(this / UiDensity.scale)

/** Runtime density configuration and authored-unit conversion. */
object UiDensity {
    var scale: Float = 1f
        set(value) {
            field = if (value.isFinite() && value > 0f) value else 1f
        }

    var fontScale: Float = 1f
        set(value) {
            field = if (value.isFinite() && value > 0f) value else 1f
        }

    fun pxToDp(pixels: Float): Float = pixels / scale
    fun pxToSp(pixels: Float): Float = pixels / (scale * fontScale)
}

fun Dp.toPx(): Float = value * UiDensity.scale
fun Sp.toPx(): Float = value * UiDensity.scale * UiDensity.fontScale
