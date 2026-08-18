// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/**
 * The natural, UNPADDED component shape of one GPU value -- [Vec3] is always 3 components. How
 * many floats/bytes that actually occupies in a given buffer depends on THAT buffer's own
 * alignment rules, not on this type -- see [vertexByteSize] (vertex buffers, no padding) vs
 * [io.github.ronjunevaldoz.awake.render.renderer.uniformFloats] (uniform buffers, std140/WGSL's
 * mandatory vec3->vec4 padding). One shared shape, two buffer-specific sizing extensions -- not
 * two separate enums that happen to mean almost the same thing. Which meaning applies at any
 * given call site comes from the CONTAINING field ([VertexAttribute.format] vs a uniform
 * layout's own field type), not from this enum's case names.
 */
enum class GpuDataShape(val componentCount: Int, val bytesPerComponent: Int = FLOAT_BYTES) {
    // Float/Vec2/Vec3/Vec4 shadow well-known names (kotlin.Float, and Vec2/Vec3/Vec4 in
    // core.math) -- always reference these qualified as GpuDataShape.Float/.Vec3/etc (as every
    // call site in this codebase already does), never with an unqualified import, or a bare
    // `Float` inside a `when (this)` branch silently resolves to the wrong type.
    Float(1),
    Vec2(2),
    Vec3(3),
    Vec4(4),
    Mat4(16),

    /** glTF's `JOINTS_0` is `ubyte4`/`ushort4`; this widens either to 4 bytes/component at
     * import time so there's one integer vertex format to map per backend instead of two.
     * Vertex-buffer only -- no uniform buffer in this codebase has an integer field. */
    UInt4(4),
}

private const val FLOAT_BYTES = 4

/** Unpadded vertex-buffer byte size -- what [VertexFormat] uses to compute each attribute's
 * offset/stride. */
val GpuDataShape.vertexByteSize: Int get() = componentCount * bytesPerComponent

/** What role an attribute's data plays -- distinct from [GpuDataShape], which is just the
 * numeric shape; a mesh importer/builder picks the semantic, [GpuDataShape] follows. */
enum class VertexSemantic { Position, Normal, Uv, Color, JointIndices, JointWeights, Tangent }

/**
 * One named slot in an interleaved vertex buffer -- a [semantic] role plus the [format]
 * backing it. [location] is the shader input location this attribute binds to
 * (`layout(location = N)` in GLSL / `@location(N)` in WGSL) -- caller-assigned, not
 * auto-numbered from list position, so a shader's location layout stays an explicit, readable
 * contract instead of an implicit array-index one.
 */
data class VertexAttribute(
    val semantic: VertexSemantic,
    val format: GpuDataShape,
    val location: Int,
)
