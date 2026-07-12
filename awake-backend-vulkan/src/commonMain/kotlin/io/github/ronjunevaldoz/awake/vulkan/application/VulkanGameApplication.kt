// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GenericGameApplication
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
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
    vertexStride: Int,
    game: Game
) : GenericGameApplication(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexStride,
    game
) {
    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
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
        swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.create()
        pipelineLayoutMaterial = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            readResourceBytes(vertexShaderResourcePath),
            readResourceBytes(fragmentShaderResourcePath),
            vertexStride
        )
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            readResourceBytes(DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH)
        )
        transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            lineRenderPipeline,
            transferContext,
            readResourceBytes(UI_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_FRAGMENT_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH),
            MAX_FRAMES_IN_FLIGHT
        )
        swapchainManager.createSyncObjects()

        return BackendResources(
            renderer = renderer,
            viewportSize = { swapchainManager.extent.width.toFloat() to swapchainManager.extent.height.toFloat() }
        )
    }

    override fun destroyBackend() {
        renderer.destroy()
        swapchainManager.destroy()
        swapchainManager.destroySyncObjects()
        transferContext.destroy()
        VulkanDescriptors.vkDestroyDescriptorSetLayout(
            graphicsDevice.device,
            pipelineLayoutMaterial.descriptorSetLayout.handle
        )
        renderPipeline.destroy()
        lineRenderPipeline.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2

        // Bundled once in this module (`awake-backend-vulkan/src/commonMain/resources`),
        // not duplicated per consumer -- these files are identical for every
        // VulkanGameApplication subclass, so they live with the framework code that reads
        // them and reach every consumer transitively via normal KMP resource bundling (see
        // docs/MVP_PLAN.md's custom-UI decision log entry, and AGENTS.md's resource-
        // bundling rule). Per-app shaders (each subclass's own vertexShaderResourcePath/
        // fragmentShaderResourcePath) are different: they vary per game, so they stay
        // constructor-injected and consumer-owned.
        const val UI_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.vert.spv"
        const val UI_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.frag.spv"
        const val UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.vert.spv"
        const val UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.frag.spv"
        const val UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_texture.vert.spv"
        const val UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_texture.frag.spv"
        const val DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.vert.spv"
        const val DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.frag.spv"
    }
}
