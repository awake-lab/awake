// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package demo

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

/**
 * wasmJs counterpart to `VulkanApplication.kt` -- same geometry data, same
 * [SceneRuntimeHost] wiring, built on [WebGpuGameApplication] (`awake-backend-webgpu`)
 * instead. See docs/MVP_PLAN.md's Decision Log ("reusable-Application gap fix") for why the
 * generic engine bootstrap moved out of this class. `create()`'s `surface` parameter must be
 * a pre-resolved `io.ygdrasil.webgpu.WGPUContext`, not a raw canvas -- `main.kt` resolves it
 * before calling `create()`, see [WebGpuGameApplication]'s own doc comment.
 */
class WebGpuApplication : WebGpuGameApplication(
    vertexShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    fragmentShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    vertexStride = VERTEX_STRIDE,
    meshes = mapOf(
        "cube" to MeshGeometry(cubeVertices, cubeIndices),
        "ground" to MeshGeometry(groundVertices, groundIndices)
    ),
    scenePath = "scenes/mvp.scene.json"
) {
    private lateinit var sceneHost: SceneRuntimeHost

    override fun onSceneReady() {
        sceneHost = SceneRuntimeHost.create(scene, world)
    }

    override fun onFixedUpdate(delta: Float) {
        sceneHost.fixedUpdate(delta)
        super.onFixedUpdate(delta)
    }

    companion object {
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        // Same cube geometry as demo/VulkanApplication.kt (interleaved pos/color/uv, 8
        // corners, RGB-cube palette) -- the WGSL shader only reads pos+color, the uv floats
        // are inert padding matching the shared stride.
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

        // MVP1a ground-plane slice (see docs/MMORPG_ROADMAP.md) -- see VulkanApplication.kt's
        // matching companion-object fields for the full rationale.
        val groundVertices = floatArrayOf(
            -10f, 0f, -10f, 1f, 1f, 1f, 0f, 0f, // v0
            10f, 0f, -10f, 1f, 1f, 1f, 8f, 0f, // v1
            10f, 0f, 10f, 1f, 1f, 1f, 8f, 8f, // v2
            -10f, 0f, 10f, 1f, 1f, 1f, 0f, 8f, // v3
        )
        val groundIndices = intArrayOf(0, 2, 1, 0, 3, 2)
    }
}
