// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api.layout

/** Immutable measured bounds shared by every public UI layer. */
data class UiBounds(val x: Float, val y: Float, val width: Float, val height: Float)

/** Clamps this rect to the region it shares with [other] -- zero-size if they do not overlap. */
fun UiBounds.intersect(other: UiBounds): UiBounds {
    val left = maxOf(x, other.x)
    val top = maxOf(y, other.y)
    val right = minOf(x + width, other.x + other.width)
    val bottom = minOf(y + height, other.y + other.height)
    return UiBounds(left, top, (right - left).coerceAtLeast(0f), (bottom - top).coerceAtLeast(0f))
}

/** True when [other] lies entirely within this rect (on-edge counts as contained). */
fun UiBounds.contains(other: UiBounds): Boolean =
    other.x >= x &&
        other.y >= y &&
        other.x + other.width <= x + width &&
        other.y + other.height <= y + height
