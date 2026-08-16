// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/**
 * An ordered, tightly-packed interleaved vertex layout -- [attributes] in declaration order,
 * each attribute's byte offset computed from the cumulative size of the attributes before it
 * (no manual offset bookkeeping, unlike the hand-written offset tables this replaces in a
 * backend's own vertex-input-state setup). [strideBytes] is the total per-vertex byte size --
 * the same value backends used to thread through as a bare `vertexStride: Int` parameter.
 */
data class VertexFormat(val attributes: List<VertexAttribute>) {
    data class Entry(val attribute: VertexAttribute, val offsetBytes: Int)

    val entries: List<Entry> = attributes
        .runningFold(0) { offset, attribute -> offset + attribute.format.byteSize }
        .zip(attributes) { offset, attribute -> Entry(attribute, offset) }
    val strideBytes: Int = attributes.sumOf { it.format.byteSize }

    companion object {
        /** Position vec3 @ 0, color vec3 @ 12, uv vec2 @ 24, stride 32 -- the exact layout every
         * backend's vertex pipeline hardcoded before this type existed. Kept as the default
         * [io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry] format so every pre-existing
         * mesh/shader pair keeps working unchanged. */
        val PositionColorUv = VertexFormat(
            listOf(
                VertexAttribute(VertexSemantic.Position, VertexAttributeFormat.Float3, location = 0),
                VertexAttribute(VertexSemantic.Color, VertexAttributeFormat.Float3, location = 1),
                VertexAttribute(VertexSemantic.Uv, VertexAttributeFormat.Float2, location = 2),
            ),
        )

        /** Position vec3 @ 0, normal vec3 @ 12, color vec3 @ 24, stride 36 -- for a shader that
         * shades with real per-vertex normals (Lambertian diffuse) instead of flat unlit vertex
         * color. Not the [MeshGeometry] default -- opt in per bootstrap (see
         * `Scene3DPlaygroundVulkanBootstrap.kt`'s own `vertexFormat` choice) since every mesh
         * drawn through one [io.github.ronjunevaldoz.awake.render.renderer.Renderer] shares its
         * one bound pipeline's vertex layout; switching this changes every mesh in that renderer,
         * not just one. */
        val PositionNormalColor = VertexFormat(
            listOf(
                VertexAttribute(VertexSemantic.Position, VertexAttributeFormat.Float3, location = 0),
                VertexAttribute(VertexSemantic.Normal, VertexAttributeFormat.Float3, location = 1),
                VertexAttribute(VertexSemantic.Color, VertexAttributeFormat.Float3, location = 2),
            ),
        )

        /** [PositionNormalColor] plus joint indices (uint4 @ 36) and joint weights (vec4 @ 52),
         * stride 68 -- for a GPU-skinned mesh's vertex shader, which blends up to 4 joint
         * matrices by weight before applying MVP (see `skinned.wgsl`). Matches
         * [io.github.ronjunevaldoz.awake.asset.gltf.GltfMesh.toInterleavedSkinned]'s layout. */
        val PositionNormalColorSkin = VertexFormat(
            listOf(
                VertexAttribute(VertexSemantic.Position, VertexAttributeFormat.Float3, location = 0),
                VertexAttribute(VertexSemantic.Normal, VertexAttributeFormat.Float3, location = 1),
                VertexAttribute(VertexSemantic.Color, VertexAttributeFormat.Float3, location = 2),
                VertexAttribute(VertexSemantic.JointIndices, VertexAttributeFormat.UInt4, location = 3),
                VertexAttribute(VertexSemantic.JointWeights, VertexAttributeFormat.Float4, location = 4),
            ),
        )

        /** [PositionNormalColor] plus a uv vec2 @ 36, stride 44 -- for a mesh whose material has
         * a real `baseColorTexture` to sample (see `textured.wgsl`). Matches
         * [io.github.ronjunevaldoz.awake.asset.gltf.GltfMesh.toInterleavedPositionNormalColorUv]'s
         * layout. */
        val PositionNormalColorUv = VertexFormat(
            listOf(
                VertexAttribute(VertexSemantic.Position, VertexAttributeFormat.Float3, location = 0),
                VertexAttribute(VertexSemantic.Normal, VertexAttributeFormat.Float3, location = 1),
                VertexAttribute(VertexSemantic.Color, VertexAttributeFormat.Float3, location = 2),
                VertexAttribute(VertexSemantic.Uv, VertexAttributeFormat.Float2, location = 3),
            ),
        )
    }
}
