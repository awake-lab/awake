// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

/**
 * A single decoded glTF primitive's attribute arrays, before interleaving -- one `Float`
 * per component, row-major (`positions[i*3]`/`[i*3+1]`/`[i*3+2]` is vertex `i`'s x/y/z).
 * [normals]/[colors]/[uvs] are `null` when the source glTF primitive didn't have that
 * attribute at all, not zero-filled -- see [toInterleavedPositionColorUv] for the default
 * values used when interleaving a primitive that's missing one.
 */
data class GltfMesh(
    val positions: FloatArray,
    val normals: FloatArray?,
    val colors: FloatArray?,
    val uvs: FloatArray?,
    val indices: IntArray
) {
    val vertexCount: Int get() = positions.size / POSITION_COMPONENTS

    /**
     * Interleaves into the position(vec3)+color(vec3)+uv(vec2) -- 8 floats/vertex --
     * layout `VulkanApplication.cubeVertices` already uses (matching `triangle.vert`'s
     * location 0/1/2 inputs), so a parsed glTF mesh can feed the exact same
     * `Mesh(graphicsDevice, runOneTimeCommands, vertices, indices)` constructor the
     * hardcoded demo cube does. Missing [colors] default to white (`1, 1, 1`), missing
     * [uvs] default to `(0, 0)` -- both harmless, visible defaults rather than silently
     * wrong-looking geometry.
     */
    fun toInterleavedPositionColorUv(): FloatArray {
        val result = FloatArray(vertexCount * VERTEX_STRIDE_COMPONENTS)
        for (i in 0 until vertexCount) {
            val out = i * VERTEX_STRIDE_COMPONENTS
            result[out] = positions[i * POSITION_COMPONENTS]
            result[out + 1] = positions[i * POSITION_COMPONENTS + 1]
            result[out + 2] = positions[i * POSITION_COMPONENTS + 2]

            if (colors != null) {
                result[out + 3] = colors[i * COLOR_COMPONENTS]
                result[out + 4] = colors[i * COLOR_COMPONENTS + 1]
                result[out + 5] = colors[i * COLOR_COMPONENTS + 2]
            } else {
                result[out + 3] = 1f
                result[out + 4] = 1f
                result[out + 5] = 1f
            }

            if (uvs != null) {
                result[out + 6] = uvs[i * UV_COMPONENTS]
                result[out + 7] = uvs[i * UV_COMPONENTS + 1]
            } else {
                result[out + 6] = 0f
                result[out + 7] = 0f
            }
        }
        return result
    }

    /**
     * Interleaves into the position(vec3)+normal(vec3)+color(vec3) -- 9 floats/vertex --
     * layout [io.github.ronjunevaldoz.awake.render.mesh.VertexFormat.PositionNormalColor]
     * describes, for a shader that shades with real per-vertex normals instead of flat unlit
     * vertex color. Missing [normals] default to world-up (`0, 1, 0`) -- an arbitrary but
     * harmless fallback (no glTF exporter omits `NORMAL` on a mesh meant to be shaded, so this
     * mainly guards a malformed/synthetic file); missing [colors] default to white, same as
     * [toInterleavedPositionColorUv].
     */
    fun toInterleavedPositionNormalColor(): FloatArray {
        val result = FloatArray(vertexCount * NORMAL_VERTEX_STRIDE_COMPONENTS)
        for (i in 0 until vertexCount) {
            val out = i * NORMAL_VERTEX_STRIDE_COMPONENTS
            result[out] = positions[i * POSITION_COMPONENTS]
            result[out + 1] = positions[i * POSITION_COMPONENTS + 1]
            result[out + 2] = positions[i * POSITION_COMPONENTS + 2]

            if (normals != null) {
                result[out + 3] = normals[i * NORMAL_COMPONENTS]
                result[out + 4] = normals[i * NORMAL_COMPONENTS + 1]
                result[out + 5] = normals[i * NORMAL_COMPONENTS + 2]
            } else {
                result[out + 3] = 0f
                result[out + 4] = 1f
                result[out + 5] = 0f
            }

            if (colors != null) {
                result[out + 6] = colors[i * COLOR_COMPONENTS]
                result[out + 7] = colors[i * COLOR_COMPONENTS + 1]
                result[out + 8] = colors[i * COLOR_COMPONENTS + 2]
            } else {
                result[out + 6] = 1f
                result[out + 7] = 1f
                result[out + 8] = 1f
            }
        }
        return result
    }

    private companion object {
        const val POSITION_COMPONENTS = 3
        const val NORMAL_COMPONENTS = 3
        const val COLOR_COMPONENTS = 3
        const val UV_COMPONENTS = 2
        const val VERTEX_STRIDE_COMPONENTS = 8
        const val NORMAL_VERTEX_STRIDE_COMPONENTS = 9
    }
}
