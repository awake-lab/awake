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
