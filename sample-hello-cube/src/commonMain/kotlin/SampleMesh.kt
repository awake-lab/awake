// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry

// Same interleaved position(vec3) + color(vec3) + uv(vec2) layout the shared
// triangle.vert/.frag (Vulkan) and triangle.wgsl (WebGPU) shaders expect -- see the
// (now-retired) awake-demo's VulkanApplication.kt for the full rationale behind this exact
// vertex format/palette. Hoisted here (see docs/MVP_PLAN.md's decision log,
// "GenericGameApplication a standalone render bootstrap") so it's written once in commonMain
// instead of byte-for-byte duplicated between the (now-deleted) SampleApplication.kt/
// WebGpuSampleApplication.kt.
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
