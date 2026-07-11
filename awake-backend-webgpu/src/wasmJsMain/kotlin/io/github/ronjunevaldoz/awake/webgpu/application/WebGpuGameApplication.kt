// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRenderableRequest
import io.github.ronjunevaldoz.awake.scene.runtime.attachRenderableComponents
import io.github.ronjunevaldoz.awake.scene.runtime.instantiate
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Reusable WebGPU game bootstrap -- wasmJs counterpart to `VulkanGameApplication`
 * (`awake-backend-vulkan`), see that class's doc comment and docs/MVP_PLAN.md's Decision
 * Log ("reusable-Application gap fix") for the full rationale. Mirrors
 * `VulkanGameApplication`'s constructor/lifecycle shape exactly, but the bootstrap sequence
 * genuinely omits `TransferContext`/`Texture`/`Material.createResources` -- this backend's
 * `Material` is still a placeholder (`TODO()`-only) and `Mesh`'s upload doesn't need a
 * staging-buffer/command-buffer dance the way Vulkan's does, so [texture] is accepted for
 * constructor-shape parity with `VulkanGameApplication` but never actually used here.
 *
 * [create]'s `surface` parameter must be a pre-resolved `io.ygdrasil.webgpu.WGPUContext`,
 * not a raw canvas (see [GraphicsDevice]'s own doc comment) -- the platform entry point
 * (`main.kt`) resolves it via `canvasContextRenderer()` + `surface.configure()` in its own
 * coroutine before calling [create], since that resolution is itself `suspend`.
 */
abstract class WebGpuGameApplication(
    private val vertexShaderResourcePath: String,
    private val fragmentShaderResourcePath: String,
    private val vertexStride: Int,
    private val meshes: Map<String, MeshGeometry>,
    private val texture: TextureAsset? = null,
    private val scenePath: String
) : Application {
    private val graphicsDevice = GraphicsDevice()
    private val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
    private lateinit var renderPipeline: RenderPipeline
    private lateinit var renderer: Renderer
    private lateinit var material: Material
    private val meshInstances = mutableMapOf<String, Mesh>()

    protected lateinit var world: World
        private set

    /** The instantiated scene graph (root node names/entities) -- see [world]'s doc
     * comment. */
    protected lateinit var scene: SceneInstance
        private set
    private val transformSystem = TransformSystem()
    private lateinit var renderSystem: RenderSystem
    private val fixedTimestepLoop = FixedTimestepLoop()

    /** Same "create() stays synchronous, launch internally" reasoning as
     * `VulkanGameApplication` -- [update] is a no-op until [setupWebGpu] finishes. */
    private var isReady = false

    final override fun create(surface: Any?) {
        surface?.let { window -> MainScope().launch { setupWebGpu(window) } }
    }

    final override fun update(delta: Float) {
        if (!isReady) return
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = ::onFixedUpdate,
            render = { onRender() }
        )
    }

    override fun pause() {}
    override fun resume() {}
    override fun resize(x: Int, y: Int, width: Int, height: Int) {}

    final override fun dispose() {
        if (isReady) destroy()
    }

    /** See `VulkanGameApplication.onFixedUpdate`'s doc comment -- identical contract. */
    protected open fun onFixedUpdate(delta: Float) {
        transformSystem.update(world, delta)
    }

    /** See `VulkanGameApplication.onRender`'s doc comment -- identical contract. */
    protected open fun onRender() {
        renderSystem.update(world, 0f)
    }

    /** See `VulkanGameApplication.resolveRenderable`'s doc comment -- identical contract. */
    protected open fun resolveRenderable(request: SceneRenderableRequest): MeshRenderer {
        val resolvedMesh = meshInstances[request.meshRenderer.mesh]
            ?: error("Unsupported scene mesh '${request.meshRenderer.mesh}'.")
        return MeshRenderer(resolvedMesh, material)
    }

    /** See `VulkanGameApplication.onSceneReady`'s doc comment -- identical contract. */
    protected open fun onSceneReady() {}

    private suspend fun setupWebGpu(window: Any) {
        graphicsDevice.create(window)
        swapchainManager.create()
        material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            DescriptorSetLayoutHandle(0),
            readResourceBytes(vertexShaderResourcePath),
            ByteArray(0),
            vertexStride
        )
        renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            0L,
            MAX_FRAMES_IN_FLIGHT
        )
        meshes.forEach { (name, geometry) ->
            meshInstances[name] = Mesh(graphicsDevice, {}, geometry.vertices, geometry.indices)
        }

        world = World()
        scene = SceneLoader.loadFromResource(scenePath).instantiate(world)
        scene.attachRenderableComponents(::resolveRenderable)
        renderSystem = RenderSystem(renderer)
        onSceneReady()

        isReady = true
    }

    private fun destroy() {
        renderer.destroy()
        swapchainManager.destroy()
        meshInstances.values.forEach { it.destroy() }
        renderPipeline.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 1
    }
}
