// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Maps a fraction of elapsed animation time in `[0, 1]` to an eased fraction, also in `[0, 1]`,
 * used by [animateFloatTween] to shape a fixed-duration animation. Mirrors Compose's `Easing`
 * fun-interface (and this codebase's existing house convention for single-method callback types,
 * see [UiPopupPositionProvider]).
 */
fun interface Easing {
    fun transform(fraction: Float): Float
}

/** No easing -- constant rate of change from start to end. Matches CSS `linear`. */
val LinearEasing = Easing { fraction -> fraction }

/** Starts slow, ends fast. Matches CSS `ease-in` (cubic-bezier(0.42, 0, 1, 1)). */
val EaseIn: Easing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

/** Starts fast, ends slow. Matches CSS `ease-out` (cubic-bezier(0, 0, 0.58, 1)). */
val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/** Starts slow, speeds up, ends slow. Matches CSS `ease-in-out` (cubic-bezier(0.42, 0, 0.58, 1)). */
val EaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/**
 * Cubic-bezier easing curve defined by control points (x1, y1) and (x2, y2), with the curve's
 * start and end anchored at (0, 0) and (1, 1) -- the same parameterization as CSS
 * `cubic-bezier()` and Compose's `CubicBezierEasing`. Solves for the bezier's `t` parameter at a
 * given `x` (the input fraction) via Newton-Raphson with a bisection fallback, then evaluates `y`
 * at that `t` -- the standard approach browsers use for CSS transition-timing-function (see
 * WebKit's UnitBezier), accurate enough for a UI easing curve without needing a lookup table.
 */
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
            val x2Guess = sampleCurveX(t) - x
            if (kotlin.math.abs(x2Guess) < 1e-6f) return t
            val derivative = sampleCurveDerivativeX(t)
            if (kotlin.math.abs(derivative) < 1e-6f) return@repeat
            t -= x2Guess / derivative
        }

        var lo = 0f
        var hi = 1f
        t = x
        while (lo < hi) {
            val guess = sampleCurveX(t)
            if (kotlin.math.abs(guess - x) < 1e-6f) return t
            if (x > guess) lo = t else hi = t
            t = (hi + lo) / 2f
        }
        return t
    }

    override fun transform(fraction: Float): Float = sampleCurveY(solveCurveX(fraction))
}
