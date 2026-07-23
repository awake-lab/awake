// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.scope

/**
 * TODO should be internal only??
 */
data class UiSlot(val x: Float, val y: Float, val width: Float, val height: Float)

/** Clamps this rect to the region it shares with [other] -- zero-size if they don't overlap. */
fun UiSlot.intersect(other: UiSlot): UiSlot {
    val left = maxOf(x, other.x)
    val top = maxOf(y, other.y)
    val right = minOf(x + width, other.x + other.width)
    val bottom = minOf(y + height, other.y + other.height)
    return UiSlot(left, top, (right - left).coerceAtLeast(0f), (bottom - top).coerceAtLeast(0f))
}
