// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** Which viewport edge a slide-in panel (sheet, drawer, side nav) is anchored to. */
enum class UiEdge {
    Top,
    Right,
    Bottom,
    Left,
}

/**
 * [edge]-anchored slide offset: [slideProgress] `0` rests the panel fully off-screen past
 * [edge], `1` rests it flush against that edge -- any differently-skinned slide-over panel
 * needs the same interpolation, not just shadcn's `Sheet`.
 */
fun slideInPanelBounds(
    edge: UiEdge,
    sizePx: Float,
    slideProgress: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): UiBounds {
    val hiddenOffset = (1f - slideProgress) * sizePx
    return when (edge) {
        UiEdge.Left -> UiBounds(-hiddenOffset, 0f, sizePx, viewportHeight)
        UiEdge.Right -> UiBounds(viewportWidth - sizePx + hiddenOffset, 0f, sizePx, viewportHeight)
        UiEdge.Top -> UiBounds(0f, -hiddenOffset, viewportWidth, sizePx)
        UiEdge.Bottom -> UiBounds(0f, viewportHeight - sizePx + hiddenOffset, viewportWidth, sizePx)
    }
}
