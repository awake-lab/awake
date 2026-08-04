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
                VertexAttribute(VertexSemantic.Uv, VertexAttributeFormat.Float2, location = 2)
            )
        )
    }
}
