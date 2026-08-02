// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry

// Same interleaved position(vec3) + color(vec3) + uv(vec2) layout the shared sample cube
// meshes already use. The canonical shader source now lives in src/commonMain/shaders and
// only reads position+color today; UV remains in the mesh layout so Vulkan/WebGPU can stay
// aligned with the existing renderer contract while the shared shader pipeline matures.
val sampleVertexStride = 8 * Float.SIZE_BYTES

private val cubeVertices = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
    0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
    0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
    -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
    -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
    0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
    0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
    -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
)
private val cubeIndices = intArrayOf(
    0, 1, 2, 2, 3, 0, // back
    4, 5, 6, 6, 7, 4, // front
    0, 3, 7, 7, 4, 0, // left
    1, 5, 6, 6, 2, 1, // right
    0, 4, 5, 5, 1, 0, // bottom
    3, 2, 6, 6, 7, 3, // top
)

val sampleCubeGeometry = MeshGeometry(cubeVertices, cubeIndices)

/**
 * Flat checkerboard ground plane at local y=0 -- gives the physics demo's falling box a
 * visually legible reference surface (alternating light/dark cells make position and scale
 * readable at a glance, the same reason level editors default to a checkered ground) without
 * introducing a new mesh topology: same interleaved position/color/uv triangle-list layout
 * [sampleCubeGeometry] already uses, just a subdivided quad instead of a cube.
 */
fun sampleGridGeometry(halfSize: Float = 5f, cells: Int = 10): MeshGeometry {
    val cellSize = (halfSize * 2f) / cells
    val vertices = mutableListOf<Float>()
    val indices = mutableListOf<Int>()
    var nextVertexIndex = 0
    for (row in 0 until cells) {
        for (col in 0 until cells) {
            val x0 = -halfSize + col * cellSize
            val z0 = -halfSize + row * cellSize
            val x1 = x0 + cellSize
            val z1 = z0 + cellSize
            val light = (row + col) % 2 == 0
            val shade = if (light) 0.75f else 0.35f
            val corners = listOf(
                Triple(x0, 0f, z0) to (0f to 0f),
                Triple(x1, 0f, z0) to (1f to 0f),
                Triple(x1, 0f, z1) to (1f to 1f),
                Triple(x0, 0f, z1) to (0f to 1f)
            )
            corners.forEach { (position, uv) ->
                vertices += position.first
                vertices += position.second
                vertices += position.third
                vertices += shade
                vertices += shade
                vertices += shade
                vertices += uv.first
                vertices += uv.second
            }
            indices += nextVertexIndex
            indices += nextVertexIndex + 1
            indices += nextVertexIndex + 2
            indices += nextVertexIndex + 2
            indices += nextVertexIndex + 3
            indices += nextVertexIndex
            nextVertexIndex += 4
        }
    }
    return MeshGeometry(vertices.toFloatArray(), indices.toIntArray())
}
