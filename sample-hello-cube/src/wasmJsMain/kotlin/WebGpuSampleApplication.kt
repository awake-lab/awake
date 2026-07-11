// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

/**
 * wasmJs counterpart to `SampleApplication` (`appMain`, desktop/Android/iOS only -- not
 * visible from this wasmJs-only source set, same reason `awake-demo`'s two `Application`
 * classes each keep their own copy of the same geometry rather than sharing one). Same
 * single-cube geometry, no texture, built on [WebGpuGameApplication] (`awake-backend-
 * webgpu`) instead.
 */
class WebGpuSampleApplication : WebGpuGameApplication(
    vertexShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    fragmentShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    vertexStride = VERTEX_STRIDE,
    meshes = mapOf("cube" to MeshGeometry(cubeVertices, cubeIndices)),
    scenePath = "scenes/sample.scene.json"
) {
    companion object {
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )
    }
}
