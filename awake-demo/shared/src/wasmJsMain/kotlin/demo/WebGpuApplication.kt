// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package demo

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRenderableRequest
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.webgpu.triangleWgslShader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Web demo (see docs/MVP_PLAN.md's decision log): wasmJs's WebGPU counterpart to
 * `VulkanApplication.kt` -- same cube geometry, [MAX_FRAMES_IN_FLIGHT] shape,
 * [resolveRenderable]'s mesh="cube"/material="textured-default" contract, and
 * [SceneRuntimeHost] wiring, built on `awake-backend-webgpu`'s types instead. [Material] is
 * constructed only as a placeholder to satisfy [MeshRenderer]'s type -- its real methods
 * are still `TODO()` on this backend, and [Renderer.draw] never touches `DrawCall
 * .material` (confirmed this session's browser verification), so nothing calls them.
 * [Texture] isn't constructed at all for the same reason.
 *
 * [create]'s [surface] parameter must be a pre-resolved `io.ygdrasil.webgpu.WGPUContext`,
 * not a raw canvas (see [GraphicsDevice]'s own doc comment) -- `main.kt` resolves it via
 * `canvasContextRenderer()` + `surface.configure()` in its own coroutine before calling
 * this, since that resolution is itself `suspend` and [create] must stay synchronous (see
 * this class's own doc comment on [create] for why).
 */
class WebGpuApplication : Application {
    private val graphicsDevice = GraphicsDevice()
    private val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
    private lateinit var renderPipeline: RenderPipeline
    private lateinit var renderer: Renderer
    private lateinit var mesh: Mesh
    /** MVP1a ground-plane slice (see docs/MMORPG_ROADMAP.md) -- see VulkanApplication.kt's
     * matching field doc comment. */
    private lateinit var groundMesh: Mesh
    private lateinit var material: Material
    private lateinit var sceneHost: SceneRuntimeHost
    private val fixedTimestepLoop = FixedTimestepLoop()
    /** Same "create() stays synchronous, launch internally" reasoning as
     * `VulkanApplication` -- [update] is a no-op until [setupWebGpu] finishes. */
    private var isReady = false

    companion object {
        const val MAX_FRAMES_IN_FLIGHT = 1

        // Same cube geometry as demo/VulkanApplication.kt (interleaved pos/color/uv, 8
        // corners, RGB-cube palette) -- the WGSL shader only reads pos+color (see
        // TriangleShader.kt), the uv floats are inert padding matching the shared stride.
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
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )

        // MVP1a ground-plane slice (see docs/MMORPG_ROADMAP.md) -- see VulkanApplication.kt's
        // matching companion-object fields for the full rationale (extent matches
        // DemoNavMeshGeometry, flat white color, UVs scaled for tiling).
        val groundVertices = floatArrayOf(
            -10f, 0f, -10f, 1f, 1f, 1f, 0f, 0f, // v0
            10f, 0f, -10f, 1f, 1f, 1f, 8f, 0f, // v1
            10f, 0f, 10f, 1f, 1f, 1f, 8f, 8f, // v2
            -10f, 0f, 10f, 1f, 1f, 1f, 0f, 8f, // v3
        )
        val groundIndices = intArrayOf(0, 2, 1, 0, 3, 2)
    }

    override fun create(surface: Any?) {
        surface?.let { window -> MainScope().launch { setupWebGpu(window) } }
    }

    override fun update(delta: Float) {
        if (!isReady) return
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = sceneHost::fixedUpdate,
            render = { sceneHost.render() }
        )
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(x: Int, y: Int, width: Int, height: Int) {
    }

    override fun dispose() {
        if (isReady) destroy()
    }

    private suspend fun setupWebGpu(window: Any) {
        graphicsDevice.create(window)
        swapchainManager.create()
        material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            DescriptorSetLayoutHandle(0),
            triangleWgslShader.encodeToByteArray(),
            ByteArray(0),
            VERTEX_STRIDE
        )
        renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            0L,
            MAX_FRAMES_IN_FLIGHT
        )
        mesh = Mesh(graphicsDevice, {}, cubeVertices, cubeIndices)
        groundMesh = Mesh(graphicsDevice, {}, groundVertices, groundIndices)
        sceneHost = SceneRuntimeHost.create(renderer) { request ->
            resolveRenderable(request)
        }
        isReady = true
    }

    private fun destroy() {
        renderer.destroy()
        swapchainManager.destroy()
        mesh.destroy()
        groundMesh.destroy()
        renderPipeline.destroy()
        graphicsDevice.destroy()
    }

    private fun resolveRenderable(request: SceneRenderableRequest): MeshRenderer {
        val resolvedMesh = when (request.meshRenderer.mesh) {
            "cube" -> mesh
            "ground" -> groundMesh
            else -> error("Unsupported scene mesh '${request.meshRenderer.mesh}'.")
        }
        require(request.meshRenderer.material == "textured-default") {
            "Unsupported scene material '${request.meshRenderer.material}'."
        }
        return MeshRenderer(resolvedMesh, material)
    }
}
