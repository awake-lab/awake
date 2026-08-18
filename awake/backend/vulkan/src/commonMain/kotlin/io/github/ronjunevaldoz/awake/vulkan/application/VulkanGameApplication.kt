// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.game.Game
import io.github.ronjunevaldoz.awake.engine.game.GameApplication
import io.github.ronjunevaldoz.awake.engine.game.GameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.debug.SkyboxRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPolygonMode
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.SkinnedInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineVariant
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShadowRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.surfaceFramebufferExtent
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.ShadowMap

/**
 * Reusable Vulkan game bootstrap (see docs/MVP_PLAN.md's Decision Log: "reusable-Application
 * gap fix", "GameApplication", and "GameApplication a standalone render
 * bootstrap"). A new game supplies its shader/vertex-layout via the constructor and its own
 * behavior via the injected [game] (`Game.ready(renderer)`/`Game.render(...)`) -- this class
 * only builds/tears down Vulkan's GPU resources, it never knows what the game actually draws.
 */
open class VulkanGameApplication(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    game: Game,
    private val vertexShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    private val fragmentShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    /** Extra 3D pipelines keyed by the vertex format each one draws -- registered into
     * `Renderer.pipelinesByFormat`, so a `MeshRenderer` entity using that format draws through
     * its own pipeline instead of the primary one. Empty (default) for games with only the
     * primary pipeline. Keyed by format rather than named per kind (skinned/textured/...) on
     * purpose: this class builds GPU resources and must not know what the game draws, and a
     * per-kind pair of parameters grew this constructor by two every time a kind was added. */
    private val additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
    /** Builds a `VK_POLYGON_MODE_LINE` companion pipeline for the primary pipeline and for
     * [additionalPipelines] (whichever are present), reusing the
     * exact same loaded shaders/vertex layout as their filled counterpart -- toggled on/off
     * per frame via `Renderer.wireframe`. `false` by default so a game that never uses it
     * doesn't pay for the extra pipeline objects, same "opt in per game" shape as
     * [additionalPipelines]. Requires the device's `fillModeNonSolid`
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
    private val shadowShaderSet: GameShaderSet? = null,
    /** Opts into GPU instancing for [vertexFormat] (see `Renderer.instancedPipelinesByFormat`):
     * builds ONE extra pipeline from this shader set with a second, instance-rate vertex binding.
     * The shader's uniform block must hold `viewProjection` (not `mvp`) and it must declare the
     * 4 instance-matrix attributes -- see `instanced.wgsl`. `null` (default) means no instanced
     * pipeline is built at all and an `InstancedMeshRenderer` entity simply doesn't draw. */
    private val instancedShaderSet: GameShaderSet? = null,
    /** Opts into ANIMATED GPU instancing (see `Renderer.skinnedInstancedPipelinesByFormat`):
     * builds ONE extra pipeline from this shader set with both the instance-rate model-matrix
     * binding and a `@group(1)` joint-palette storage-buffer set -- see
     * `skinned_instanced.wgsl`. Always built for [VertexFormat.PositionNormalColorSkin] rather
     * than for the primary [vertexFormat]: skinning needs joint indices/weights per vertex, so
     * that is the only format this shader can ever read. `null` (default) means no such
     * pipeline exists and an `InstancedSkinnedMeshRenderer` entity simply doesn't draw. */
    private val skinnedInstancedShaderSet: GameShaderSet? = null,
    /** Opts into the procedural sky (see `SkyboxRenderPipeline`/`Renderer.showEnvironment`):
     * builds ONE extra pipeline from this shader set against the primary pipeline's own render
     * pass. `null` (default) means no skybox pipeline is built at all and
     * `Renderer.showEnvironment` stays an inert flag -- which is also why this is opt-in rather
     * than always-on: `skybox.wgsl` ships in `awake:asset:shaders`, so only a module that syncs
     * that directory actually has the compiled SPIR-V on its resource path. */
    private val skyboxShaderSet: GameShaderSet? = null,
    /** Opts into billboard-particle instancing (see `Renderer.instancedPipelinesByFormat`
     * keyed by [VertexFormat.PositionUv]): builds ONE extra pipeline from this shader set with
     * `instanced = true, instanceAlpha = true, blendEnabled = true, depthWriteEnabled = false`
     * -- see `particle.wgsl`. `null` (default) means no such pipeline exists and a
     * `ParticleEmitter` entity simply doesn't draw. */
    private val particleShaderSet: GameShaderSet? = null,
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
        additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
        wireframeSupport: Boolean = false,
        shadowShaderSet: GameShaderSet? = null,
        instancedShaderSet: GameShaderSet? = null,
        skinnedInstancedShaderSet: GameShaderSet? = null,
        skyboxShaderSet: GameShaderSet? = null,
        particleShaderSet: GameShaderSet? = null,
    ) : this(
        vertexShaderResourcePath = shaderSet.vulkan.vertexResourcePath,
        fragmentShaderResourcePath = shaderSet.vulkan.fragmentResourcePath,
        vertexFormat = vertexFormat,
        game = game,
        vertexShaderEntryPoint = shaderSet.vulkan.vertexEntryPoint,
        fragmentShaderEntryPoint = shaderSet.vulkan.fragmentEntryPoint,
        additionalPipelines = additionalPipelines,
        wireframeSupport = wireframeSupport,
        shadowShaderSet = shadowShaderSet,
        instancedShaderSet = instancedShaderSet,
        skinnedInstancedShaderSet = skinnedInstancedShaderSet,
        skyboxShaderSet = skyboxShaderSet,
        particleShaderSet = particleShaderSet,
    )

    private lateinit var graphicsDevice: GraphicsDevice
    private lateinit var swapchainManager: SwapchainManager
    private lateinit var renderPipeline: RenderPipeline
    private var wireframeRenderPipeline: RenderPipeline? = null
    private val additionalRenderPipelines = mutableMapOf<VertexFormat, RenderPipeline>()
    private val wireframeAdditionalRenderPipelines = mutableMapOf<VertexFormat, RenderPipeline>()
    private lateinit var lineRenderPipeline: LineRenderPipeline
    private var skyboxRenderPipeline: SkyboxRenderPipeline? = null
    private lateinit var transferContext: TransferContext
    private var shadowMap: ShadowMap? = null
    private var shadowRenderPipeline: ShadowRenderPipeline? = null
    private var instancedRenderPipeline: RenderPipeline? = null
    private var skinnedInstancedRenderPipeline: RenderPipeline? = null
    private var particleRenderPipeline: RenderPipeline? = null

    /** The `@group(1)` joint-palette set layout `skinnedInstancedRenderPipeline`'s layout is
     * built from -- see `SkinnedInstanceBuffer.createDescriptorSetLayout` for why each pooled
     * buffer creates its own compatible copy instead of sharing this handle. */
    private var skinnedInstanceDescriptorSetLayout: DescriptorSetLayoutHandle? = null

    /** Needed to build [renderPipeline]'s pipeline layout before any real [Material] exists. */
    private var pipelineDescriptorSetLayout: DescriptorSetLayoutHandle =
        DescriptorSetLayoutHandle(0)

    override suspend fun createBackendResources(window: Any): BackendResources {
        graphicsDevice = GraphicsDevice()
        graphicsDevice.create(window)
        swapchainManager = SwapchainManager(
            graphicsDevice,
            MAX_FRAMES_IN_FLIGHT,
            surfaceExtentProvider = { surfaceFramebufferExtent(window) },
        )
        swapchainManager.create()
        // Built before pipelineDescriptorSetLayout/renderPipeline: both need this ShadowMap's
        // existence (not its content -- the shadow pass hasn't run its first frame yet) to
        // decide whether their shared descriptor set layout declares the extra shadow
        // bindings (see Material.kt's own shadowMap doc comment).
        shadowMap = shadowShaderSet?.let { ShadowMap(graphicsDevice) }
        pipelineDescriptorSetLayout =
            Material.createDescriptorSetLayout(graphicsDevice, shadowMap = shadowMap)

        // fillPipeline(shaders, format) builds the normal pipeline plus, when wireframeSupport
        // is on, a VK_POLYGON_MODE_LINE companion sharing the exact same loaded ShaderPair --
        // one resource read instead of two, and the pair this app's own field assignments
        // below destructure.
        suspend fun fillAndWireframePipeline(
            vertexPath: String,
            fragmentPath: String,
            format: VertexFormat,
            vertexEntryPoint: String,
            fragmentEntryPoint: String,
        ): Pair<RenderPipeline, RenderPipeline?> {
            val shaders = loadShaderPair(vertexPath, fragmentPath)
            val fill = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineDescriptorSetLayout,
                shaders,
                format,
                vertexEntryPoint,
                fragmentEntryPoint,
            )
            val wireframe = if (wireframeSupport) {
                RenderPipeline(
                    graphicsDevice,
                    swapchainManager,
                    pipelineDescriptorSetLayout,
                    shaders,
                    format,
                    vertexEntryPoint,
                    fragmentEntryPoint,
                    polygonMode = VkPolygonMode.VK_POLYGON_MODE_LINE,
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
            fragmentShaderEntryPoint,
        )
        renderPipeline = fill
        wireframeRenderPipeline = wireframe

        additionalPipelines.forEach { (format, shaderSet) ->
            val (additionalFill, additionalWireframe) = fillAndWireframePipeline(
                shaderSet.vulkan.vertexResourcePath,
                shaderSet.vulkan.fragmentResourcePath,
                format,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint,
            )
            additionalRenderPipelines[format] = additionalFill
            additionalWireframe?.let { wireframeAdditionalRenderPipelines[format] = it }
        }
        instancedRenderPipeline = instancedShaderSet?.let { shaderSet ->
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineDescriptorSetLayout,
                loadShaderPair(
                    shaderSet.vulkan.vertexResourcePath,
                    shaderSet.vulkan.fragmentResourcePath,
                ),
                // Same vertex layout as the primary pipeline -- it draws the same meshes, just
                // many copies of them; `instanced` only ADDS binding 1 on top of it.
                vertexFormat,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint,
                variant = PipelineVariant.Instanced,
            )
        }
        skinnedInstancedRenderPipeline = skinnedInstancedShaderSet?.let { shaderSet ->
            val paletteLayout = SkinnedInstanceBuffer.createDescriptorSetLayout(graphicsDevice)
            skinnedInstanceDescriptorSetLayout = paletteLayout
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineDescriptorSetLayout,
                loadShaderPair(
                    shaderSet.vulkan.vertexResourcePath,
                    shaderSet.vulkan.fragmentResourcePath,
                ),
                VertexFormat.PositionNormalColorSkin,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint,
                variant = PipelineVariant.Instanced,
                extraDescriptorSetLayouts = listOf(paletteLayout),
            )
        }
        particleRenderPipeline = particleShaderSet?.let { shaderSet ->
            RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineDescriptorSetLayout,
                loadShaderPair(
                    shaderSet.vulkan.vertexResourcePath,
                    shaderSet.vulkan.fragmentResourcePath,
                ),
                VertexFormat.PositionUv,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint,
                variant = PipelineVariant.AlphaBlendedParticle,
            )
        }
        shadowRenderPipeline = shadowMap?.let { map ->
            val shaderSet = requireNotNull(shadowShaderSet)
            ShadowRenderPipeline(
                graphicsDevice,
                map.renderPass,
                pipelineDescriptorSetLayout,
                loadShaderPair(
                    shaderSet.vulkan.vertexResourcePath,
                    shaderSet.vulkan.fragmentResourcePath,
                ),
                // Same vertex layout as the primary pipeline -- it draws the same meshes.
                vertexFormat,
                map.size,
                shaderSet.vulkan.vertexEntryPoint,
                shaderSet.vulkan.fragmentEntryPoint,
            )
        }
        lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            loadShaderPair(
                DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH,
                DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH,
            ),
            MAX_FRAMES_IN_FLIGHT,
        )
        skyboxRenderPipeline = skyboxShaderSet?.let { shaderSet ->
            SkyboxRenderPipeline(
                graphicsDevice,
                swapchainManager,
                // The EXISTING 3D pass, same reuse lineRenderPipeline above does.
                renderPipeline.renderPass,
                loadShaderPair(
                    shaderSet.vulkan.vertexResourcePath,
                    shaderSet.vulkan.fragmentResourcePath,
                ),
                MAX_FRAMES_IN_FLIGHT,
            )
        }
        transferContext = TransferContext(graphicsDevice)
        val additionalPipelinesByFormat = additionalRenderPipelines.toMap()
        val wireframePipelinesByFormat = buildMap {
            wireframeRenderPipeline?.let { put(vertexFormat, it) }
            putAll(wireframeAdditionalRenderPipelines)
        }
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            additionalPipelinesByFormat,
            lineRenderPipeline,
            transferContext,
            loadShaderPair(UI_VERTEX_SHADER_RESOURCE_PATH, UI_FRAGMENT_SHADER_RESOURCE_PATH),
            loadShaderPair(
                UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH,
                UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH,
            ),
            loadShaderPair(
                UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH,
                UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH,
            ),
            loadShaderPair(
                UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH,
                UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH,
            ),
            MAX_FRAMES_IN_FLIGHT,
            wireframePipelinesByFormat,
            shadowMap,
            shadowRenderPipeline,
            buildMap {
                instancedRenderPipeline?.let { put(vertexFormat, it) }
                particleRenderPipeline?.let { put(VertexFormat.PositionUv, it) }
            },
            skinnedInstancedRenderPipeline
                ?.let { mapOf(VertexFormat.PositionNormalColorSkin to it) }
                ?: emptyMap(),
            skyboxRenderPipeline,
        )
        swapchainManager.createSyncObjects()

        return BackendResources(
            renderer = renderer,
            viewportSize = { swapchainManager.extent.width.toFloat() to swapchainManager.extent.height.toFloat() },
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
            pipelineDescriptorSetLayout.handle,
        )
        renderPipeline.destroy()
        additionalRenderPipelines.values.forEach { it.destroy() }
        wireframeRenderPipeline?.destroy()
        wireframeAdditionalRenderPipelines.values.forEach { it.destroy() }
        instancedRenderPipeline?.destroy()
        skinnedInstancedRenderPipeline?.destroy()
        particleRenderPipeline?.destroy()
        skinnedInstanceDescriptorSetLayout?.let {
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, it.handle)
        }
        shadowRenderPipeline?.destroy()
        shadowMap?.destroy()
        lineRenderPipeline.destroy()
        skyboxRenderPipeline?.destroy()
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
        const val UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/ui_texture.vert.spv"
        const val UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/ui_texture.frag.spv"
        const val UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/ui_rounded_quad.vert.spv"
        const val UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/ui_rounded_quad.frag.spv"
        const val DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/debug_line.vert.spv"
        const val DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH =
            "assets/shader/vulkan/debug_line.frag.spv"
    }
}
