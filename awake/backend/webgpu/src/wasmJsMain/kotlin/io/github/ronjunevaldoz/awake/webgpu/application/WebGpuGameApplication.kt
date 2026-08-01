// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameShaderSet
import io.github.ronjunevaldoz.awake.engine.application.GenericGameApplication
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager

/**
 * Reusable WebGPU game bootstrap -- wasmJs counterpart to `VulkanGameApplication`
 * (`awake-backend-vulkan`), see that class's doc comment and docs/MVP_PLAN.md's Decision
 * Log ("reusable-Application gap fix", "GenericGameApplication", and "GenericGameApplication
 * a standalone render bootstrap") for the full rationale. Mirrors `VulkanGameApplication`'s
 * constructor/lifecycle shape exactly.
 *
 * `create`'s `surface` parameter must be a pre-resolved `io.ygdrasil.webgpu.WGPUContext`,
 * not a raw canvas (see [GraphicsDevice]'s own doc comment) -- the platform entry point
 * (`main.kt`) resolves it via `canvasContextRenderer()` + `surface.configure()` in its own
 * coroutine before calling `create`, since that resolution is itself `suspend`.
 */
class WebGpuGameApplication(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexStride: Int,
    game: Game,
    private val vertexShaderEntryPoint: String = DEFAULT_VERTEX_SHADER_ENTRY_POINT,
    private val fragmentShaderEntryPoint: String = DEFAULT_FRAGMENT_SHADER_ENTRY_POINT
) : GenericGameApplication(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexStride,
    game
) {
    constructor(
        shaderSet: GameShaderSet,
        vertexStride: Int,
        game: Game
    ) : this(
        vertexShaderResourcePath = shaderSet.webGpu.vertexResourcePath,
        fragmentShaderResourcePath = shaderSet.webGpu.fragmentResourcePath,
        vertexStride = vertexStride,
        game = game,
        vertexShaderEntryPoint = shaderSet.webGpu.vertexEntryPoint,
        fragmentShaderEntryPoint = shaderSet.webGpu.fragmentEntryPoint
    )

    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private lateinit var lineRenderPipeline: LineRenderPipeline

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.create()
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            DescriptorSetLayoutHandle(0),
            readResourceBytes(vertexShaderResourcePath),
            ByteArray(0),
            vertexStride,
            vertexShaderEntryPoint,
            fragmentShaderEntryPoint
        )
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(DEBUG_LINE_SHADER_RESOURCE_PATH)
        )
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            lineRenderPipeline,
            readResourceBytes(UI_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_GLYPH_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_TEXTURE_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_ROUNDED_QUAD_SHADER_RESOURCE_PATH),
            0L,
            MAX_FRAMES_IN_FLIGHT
        )

        return BackendResources(
            renderer = renderer,
            viewportSize = {
                val renderingContext = graphicsDevice.wgpuContext.renderingContext
                renderingContext.width.toFloat() to renderingContext.height.toFloat()
            }
        )
    }

    override fun destroyBackend() {
        renderer.destroy()
        swapchainManager.destroy()
        renderPipeline.destroy()
        lineRenderPipeline.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 1
        const val DEFAULT_VERTEX_SHADER_ENTRY_POINT = "vertexMain"
        const val DEFAULT_FRAGMENT_SHADER_ENTRY_POINT = "fragmentMain"

        // Bundled once in this module (`awake-backend-webgpu/src/wasmJsMain/resources`),
        // not duplicated per consumer -- see VulkanGameApplication's identical companion
        // constant doc comment for the full rationale.
        const val UI_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_quad.wgsl"
        const val UI_GLYPH_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_glyph.wgsl"
        const val UI_TEXTURE_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_texture.wgsl"
        const val UI_ROUNDED_QUAD_SHADER_RESOURCE_PATH = "assets/shader/webgpu/ui_rounded_quad.wgsl"
        const val DEBUG_LINE_SHADER_RESOURCE_PATH = "assets/shader/webgpu/debug_line.wgsl"
    }
}
