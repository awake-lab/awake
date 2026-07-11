// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Backend-neutral output of a [UiContext] frame -- each backend's `Renderer.drawUi` converts
 * these into its own dynamic vertex/index buffer. Pixel-space coordinates (screen-space,
 * Y-down), not NDC -- the NDC transform is the shader's job (see `ui_quad.vert`/`.wgsl`).
 */
sealed class UiDrawPrimitive {
    data class Quad(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val color: FloatArray
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Quad) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                color.contentEquals(other.color)
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + color.contentHashCode()
            return result
        }
    }
}
