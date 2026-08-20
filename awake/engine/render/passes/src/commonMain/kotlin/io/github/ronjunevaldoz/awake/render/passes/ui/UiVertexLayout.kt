// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.mesh.VertexAttribute
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.mesh.VertexSemantic

/**
 * Standardized vertex formats, strides, quad geometries, and buffer constants for 2D UI rendering.
 */
object UiVertexLayout {

    /** 2D Colored Quad: pos (vec2) + color (vec4) + transform (vec4) = 10 floats (40 bytes). */
    val Quad = VertexFormat(
        listOf(
            VertexAttribute(VertexSemantic.Position, GpuDataShape.Vec2, location = 0),
            VertexAttribute(VertexSemantic.Color, GpuDataShape.Vec4, location = 1),
            VertexAttribute(VertexSemantic.Transform, GpuDataShape.Vec4, location = 2),
        ),
    )

    /** 2D Textured Glyph: pos (vec2) + uv (vec2) + color (vec4) + transform (vec4) = 12 floats (48 bytes). */
    val Glyph = VertexFormat(
        listOf(
            VertexAttribute(VertexSemantic.Position, GpuDataShape.Vec2, location = 0),
            VertexAttribute(VertexSemantic.Uv, GpuDataShape.Vec2, location = 1),
            VertexAttribute(VertexSemantic.Color, GpuDataShape.Vec4, location = 2),
            VertexAttribute(VertexSemantic.Transform, GpuDataShape.Vec4, location = 3),
        ),
    )

    /** 2D Rounded Quad: pos (vec2) + localPos (vec2) + halfSize (vec2) + radius/smoothing (vec2) + color (vec4) + transform (vec4) = 16 floats (64 bytes). */
    val RoundedQuad = VertexFormat(
        listOf(
            VertexAttribute(VertexSemantic.Position, GpuDataShape.Vec2, location = 0),
            VertexAttribute(VertexSemantic.LocalPosition, GpuDataShape.Vec2, location = 1),
            VertexAttribute(VertexSemantic.Size, GpuDataShape.Vec2, location = 2),
            VertexAttribute(VertexSemantic.Radius, GpuDataShape.Vec2, location = 3),
            VertexAttribute(VertexSemantic.Color, GpuDataShape.Vec4, location = 4),
            VertexAttribute(VertexSemantic.Transform, GpuDataShape.Vec4, location = 5),
        ),
    )

    /** 3D Debug Line: pos (vec3) + color (vec4) = 7 floats (28 bytes). */
    val Line = VertexFormat(
        listOf(
            VertexAttribute(VertexSemantic.Position, GpuDataShape.Vec3, location = 0),
            VertexAttribute(VertexSemantic.Color, GpuDataShape.Vec4, location = 1),
        ),
    )

    /** Colored quad layout float count. */
    const val FLOATS_PER_VERTEX = 10

    /** Textured glyph quad layout float count. */
    const val GLYPH_FLOATS_PER_VERTEX = 12

    /** Rounded quad layout float count. */
    const val ROUNDED_QUAD_FLOATS_PER_VERTEX = 16

    /** 3D debug line layout float count. */
    const val LINE_FLOATS_PER_VERTEX = 7

    /** Standard quad geometry. */
    const val VERTICES_PER_QUAD = 4
    const val INDICES_PER_QUAD = 6
}
