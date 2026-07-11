// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.application.GenericGameApplication
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.webgpu.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRenderPipeline

/**
 * Reusable WebGPU game bootstrap -- wasmJs counterpart to `VulkanGameApplication`
 * (`awake-backend-vulkan`), see that class's doc comment and docs/MVP_PLAN.md's Decision
 * Log ("reusable-Application gap fix", and the later "GenericGameApplication" entry) for the
 * full rationale. Mirrors `VulkanGameApplication`'s constructor/lifecycle shape exactly, but
 * the bootstrap sequence genuinely omits `TransferContext`/`Texture`/
 * `Material.createResources` -- this backend's `Material` is still a placeholder
 * (`TODO()`-only) and `Mesh`'s upload doesn't need a staging-buffer/command-buffer dance the
 * way Vulkan's does, so `texture` is accepted for constructor-shape parity with
 * `VulkanGameApplication` but never actually used here.
 *
 * `create`'s `surface` parameter must be a pre-resolved `io.ygdrasil.webgpu.WGPUContext`,
 * not a raw canvas (see [GraphicsDevice]'s own doc comment) -- the platform entry point
 * (`main.kt`) resolves it via `canvasContextRenderer()` + `surface.configure()` in its own
 * coroutine before calling `create`, since that resolution is itself `suspend`.
 */
abstract class WebGpuGameApplication(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexStride: Int,
    meshes: Map<String, MeshGeometry>,
    texture: TextureAsset? = null,
    scenePath: String
) : GenericGameApplication(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexStride,
    meshes,
    texture,
    scenePath
) {
    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private lateinit var uiRenderPipeline: UiRenderPipeline
    private lateinit var uiGlyphRenderPipeline: UiGlyphRenderPipeline
    private lateinit var lineRenderPipeline: LineRenderPipeline

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.create()
        val material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            DescriptorSetLayoutHandle(0),
            readResourceBytes(vertexShaderResourcePath),
            ByteArray(0),
            vertexStride
        )
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(DEBUG_LINE_SHADER_RESOURCE_PATH)
        )
        uiRenderPipeline = UiRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(UI_SHADER_RESOURCE_PATH)
        )
        uiGlyphRenderPipeline = UiGlyphRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(UI_GLYPH_SHADER_RESOURCE_PATH),
            font
        )
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            uiRenderPipeline,
            uiGlyphRenderPipeline,
            lineRenderPipeline,
            0L,
            MAX_FRAMES_IN_FLIGHT
        )
        val meshInstances = meshes.mapValues { (_, geometry) ->
            Mesh(graphicsDevice, {}, geometry.vertices, geometry.indices)
        }

        return BackendResources(
            renderer = renderer,
            meshInstances = meshInstances,
            material = material,
            viewportSize = {
                val renderingContext = graphicsDevice.wgpuContext.renderingContext
                renderingContext.width.toFloat() to renderingContext.height.toFloat()
            }
        )
    }

    override fun destroyBackend() {
        renderer.destroy()
        swapchainManager.destroy()
        meshInstances.values.forEach { it.destroy() }
        renderPipeline.destroy()
        lineRenderPipeline.destroy()
        uiRenderPipeline.destroy()
        uiGlyphRenderPipeline.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 1

        // Bundled once in this module (`awake-backend-webgpu/src/wasmJsMain/resources`),
        // not duplicated per consumer -- see VulkanGameApplication's identical companion
        // constant doc comment for the full rationale.
        const val UI_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_quad.wgsl"
        const val UI_GLYPH_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_glyph.wgsl"
        const val DEBUG_LINE_SHADER_RESOURCE_PATH = "assets/shader/webgpu/debug_line.wgsl"
    }
}
