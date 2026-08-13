// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api.layout

import io.github.ronjunevaldoz.awake.ui.api.Dp

/** Child placement intent inside a parent slot. Pixel placement belongs to ui-core. */
enum class UiAlignment {
    TopStart,
    TopCenter,
    TopEnd,
    CenterStart,
    Center,
    CenterEnd,
    BottomStart,
    BottomCenter,
    BottomEnd,
    ;

    /** Row cross-axis alignment. */
    enum class Vertical { Top, Center, Bottom }

    /** Column cross-axis alignment. */
    enum class Horizontal { Start, Center, End }

    companion object {
        /** Combines axis-specific alignment values into a full placement intent. */
        fun of(vertical: Vertical, horizontal: Horizontal): UiAlignment = when (vertical) {
            Vertical.Top -> when (horizontal) {
                Horizontal.Start -> TopStart
                Horizontal.Center -> TopCenter
                Horizontal.End -> TopEnd
            }
            Vertical.Center -> when (horizontal) {
                Horizontal.Start -> CenterStart
                Horizontal.Center -> Center
                Horizontal.End -> CenterEnd
            }
            Vertical.Bottom -> when (horizontal) {
                Horizontal.Start -> BottomStart
                Horizontal.Center -> BottomCenter
                Horizontal.End -> BottomEnd
            }
        }
    }
}

/** Converts a Tailwind grid multiplier to Dp (1 unit = 4dp). e.g., 2.5.tw = 10.dp, 0.5.tw = 2.dp */
val Number.tw: Dp get() = Dp(this.toFloat() * 4f)

/** Authored padding/margin values. Density conversion belongs to ui-core. */
data class UiInsets(
    val start: Dp = Dp(0f),
    val top: Dp = Dp(0f),
    val end: Dp = Dp(0f),
    val bottom: Dp = Dp(0f),
) {
    companion object {
        val Zero = UiInsets()

        /** Tailwind padding constructor (1 unit = 4dp). e.g., UiInsets.tw(px = 2.5, py = 0.5) */
        fun tw(px: Number = 0, py: Number = 0): UiInsets = UiInsets(
            horizontal = px.tw,
            vertical = py.tw,
        )

        fun tw(p: Number): UiInsets = UiInsets(all = p.tw)
    }
}

fun UiInsets(all: Dp): UiInsets = UiInsets(all, all, all, all)

fun UiInsets(horizontal: Dp, vertical: Dp): UiInsets = UiInsets(horizontal, vertical, horizontal, vertical)

/** Snaps a coordinate to the nearest integer pixel to avoid subpixel rendering blur. */
fun pixelPerfectPixel(value: Float): Float = kotlin.math.round(value)

