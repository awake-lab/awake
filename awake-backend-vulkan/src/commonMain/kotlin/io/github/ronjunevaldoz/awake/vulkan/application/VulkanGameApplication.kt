// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.application.GenericGameApplication
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline

/**
 * Reusable Vulkan game bootstrap (see docs/MVP_PLAN.md's Decision Log: "reusable-Application
 * gap fix", and the later "GenericGameApplication" entry documenting why the backend-neutral
 * parts moved out to `awake-engine-game`). A new game supplies its mesh geometry/texture/
 * scene path via the constructor and, if it needs game-specific per-frame logic (player
 * movement, AI, camera control), overrides `onFixedUpdate`/`onRender` -- calling `super`
 * first so [GenericGameApplication]'s scene systems still run.
 *
 * `texture` is optional: when `null`, a trivial 1x1 white pixel is bound instead, so the
 * existing shader/descriptor-set contract (always expects a combined-image-sampler) doesn't
 * need a second no-texture pipeline variant.
 */
abstract class VulkanGameApplication(
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
    private lateinit var fontTexture: Texture
    private lateinit var transferContext: TransferContext
    private lateinit var textureInstance: Texture

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.create()
        val material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            material.descriptorSetLayout,
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
        uiRenderPipeline = UiRenderPipeline(
            graphicsDevice,
            swapchainManager,
            readResourceBytes(UI_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_FRAGMENT_SHADER_RESOURCE_PATH)
        )
        transferContext = TransferContext(graphicsDevice)
        fontTexture = Texture(
            graphicsDevice,
            transferContext::runOneTimeCommands,
            font.atlasPixelsRgba,
            font.atlasWidth,
            font.atlasHeight
        )
        uiGlyphRenderPipeline = UiGlyphRenderPipeline(
            graphicsDevice,
            swapchainManager,
            uiRenderPipeline.renderPass,
            readResourceBytes(UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH),
            readResourceBytes(UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH),
            fontTexture
        )
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            uiRenderPipeline,
            uiGlyphRenderPipeline,
            lineRenderPipeline,
            transferContext.commandPool.handle,
            MAX_FRAMES_IN_FLIGHT
        )
        val meshInstances = meshes.mapValues { (_, geometry) ->
            Mesh(
                graphicsDevice,
                transferContext::runOneTimeCommands,
                geometry.vertices,
                geometry.indices
            )
        }
        val effectiveTexture = texture ?: PLACEHOLDER_TEXTURE
        textureInstance = Texture(
            graphicsDevice,
            transferContext::runOneTimeCommands,
            effectiveTexture.data,
            effectiveTexture.width,
            effectiveTexture.height
        )
        material.createResources(textureInstance)
        swapchainManager.createSyncObjects()

        return BackendResources(
            renderer = renderer,
            meshInstances = meshInstances,
            material = material,
            viewportSize = { swapchainManager.extent.width.toFloat() to swapchainManager.extent.height.toFloat() }
        )
    }

    override fun destroyBackend() {
        renderer.destroy()
        swapchainManager.destroy()
        swapchainManager.destroySyncObjects()
        transferContext.destroy()
        meshInstances.values.forEach { it.destroy() }
        textureInstance.destroy()
        material.destroy()
        renderPipeline.destroy()
        lineRenderPipeline.destroy()
        uiRenderPipeline.destroy()
        uiGlyphRenderPipeline.destroy()
        fontTexture.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2
        val PLACEHOLDER_TEXTURE = TextureAsset(byteArrayOf(-1, -1, -1, -1), 1, 1)

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
        const val DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.vert.spv"
        const val DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/debug_line.frag.spv"
    }
}
