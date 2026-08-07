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
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPolygonMode
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShadowRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.surfaceFramebufferExtent
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.ShadowMap

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
    /** An optional second 3D pipeline for GPU-skinned meshes -- registered into `Renderer
     * .pipelinesByFormat` under [skinnedVertexFormat] so a `MeshRenderer` entity using that
     * format draws through it instead of the primary pipeline. `null` (default) for every
     * game that has no skinned content -- most of them -- which skips building it entirely,
     * same as [GenericGameApplication]'s existing single-pipeline shape for every other game. */
    private val skinnedShaderSet: GameShaderSet? = null,
    private val skinnedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorSkin,
    /** An optional third 3D pipeline for meshes with a real `baseColorTexture` -- same
     * `pipelinesByFormat` registration shape as [skinnedShaderSet], under
     * [texturedVertexFormat]. Same "null by default, opt in per game" shape as
     * [skinnedShaderSet]. */
    private val texturedShaderSet: GameShaderSet? = null,
    private val texturedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorUv,
    /** Builds a `VK_POLYGON_MODE_LINE` companion pipeline for the primary pipeline and for
     * [skinnedShaderSet]/[texturedShaderSet] (whichever are actually present), reusing the
     * exact same loaded shaders/vertex layout as their filled counterpart -- toggled on/off
     * per frame via `Renderer.wireframe`. `false` by default so a game that never uses it
     * doesn't pay for the extra pipeline objects, same "opt in per game" shape as
     * [skinnedShaderSet]/[texturedShaderSet]. Requires the device's `fillModeNonSolid`
     * feature -- see `RenderPipeline`'s own `polygonMode` doc comment for why that's already
     * satisfied whenever the GPU supports it, no extra wiring needed here. */
    private val wireframeSupport: Boolean = false,
    /** Opts into the shadow depth pre-pass (see `ShadowMap`/`ShadowRenderPipeline`/
     * `Renderer.shadowMap`'s own doc comments) -- `null` (default) is the "shadows never
     * existed" path: no `ShadowMap` is built, [Material]'s descriptor set layout stays its
     * original 3-binding shape, and [primaryVertexFormat]'s uniform buffer stays 24 floats.
     * The vertex-shader entry point here must read the SAME [vertexFormat] vertex attributes
     * as [vertexShaderResourcePath] itself (see `shadow_depth.wgsl`), since both draw the
     * exact same meshes. */
    private val shadowShaderSet: GameShaderSet? = null
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
        texturedVertexFormat: VertexFormat = VertexFormat.PositionNormalColorUv,
        wireframeSupport: Boolean = false,
        shadowShaderSet: GameShaderSet? = null
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
        texturedVertexFormat = texturedVertexFormat,
        wireframeSupport = wireframeSupport,
        shadowShaderSet = shadowShaderSet
    )

    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private var skinnedRenderPipeline: RenderPipeline? = null
    private var texturedRenderPipeline: RenderPipeline? = null
    private var wireframeRenderPipeline: RenderPipeline? = null
    private var wireframeSkinnedRenderPipeline: RenderPipeline? = null
    private var wireframeTexturedRenderPipeline: RenderPipeline? = null
    private lateinit var lineRenderPipeline: LineRenderPipeline
    private lateinit var transferContext: TransferContext
    private var shadowMap: ShadowMap? = null
    private var shadowRenderPipeline: ShadowRenderPipeline? = null

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
        // Built before pipelineLayoutMaterial/renderPipeline: both need this ShadowMap's
        // existence (not its content -- the shadow pass hasn't run its first frame yet) to
        // decide whether their shared descriptor set layout declares the extra shadow
        // bindings (see Material.kt's own shadowMap doc comment).
        shadowMap = shadowShaderSet?.let { ShadowMap(graphicsDevice) }
        pipelineLayoutMaterial = Material(graphicsDevice, shadowMap = shadowMap)

        // fillPipeline(shaders, format) builds the normal pipeline plus, when wireframeSupport
        // is on, a VK_POLYGON_MODE_LINE companion sharing the exact same loaded ShaderPair --
        // one resource read instead of two, and the pair this app's own field assignments
        // below destructure.
        suspend fun fillAndWireframePipeline(
            vertexPath: String,
            fragmentPath: String,
            format: VertexFormat,
            vertexEntryPoint: String,
            fragmentEntryPoint: String
        ): Pair<RenderPipeline, RenderPipeline?> {
            val shaders = loadShaderPair(vertexPath, fragmentPath)
            val fill = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineLayoutMaterial.descriptorSetLayout,
                shaders,
                format,
                vertexEntryPoint,
                fragmentEntryPoint
            )
            val wireframe = if (wireframeSupport) {
                RenderPipeline(
                    graphicsDevice,
                    swapchainManager,
                    pipelineLayoutMaterial.descriptorSetLayout,
                    shaders,
                    format,
                    vertexEntryPoint,
                    fragmentEntryPoint,
                    polygonMode = VkPolygonMode.VK_POLYGON_MODE_LINE
                )
            } else {
                null
            }
            return fill to wireframe
        }

        val (fill, wireframe) = fillAndWireframePipeline(
            vertexShaderResourcePath,
            fragmentShaderResourcePath,
            vertexFormat,
            vertexShaderEntryPoint,
            fragmentShaderEntryPoint
        )
        renderPipeline = fill
        wireframeRenderPipeline = wireframe

        skinnedShaderSet?.let { shaderSet ->
            val (skinnedFill, skinnedWireframe) = fillAndWireframePipeline(
                shaderSet.vulkan.vertexResourcePath,
                shaderSet.vulkan.fragmentResourcePath,
                skinnedVertexFormat,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint
            )
            skinnedRenderPipeline = skinnedFill
            wireframeSkinnedRenderPipeline = skinnedWireframe
        }

        texturedShaderSet?.let { shaderSet ->
            val (texturedFill, texturedWireframe) = fillAndWireframePipeline(
                shaderSet.vulkan.vertexResourcePath,
                shaderSet.vulkan.fragmentResourcePath,
                texturedVertexFormat,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint
            )
            texturedRenderPipeline = texturedFill
            wireframeTexturedRenderPipeline = texturedWireframe
        }
        shadowRenderPipeline = shadowMap?.let { map ->
            val shaderSet = requireNotNull(shadowShaderSet)
            ShadowRenderPipeline(
                graphicsDevice,
                map.renderPass,
                pipelineLayoutMaterial.descriptorSetLayout,
                loadShaderPair(shaderSet.vulkan.vertexResourcePath, shaderSet.vulkan.fragmentResourcePath),
                // Same vertex layout as the primary pipeline -- it draws the same meshes.
                vertexFormat,
                map.size,
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
        val additionalPipelinesByFormat = buildMap {
            skinnedRenderPipeline?.let { put(skinnedVertexFormat, it) }
            texturedRenderPipeline?.let { put(texturedVertexFormat, it) }
        }
        val wireframePipelinesByFormat = buildMap {
            wireframeRenderPipeline?.let { put(vertexFormat, it) }
            wireframeSkinnedRenderPipeline?.let { put(skinnedVertexFormat, it) }
            wireframeTexturedRenderPipeline?.let { put(texturedVertexFormat, it) }
        }
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            additionalPipelinesByFormat,
            lineRenderPipeline,
            transferContext,
            loadShaderPair(UI_VERTEX_SHADER_RESOURCE_PATH, UI_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH, UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH, UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH, UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH),
            MAX_FRAMES_IN_FLIGHT,
            wireframePipelinesByFormat,
            shadowMap,
            shadowRenderPipeline
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
        wireframeRenderPipeline?.destroy()
        wireframeSkinnedRenderPipeline?.destroy()
        wireframeTexturedRenderPipeline?.destroy()
        shadowRenderPipeline?.destroy()
        shadowMap?.destroy()
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
