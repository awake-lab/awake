// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/**
 * Raw vertex/index data a game supplies to a backend's game-application bootstrap (see
 * `VulkanGameApplication`/`WebGpuGameApplication`) -- backend-neutral input, not a rendering
 * abstraction itself. [format] declares [vertices]' actual interleaved layout (attribute
 * order/offsets, stride) -- defaults to [VertexFormat.PositionColorUv], the layout every
 * pre-existing mesh already used before this field existed, so no caller needs to change.
 */
data class MeshGeometry(
    val vertices: FloatArray,
    val indices: IntArray,
    val format: VertexFormat = VertexFormat.PositionColorUv,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshGeometry) return false
        return vertices.contentEquals(other.vertices) && indices.contentEquals(other.indices) && format == other.format
    }

    override fun hashCode(): Int = 31 * (31 * vertices.contentHashCode() + indices.contentHashCode()) + format.hashCode()
}
