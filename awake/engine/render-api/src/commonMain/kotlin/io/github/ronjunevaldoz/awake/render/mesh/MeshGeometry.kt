// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/**
 * Raw vertex/index data a game supplies to a backend's game-application bootstrap (see
 * `VulkanGameApplication`/`WebGpuGameApplication`) -- backend-neutral input, not a rendering
 * abstraction itself. Layout (stride, attribute order) is a contract between the caller and
 * the shader it pairs with, not something this type enforces.
 */
data class MeshGeometry(val vertices: FloatArray, val indices: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshGeometry) return false
        return vertices.contentEquals(other.vertices) && indices.contentEquals(other.indices)
    }

    override fun hashCode(): Int {
        return 31 * vertices.contentHashCode() + indices.contentHashCode()
    }
}
