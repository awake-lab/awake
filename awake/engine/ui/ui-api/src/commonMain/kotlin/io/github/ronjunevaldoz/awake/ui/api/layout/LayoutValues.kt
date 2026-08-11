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

/** Authored padding/margin values. Density conversion belongs to ui-core. */
data class UiInsets(
    val start: Dp = Dp(0f),
    val top: Dp = Dp(0f),
    val end: Dp = Dp(0f),
    val bottom: Dp = Dp(0f),
) {
    companion object {
        val Zero = UiInsets()
    }
}

fun UiInsets(all: Dp): UiInsets = UiInsets(all, all, all, all)

fun UiInsets(horizontal: Dp, vertical: Dp): UiInsets = UiInsets(horizontal, vertical, horizontal, vertical)
