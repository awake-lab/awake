// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/** Numeric shape of one [VertexAttribute]'s data -- component count times bytes-per-component
 * gives [byteSize], the value [VertexFormat] uses to compute each attribute's offset. */
enum class VertexAttributeFormat(val componentCount: Int, val bytesPerComponent: Int) {
    Float2(2, 4),
    Float3(3, 4),
    Float4(4, 4),

    /** glTF's `JOINTS_0` is `ubyte4`/`ushort4`; this widens either to 4 bytes/component at
     * import time so there's one integer vertex format to map per backend instead of two. */
    UInt4(4, 4);

    val byteSize: Int get() = componentCount * bytesPerComponent
}

/** What role an attribute's data plays -- distinct from [VertexAttributeFormat], which is just
 * the numeric shape; a mesh importer/builder picks the semantic, [VertexAttributeFormat] follows. */
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
    val format: VertexAttributeFormat,
    val location: Int
)
