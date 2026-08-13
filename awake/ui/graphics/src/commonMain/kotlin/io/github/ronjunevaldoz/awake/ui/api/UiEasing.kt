// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api

/** Runtime-free mapping from an elapsed fraction to an eased animation fraction. */
fun interface Easing {
    fun transform(fraction: Float): Float
}

val LinearEasing = Easing { fraction -> fraction }
val EaseIn: Easing = CubicBezierEasing(0.42f, 0f, 1f, 1f)
val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)
val EaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/** CSS/Compose-compatible cubic-bezier easing curve. */
class CubicBezierEasing(
    private val x1: Float,
    private val y1: Float,
    private val x2: Float,
    private val y2: Float,
) : Easing {
    private val ax = 1f - 3f * x2 + 3f * x1
    private val bx = 3f * x2 - 6f * x1
    private val cx = 3f * x1
    private val ay = 1f - 3f * y2 + 3f * y1
    private val by = 3f * y2 - 6f * y1
    private val cy = 3f * y1

    private fun sampleCurveX(t: Float) = ((ax * t + bx) * t + cx) * t
    private fun sampleCurveY(t: Float) = ((ay * t + by) * t + cy) * t
    private fun sampleCurveDerivativeX(t: Float) = (3f * ax * t + 2f * bx) * t + cx

    private fun solveCurveX(x: Float): Float {
        if (x <= 0f) return 0f
        if (x >= 1f) return 1f
        var t = x
        repeat(8) {
            val difference = sampleCurveX(t) - x
            if (kotlin.math.abs(difference) < 1e-6f) return t
            val derivative = sampleCurveDerivativeX(t)
            if (kotlin.math.abs(derivative) >= 1e-6f) t -= difference / derivative
        }
        var low = 0f
        var high = 1f
        t = x
        while (low < high) {
            val difference = sampleCurveX(t) - x
            if (kotlin.math.abs(difference) < 1e-6f) return t
            if (x > sampleCurveX(t)) low = t else high = t
            t = (high + low) / 2f
        }
        return t
    }

    override fun transform(fraction: Float): Float = sampleCurveY(solveCurveX(fraction))
}
