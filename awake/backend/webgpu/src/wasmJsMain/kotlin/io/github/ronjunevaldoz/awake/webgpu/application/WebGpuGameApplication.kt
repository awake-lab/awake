// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.game.Game
import io.github.ronjunevaldoz.awake.engine.game.GameApplication
import io.github.ronjunevaldoz.awake.engine.game.GameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.GPUPrimitiveTopology

/**
 * Reusable WebGPU game bootstrap -- wasmJs counterpart to `VulkanGameApplication`
 * (`awake-backend-vulkan`), see that class's doc comment and docs/MVP_PLAN.md's Decision
 * Log ("reusable-Application gap fix", "GameApplication", and "GameApplication
 * a standalone render bootstrap") for the full rationale. Mirrors `VulkanGameApplication`'s
 * constructor/lifecycle shape exactly.
 *
 * `create`'s `surface` parameter must be a pre-resolved `io.ygdrasil.webgpu.WGPUContext`,
 * not a raw canvas (see [GraphicsDevice]'s own doc comment) -- the platform entry point
 * (`main.kt`) resolves it via `canvasContextRenderer()` + `surface.configure()` in its own
 * coroutine before calling `create`, since that resolution is itself `suspend`.
 */
open class WebGpuGameApplication(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    game: Game,
    private val vertexShaderEntryPoint: String = DEFAULT_VERTEX_SHADER_ENTRY_POINT,
    private val fragmentShaderEntryPoint: String = DEFAULT_FRAGMENT_SHADER_ENTRY_POINT,
    /** Builds a `GPUPrimitiveTopology.LineList` companion of the primary pipeline, reusing the
     * exact same loaded shader source/vertex layout -- toggled on/off per frame via
     * `Renderer.wireframe`. `false` by default so a game that never uses it doesn't pay for
     * the extra pipeline object, mirroring `VulkanGameApplication`'s `wireframeSupport`. */
    private val wireframeSupport: Boolean = false,
    /** Extra 3D pipelines keyed by the vertex format each one draws -- registered into
     * `Renderer.additionalPipelines`, so a `MeshRenderer` entity using that format draws
     * through its own pipeline instead of the primary one. Mirrors
     * `VulkanGameApplication.additionalPipelines` (see its doc comment); empty by default. */
    private val additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
) : GameApplication(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexFormat,
    game,
) {
    constructor(
        shaderSet: GameShaderSet,
        vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
        game: Game,
        wireframeSupport: Boolean = false,
        additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
    ) : this(
        vertexShaderResourcePath = shaderSet.webGpu.vertexResourcePath,
        fragmentShaderResourcePath = shaderSet.webGpu.fragmentResourcePath,
        vertexFormat = vertexFormat,
        game = game,
        vertexShaderEntryPoint = shaderSet.webGpu.vertexEntryPoint,
        fragmentShaderEntryPoint = shaderSet.webGpu.fragmentEntryPoint,
        wireframeSupport = wireframeSupport,
        additionalPipelines = additionalPipelines,
    )

    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private var wireframeRenderPipeline: RenderPipeline? = null
    private val additionalRenderPipelines = mutableMapOf<VertexFormat, RenderPipeline>()
    private lateinit var lineRenderPipeline: LineRenderPipeline

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.create()
        val vertShaderCode = readResourceBytes(vertexShaderResourcePath)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            DescriptorSetLayoutHandle(0),
            vertShaderCode,
            ByteArray(0),
            vertexFormat,
            vertexShaderEntryPoint,
            fragmentShaderEntryPoint,
        )
        wireframeRenderPipeline = if (wireframeSupport) {
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                DescriptorSetLayoutHandle(0),
                vertShaderCode,
                ByteArray(0),
                vertexFormat,
                vertexShaderEntryPoint,
                fragmentShaderEntryPoint,
                topology = GPUPrimitiveTopology.LineList,
            )
        } else {
            null
        }
        // No LineList companion per additional format -- those keep drawing filled while
        // Renderer.wireframe is on (see RendererDraw3D's own comment).
        additionalPipelines.forEach { (format, shaderSet) ->
            additionalRenderPipelines[format] = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                DescriptorSetLayoutHandle(0),
                readResourceBytes(shaderSet.webGpu.vertexResourcePath),
                ByteArray(0),
                format,
                shaderSet.webGpu.vertexEntryPoint,
                shaderSet.webGpu.fragmentEntryPoint,
            )
        }
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(DEBUG_LINE_SHADER_RESOURCE_PATH),
        )
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            vertexFormat,
            lineRenderPipeline,
            readResourceBytes(UI_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_GLYPH_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_TEXTURE_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_ROUNDED_QUAD_SHADER_RESOURCE_PATH),
            0L,
            MAX_FRAMES_IN_FLIGHT,
            wireframeRenderPipeline,
            additionalRenderPipelines.toMap(),
        )

        return BackendResources(
            renderer = renderer,
            viewportSize = {
                val renderingContext = graphicsDevice.wgpuContext.renderingContext
                renderingContext.width.toFloat() to renderingContext.height.toFloat()
            },
        )
    }

    override fun destroyBackend() {
        renderer.destroy()
        swapchainManager.destroy()
        renderPipeline.destroy()
        wireframeRenderPipeline?.destroy()
        additionalRenderPipelines.values.forEach { it.destroy() }
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
