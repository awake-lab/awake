// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * OKLCH color token with conversion to the engine's shared sRGB [Color] type.
 *
 * Primary color format for Tailwind CSS v4 design tokens.
 */
data class OklchColor(
    val lightness: Float,
    val chroma: Float,
    val hueDegrees: Float,
    val alpha: Float = 1f,
) {
    fun toSrgb(): Color {
        val hueRadians = hueDegrees.toDouble() * PI / 180.0
        val a = chroma * cos(hueRadians).toFloat()
        val b = chroma * sin(hueRadians).toFloat()

        val lPrime = lightness + 0.39633778f * a + 0.21580376f * b
        val mPrime = lightness - 0.105561346f * a - 0.06385417f * b
        val sPrime = lightness - 0.08948418f * a - 1.2914855f * b

        val l = lPrime.toDouble().pow(3.0)
        val m = mPrime.toDouble().pow(3.0)
        val s = sPrime.toDouble().pow(3.0)

        val redLinear = (+4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s).toFloat()
        val greenLinear = (-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s).toFloat()
        val blueLinear = (-0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s).toFloat()

        return Color(
            r = redLinear.toSrgbChannel(),
            g = greenLinear.toSrgbChannel(),
            b = blueLinear.toSrgbChannel(),
            a = alpha.coerceIn(0f, 1f).stableChannel(),
        )
    }
}

fun oklch(
    lightness: Float,
    chroma: Float,
    hueDegrees: Float = 0f,
    alpha: Float = 1f,
): Color = OklchColor(lightness, chroma, hueDegrees, alpha).toSrgb()

private fun Float.toSrgbChannel(): Float {
    val clamped = coerceIn(0f, 1f)
    return (
        if (clamped <= 0.0031308f) {
            12.92f * clamped
        } else {
            1.055f * clamped.toDouble().pow(1.0 / 2.4).toFloat() - 0.055f
        }.coerceIn(0f, 1f)
        ).stableChannel()
}

private fun Float.stableChannel(): Float = (this * 1_000_000f).roundToInt() / 1_000_000f
