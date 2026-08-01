// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.jvm.JvmInline

/**
 * Device-independent unit -- converted to pixels once, at the point a size is claimed, via
 * [UiDensity]. Never appears past that boundary: [io.github.ronjunevaldoz.awake.ui.layout.UiBounds]/[UiDrawPrimitive]/[Layout.kt]'s
 * cursor math stay raw pixel [Float], the same contract every backend already consumes.
 */
@JvmInline
value class Dp(val value: Float) {
    operator fun plus(other: Dp): Dp = Dp(value + other.value)
    operator fun minus(other: Dp): Dp = Dp(value - other.value)
    operator fun times(other: Float): Dp = Dp(value * other)
    operator fun div(other: Float): Dp = Dp(value / other)
}

@JvmInline
value class Sp(val value: Float)

val Float.dp: Dp get() = Dp(this)
val Int.dp: Dp get() = Dp(this.toFloat())
val Float.sp: Sp get() = Sp(this)
val Int.sp: Sp get() = Sp(this.toFloat())

/** Wraps an already-resolved pixel value as a [Dp] that round-trips back to the exact same
 * pixel count via [toPx], regardless of [UiDensity.scale] -- used at the boundary where an
 * existing widget's own literal-pixel parameter (e.g. `button(id, width: Float, ...)`) enters
 * the [io.github.ronjunevaldoz.awake.ui.modifier.Dimension]-based `claimSlot` pipeline without being retroactively density-scaled. Not
 * the same as [dp]: `.dp` means "this many density-independent units, scale up by density";
 * `.px` means "this many pixels already, cancel density out so it's unchanged." */
val Float.px: Dp get() = Dp(this / UiDensity.scale)

/**
 * One scale factor (px per dp), set once by the platform layer from the real device's
 * backing-scale/DPI -- each backend already queries this for its own swapchain sizing, this
 * reuses that number rather than inventing a second source of truth. Mirrors kool-engine's
 * `UiScale`.
 */
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
