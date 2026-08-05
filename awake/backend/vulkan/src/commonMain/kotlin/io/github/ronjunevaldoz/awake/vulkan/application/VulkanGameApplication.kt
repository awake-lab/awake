// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameShaderSet
import io.github.ronjunevaldoz.awake.engine.application.GenericGameApplication
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.surfaceFramebufferExtent
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * Reusable Vulkan game bootstrap (see docs/MVP_PLAN.md's Decision Log: "reusable-Application
 * gap fix", "GenericGameApplication", and "GenericGameApplication a standalone render
 * bootstrap"). A new game supplies its shader/vertex-layout via the constructor and its own
 * behavior via the injected [game] (`Game.ready(renderer)`/`Game.render(...)`) -- this class
 * only builds/tears down Vulkan's GPU resources, it never knows what the game actually draws.
 */
class VulkanGameApplication(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    game: Game,
    private val vertexShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    private val fragmentShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    /** An optional second 3D pipeline for GPU-skinned meshes -- see
     * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.drawSkinnedMesh]'s doc comment.
     * `null` (default) for every game that has no skinned content -- most of them -- which
     * skips building it entirely, same as [GenericGameApplication]'s existing single-pipeline
     * shape for every other game. */
    private val skinnedShaderSet: GameShaderSet? = null,
    private val skinnedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorSkin,
    /** An optional third 3D pipeline for meshes with a real `baseColorTexture` -- see
     * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.drawTexturedMesh]'s doc comment.
     * Same "null by default, opt in per game" shape as [skinnedShaderSet]. */
    private val texturedShaderSet: GameShaderSet? = null,
    private val texturedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorUv
) : GenericGameApplication(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexFormat,
    game
) {
    constructor(
        shaderSet: GameShaderSet,
        vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
        game: Game,
        skinnedShaderSet: GameShaderSet? = null,
        skinnedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorSkin,
        texturedShaderSet: GameShaderSet? = null,
        texturedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorUv
    ) : this(
        vertexShaderResourcePath = shaderSet.vulkan.vertexResourcePath,
        fragmentShaderResourcePath = shaderSet.vulkan.fragmentResourcePath,
        vertexFormat = vertexFormat,
        game = game,
        vertexShaderEntryPoint = shaderSet.vulkan.vertexEntryPoint,
        fragmentShaderEntryPoint = shaderSet.vulkan.fragmentEntryPoint,
        skinnedShaderSet = skinnedShaderSet,
        skinnedVertexFormat = skinnedVertexFormat,
        texturedShaderSet = texturedShaderSet,
        texturedVertexFormat = texturedVertexFormat
    )

    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private var skinnedRenderPipeline: RenderPipeline? = null
    private var texturedRenderPipeline: RenderPipeline? = null
    private lateinit var lineRenderPipeline: LineRenderPipeline
    private lateinit var transferContext: TransferContext

    /** Only its [Material.descriptorSetLayout] is ever used -- needed to build
     * [renderPipeline]'s pipeline layout before any real material exists. `createResources`
     * is deliberately never called on this instance (real materials are built on demand via
     * `Renderer.createMaterial`), so [destroyBackend] must only tear down the layout, not the
     * full [Material.destroy] (which would try to destroy a uniform buffer/descriptor pool
     * that was never created). */
    private lateinit var pipelineLayoutMaterial: Material

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(
            graphicsDevice,
            MAX_FRAMES_IN_FLIGHT,
            surfaceExtentProvider = { surfaceFramebufferExtent(window) }
        )
        swapchainManager.create()
        pipelineLayoutMaterial = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            loadShaderPair(vertexShaderResourcePath, fragmentShaderResourcePath),
            vertexFormat,
            vertexShaderEntryPoint,
            fragmentShaderEntryPoint
        )
        skinnedRenderPipeline = skinnedShaderSet?.let { shaderSet ->
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineLayoutMaterial.descriptorSetLayout,
                loadShaderPair(shaderSet.vulkan.vertexResourcePath, shaderSet.vulkan.fragmentResourcePath),
                skinnedVertexFormat,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint
            )
        }
        texturedRenderPipeline = texturedShaderSet?.let { shaderSet ->
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineLayoutMaterial.descriptorSetLayout,
                loadShaderPair(shaderSet.vulkan.vertexResourcePath, shaderSet.vulkan.fragmentResourcePath),
                texturedVertexFormat,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint
            )
        }
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            loadShaderPair(DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH, DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH),
            MAX_FRAMES_IN_FLIGHT
        )
        transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            skinnedRenderPipeline,
            texturedRenderPipeline,
            lineRenderPipeline,
            transferContext,
            loadShaderPair(UI_VERTEX_SHADER_RESOURCE_PATH, UI_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH, UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH, UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH, UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH),
            MAX_FRAMES_IN_FLIGHT
        )
        swapchainManager.createSyncObjects()

        return BackendResources(
            renderer = renderer,
            viewportSize = { swapchainManager.extent.width.toFloat() to swapchainManager.extent.height.toFloat() }
        )
    }

    override fun destroyBackend() {
        // The GPU may still have the last frame's work in flight the instant the app's
        // window-close loop exits -- destroying framebuffers/image views/pipelines while
        // they're still in use is undefined behavior (confirmed by a real desktop
        // close-triggered crash without this wait). Renderer.draw() already waits idle at
        // the end of every frame it successfully completes, but a frame that bailed out
        // early (e.g. mid-resize, see Renderer.recreateSwapChain's doc comment) skips that
        // wait, so this can't rely on the last draw() call alone.
        VulkanBuffers.vkDeviceWaitIdle(graphicsDevice.device)
        renderer.destroy()
        swapchainManager.destroy()
        swapchainManager.destroySyncObjects()
        transferContext.destroy()
        VulkanDescriptors.vkDestroyDescriptorSetLayout(
            graphicsDevice.device,
            pipelineLayoutMaterial.descriptorSetLayout.handle
        )
        renderPipeline.destroy()
        skinnedRenderPipeline?.destroy()
        texturedRenderPipeline?.destroy()
        lineRenderPipeline.destroy()
        graphicsDevice.destroy()
    }

    /** Reads [vertexPath]/[fragmentPath] into one [ShaderPair] -- collapses the repeated
     * `ShaderPair(readResourceBytes(x), readResourceBytes(y))` shape at every pipeline
     * construction call site above into a single-line call. */
    private suspend fun loadShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
        ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2
        const val DEFAULT_SHADER_ENTRY_POINT = "main"

        // Identical for every subclass, so bundled once here rather than per consumer.
        // Per-app shaders vary per game and stay constructor-injected instead.
        const val UI_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.vert.spv"
        const val UI_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.frag.spv"
        const val UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.vert.spv"
        const val UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.frag.spv"
        const val UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_texture.vert.spv"
        const val UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_texture.frag.spv"
        const val UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_rounded_quad.vert.spv"
        const val UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_rounded_quad.frag.spv"
        const val DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.vert.spv"
        const val DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.frag.spv"
    }
}
