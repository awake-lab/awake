// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/**
 * A typed, coalesced UI draw command in paint order, ready for GPU buffer upload and recording.
 */
sealed class UiStagedRun {
    /** Flat colored 2D quads, gradients, clipped paths, or AA-tessellated convex polygons. */
    class QuadRun(val vertices: FloatArray, val indices: IntArray) : UiStagedRun()

    /** SDF rounded rectangle and shadow quads. */
    class RoundedQuadRun(val vertices: FloatArray, val indices: IntArray) : UiStagedRun()

    /** Textured font glyph quads. */
    class GlyphRun(val vertices: FloatArray, val indices: IntArray) : UiStagedRun()

    /** Textured image or render target primitives. */
    class TextureRun(val primitives: List<TexturedPrimitiveRun>) : UiStagedRun()

    /** Scissor rectangle update in original emission order. */
    class ClipRun(val rect: UiBounds) : UiStagedRun()
}

/**
 * A single textured primitive within a [UiStagedRun.TextureRun].
 */
data class TexturedPrimitiveRun(
    val texture: Any,
    val vertices: FloatArray,
    val indices: IntArray,
)
