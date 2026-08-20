// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

/**
 * Standardized vertex strides, quad geometries, and buffer constants for 2D UI rendering.
 */
object UiVertexLayout {
    /** Colored quad layout: pos(vec2) + color(vec4) + transform(vec4: scale.xy + pivot.xy). */
    const val FLOATS_PER_VERTEX = 10

    /** Textured glyph quad layout: pos(vec2) + uv(vec2) + color(vec4) + transform(vec4). */
    const val GLYPH_FLOATS_PER_VERTEX = 12

    /** Rounded quad layout: pos(vec2) + localPos(vec2) + halfSize(vec2) + radius(float) + smoothing(float) + color(vec4) + transform(vec4). */
    const val ROUNDED_QUAD_FLOATS_PER_VERTEX = 16

    /** 3D debug line layout: pos(vec3) + color(vec4). */
    const val LINE_FLOATS_PER_VERTEX = 7

    /** Standard quad geometry. */
    const val VERTICES_PER_QUAD = 4
    const val INDICES_PER_QUAD = 6
}
