// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

/**
 * Child placement inside a parent slot. This is the Box-style alignment API; unlike
 * [io.github.ronjunevaldoz.awake.ui.UiAnchor], which is a shell-placement helper for screen
 * corners, [UiAlignment] is about how content sits INSIDE an already-claimed container.
 */
enum class UiAlignment {
    TopStart,
    TopCenter,
    TopEnd,
    CenterStart,
    Center,
    CenterEnd,
    BottomStart,
    BottomCenter,
    BottomEnd;

    /** Cross-axis-only vertical component, matching Jetpack Compose's `Alignment.Vertical` --
     * this is what [io.github.ronjunevaldoz.awake.ui.layouts.Row]'s container-level
     * `verticalAlignment` default is expressed in (a row's horizontal position is already fully
     * determined by its main-axis arrangement, so there's no matching horizontal knob to expose
     * there). */
    enum class Vertical { Top, Center, Bottom }

    /** Cross-axis-only horizontal component, matching Jetpack Compose's `Alignment.Horizontal`
     * -- the [io.github.ronjunevaldoz.awake.ui.layouts.Column] counterpart to [Vertical]. */
    enum class Horizontal { Start, Center, End }

    companion object {
        /** Combines a cross-axis-only [Vertical]/[Horizontal] pair back into the full
         * [UiAlignment] that [place] actually understands. */
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

fun UiSlot.place(
    width: Float,
    height: Float,
    alignment: UiAlignment = UiAlignment.TopStart,
    insets: UiInsets = UiInsets.Zero,
    offsetX: Float = 0f,
    offsetY: Float = 0f
): UiSlot {
    val content = inset(insets)
    val resolvedWidth = width.coerceIn(0f, content.width)
    val resolvedHeight = height.coerceIn(0f, content.height)
    val x = when (alignment) {
        UiAlignment.TopStart, UiAlignment.CenterStart, UiAlignment.BottomStart -> content.x
        UiAlignment.TopCenter, UiAlignment.Center, UiAlignment.BottomCenter -> content.x + (content.width - resolvedWidth) / 2f
        UiAlignment.TopEnd, UiAlignment.CenterEnd, UiAlignment.BottomEnd -> content.x + content.width - resolvedWidth
    } + offsetX
    val y = when (alignment) {
        UiAlignment.TopStart, UiAlignment.TopCenter, UiAlignment.TopEnd -> content.y
        UiAlignment.CenterStart, UiAlignment.Center, UiAlignment.CenterEnd -> content.y + (content.height - resolvedHeight) / 2f
        UiAlignment.BottomStart, UiAlignment.BottomCenter, UiAlignment.BottomEnd -> content.y + content.height - resolvedHeight
    } + offsetY
    return UiSlot(x, y, resolvedWidth, resolvedHeight)
}
