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

    /** One glyph quad sampling a [io.github.ronjunevaldoz.awake.ui.font.BitmapFont]'s atlas
     * -- Phase B (see docs/MVP_PLAN.md's custom-UI decision log), drawn via a second,
     * textured pipeline after [Quad]s in the same UI overlay pass. */
    data class Glyph(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val u0: Float,
        val v0: Float,
        val u1: Float,
        val v1: Float,
        val color: FloatArray
    ) : UiDrawPrimitive() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Glyph) return false
            return x == other.x && y == other.y && w == other.w && h == other.h &&
                u0 == other.u0 && v0 == other.v0 && u1 == other.u1 && v1 == other.v1 &&
                color.contentEquals(other.color)
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + w.hashCode()
            result = 31 * result + h.hashCode()
            result = 31 * result + u0.hashCode()
            result = 31 * result + v0.hashCode()
            result = 31 * result + u1.hashCode()
            result = 31 * result + v1.hashCode()
            result = 31 * result + color.contentHashCode()
            return result
        }
    }
}
