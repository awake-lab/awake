// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.asset.shaders.ShaderSet
import io.github.ronjunevaldoz.awake.asset.shaders.ShaderSource
import io.github.ronjunevaldoz.awake.asset.shaders.ShaderStage
import io.github.ronjunevaldoz.awake.asset.shaders.ShaderStages
import io.github.ronjunevaldoz.awake.asset.shaders.entryPoint
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.app.GraphicsEngine
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.debug.SkyboxRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.SkinnedInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.pipeline.OpaqueRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineKey
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineRequest
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineTable
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineVariant
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShadowFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShadowRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.SkyboxRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiShaderPairs
import io.github.ronjunevaldoz.awake.vulkan.pipeline.buildRequestedPipelines
import io.github.ronjunevaldoz.awake.vulkan.pipeline.createSceneRenderPass
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.surfaceFramebufferExtent
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.ShadowMap

/**
 * Reusable Vulkan game bootstrap (see docs/MVP_PLAN.md's Decision Log: "reusable-Application
 * gap fix", "GameApplication", and "GameApplication a standalone render
 * bootstrap"). A new game supplies its shader/vertex-layout via the constructor and its own
 * behavior via the injected [appLifecycle] (`Game.ready(renderer)`/`Game.render(...)`) -- this class
 * only builds/tears down Vulkan's GPU resources, it never knows what the game actually draws.
 */
open class VulkanEngine(
    vertexShaderResourcePath: String,
    fragmentShaderResourcePath: String,
    vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    appLifecycle: AppLifecycle,
    private val vertexShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    private val fragmentShaderEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    /** Extra 3D pipelines keyed by the vertex format each one draws -- registered into
     * `Renderer.pipelinesByFormat`, so a `MeshRenderer` entity using that format draws through
     * its own pipeline instead of the primary one. Empty (default) for games with only the
     * primary pipeline. Keyed by format rather than named per kind (skinned/textured/...) on
     * purpose: this class builds GPU resources and must not know what the game draws, and a
     * per-kind pair of parameters grew this constructor by two every time a kind was added. */
    private val additionalPipelines: Map<VertexFormat, ShaderSet> = emptyMap(),
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
    private val shadowShaderSet: ShaderSet? = null,
    /** Opts into GPU instancing for [vertexFormat] (see `Renderer.instancedPipelinesByFormat`):
     * builds ONE extra pipeline from this shader set with a second, instance-rate vertex binding.
     * The shader's uniform block must hold `viewProjection` (not `mvp`) and it must declare the
     * 4 instance-matrix attributes -- see `instanced.wgsl`. `null` (default) means no instanced
     * pipeline is built at all and an `InstancedMeshRenderer` entity simply doesn't draw. */
    private val instancedShaderSet: ShaderSet? = null,
    /** Opts into ANIMATED GPU instancing (see `Renderer.skinnedInstancedPipelinesByFormat`):
     * builds ONE extra pipeline from this shader set with both the instance-rate model-matrix
     * binding and a `@group(1)` joint-palette storage-buffer set -- see
     * `skinned_instanced.wgsl`. Always built for [VertexFormat.PositionNormalColorSkin] rather
     * than for the primary [vertexFormat]: skinning needs joint indices/weights per vertex, so
     * that is the only format this shader can ever read. `null` (default) means no such
     * pipeline exists and an `InstancedSkinnedMeshRenderer` entity simply doesn't draw. */
    private val skinnedInstancedShaderSet: ShaderSet? = null,
    /** Opts into the procedural sky (see `SkyboxRenderPipeline`/`Renderer.showEnvironment`):
     * builds ONE extra pipeline from this shader set against the primary pipeline's own render
     * pass. `null` (default) means no skybox pipeline is built at all and
     * `Renderer.showEnvironment` stays an inert flag -- which is also why this is opt-in rather
     * than always-on: `skybox.wgsl` ships in `awake:asset:shaders`, so only a module that syncs
     * that directory actually has the compiled SPIR-V on its resource path. */
    private val skyboxShaderSet: ShaderSet? = null,
    /** Opts into billboard-particle instancing (see `Renderer.instancedPipelinesByFormat`
     * keyed by [VertexFormat.PositionUv]): builds ONE extra pipeline from this shader set with
     * `instanced = true, instanceAlpha = true, blendEnabled = true, depthWriteEnabled = false`
     * -- see `particle.wgsl`. `null` (default) means no such pipeline exists and a
     * `ParticleEmitter` entity simply doesn't draw. */
    private val particleShaderSet: ShaderSet? = null,
) : GraphicsEngine(
    vertexShaderResourcePath,
    fragmentShaderResourcePath,
    vertexFormat,
    appLifecycle,
) {
    constructor(
        shaderSet: ShaderSet,
        vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
        appLifecycle: AppLifecycle,
        additionalPipelines: Map<VertexFormat, ShaderSet> = emptyMap(),
        wireframeSupport: Boolean = false,
        shadowShaderSet: ShaderSet? = null,
        instancedShaderSet: ShaderSet? = null,
        skinnedInstancedShaderSet: ShaderSet? = null,
        skyboxShaderSet: ShaderSet? = null,
        particleShaderSet: ShaderSet? = null,
    ) : this(
        vertexShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
        fragmentShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
        vertexFormat = vertexFormat,
        appLifecycle = appLifecycle,
        vertexShaderEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
        fragmentShaderEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
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

    /** Shared by every [RenderPipeline] this app builds (and by the debug-line/skybox
     * pipelines, which reuse the existing 3D pass) -- see [createSceneRenderPass]'s
     * own doc comment for why one shared handle replaces what used to be one render pass per
     * pipeline. Owned here, not by any individual pipeline; destroyed exactly once in
     * [destroyBackend]. */
    private var sceneRenderPass: Long = 0
    private lateinit var requestedPipelines: Map<PipelineKey, Pair<RenderPipeline, RenderPipeline?>>
    private lateinit var transferContext: TransferContext

    /** Kept as a field only because [Material]'s descriptor set layout is built from it long
     * before the [ShadowFeature] that owns (and destroys) it exists. */
    private var shadowMap: ShadowMap? = null

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
        sceneRenderPass = createSceneRenderPass(graphicsDevice, swapchainManager)

        val requests = buildList {
            add(
                PipelineRequest(
                    key = PipelineKey.Primary,
                    vertexShaderResourcePath = vertexShaderResourcePath,
                    fragmentShaderResourcePath = fragmentShaderResourcePath,
                    vertexFormat = vertexFormat,
                    vertexEntryPoint = vertexShaderEntryPoint,
                    fragmentEntryPoint = fragmentShaderEntryPoint,
                    buildWireframe = wireframeSupport,
                ),
            )
            additionalPipelines.forEach { (format, shaderSet) ->
                add(
                    PipelineRequest(
                        key = PipelineKey.Format(format),
                        vertexShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                        fragmentShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                        vertexFormat = format,
                        vertexEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
                        fragmentEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
                        buildWireframe = wireframeSupport,
                    ),
                )
            }
            instancedShaderSet?.let { shaderSet ->
                add(
                    PipelineRequest(
                        key = PipelineKey.Instanced,
                        vertexShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                        fragmentShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                        // Same vertex layout as the primary pipeline -- it draws the same
                        // meshes, just many copies of them; `instanced` only ADDS binding 1.
                        vertexFormat = vertexFormat,
                        vertexEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
                        fragmentEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
                        variant = PipelineVariant.Instanced,
                    ),
                )
            }
            skinnedInstancedShaderSet?.let { shaderSet ->
                // Built BEFORE the request: the request needs the layout handle itself, and
                // this field assignment must happen regardless of what buildRequestedPipelines
                // does later (destroyBackend's cleanup reads this field independently).
                val paletteLayout = SkinnedInstanceBuffer.createDescriptorSetLayout(graphicsDevice)
                skinnedInstanceDescriptorSetLayout = paletteLayout
                add(
                    PipelineRequest(
                        key = PipelineKey.SkinnedInstanced,
                        vertexShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                        fragmentShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                        vertexFormat = VertexFormat.PositionNormalColorSkin,
                        vertexEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
                        fragmentEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
                        variant = PipelineVariant.Instanced,
                        extraDescriptorSetLayouts = listOf(paletteLayout),
                    ),
                )
            }
            particleShaderSet?.let { shaderSet ->
                add(
                    PipelineRequest(
                        key = PipelineKey.Particle,
                        vertexShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                        fragmentShaderResourcePath = shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                        vertexFormat = VertexFormat.PositionUv,
                        vertexEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
                        fragmentEntryPoint = shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
                        variant = PipelineVariant.AlphaBlendedParticle,
                    ),
                )
            }
        }
        requestedPipelines = buildRequestedPipelines(
            graphicsDevice,
            swapchainManager,
            sceneRenderPass,
            pipelineDescriptorSetLayout,
            requests,
            ::loadShaderPair,
        )
        val (primaryPipeline, primaryWireframePipeline) = requestedPipelines.getValue(PipelineKey.Primary)

        val shadowFeature = shadowMap?.let { map ->
            val shaderSet = requireNotNull(shadowShaderSet)
            val shadowPipeline = ShadowRenderPipeline(
                graphicsDevice,
                map.renderPass,
                pipelineDescriptorSetLayout,
                loadShaderPair(
                    shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                    shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                ),
                // Same vertex layout as the primary pipeline -- it draws the same meshes.
                vertexFormat,
                map.size,
                shaderSet.vulkan.entryPoint(ShaderStage.VERTEX),
                shaderSet.vulkan.entryPoint(ShaderStage.FRAGMENT),
            )
            ShadowFeature(map, shadowPipeline)
        }
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            sceneRenderPass,
            loadShaderPair(
                DEBUG_LINE_VERTEX_SHADER_RESOURCE_PATH,
                DEBUG_LINE_FRAGMENT_SHADER_RESOURCE_PATH,
            ),
            MAX_FRAMES_IN_FLIGHT,
        )
        val skyboxRenderPipeline = skyboxShaderSet?.let { shaderSet ->
            SkyboxRenderPipeline(
                graphicsDevice,
                swapchainManager,
                // The EXISTING 3D pass, same reuse lineRenderPipeline above does.
                sceneRenderPass,
                loadShaderPair(
                    shaderSet.vulkan.resourcePath(ShaderStage.VERTEX),
                    shaderSet.vulkan.resourcePath(ShaderStage.FRAGMENT),
                ),
                MAX_FRAMES_IN_FLIGHT,
            )
        }
        transferContext = TransferContext(graphicsDevice)
        // Registration order is paint order: the sky draws with depth test/write off, so it
        // must precede opaque geometry; the UI pass draws on top of both. Content features
        // (sky) exist only when their shader set was supplied; capability features (opaque
        // geometry + debug lines, UI) are always present -- see
        // docs/reference/render-extensibility.md.
        val renderFeatures: List<RenderFeature> = buildList {
            skyboxRenderPipeline?.let { add(SkyboxRenderFeature(it)) }
            add(OpaqueRenderFeature(lineRenderPipeline))
            add(UiRenderFeature())
        }
        val renderer = Renderer(
            graphicsDevice = graphicsDevice,
            swapchainManager = swapchainManager,
            pipelines = PipelineTable(
                primary = primaryPipeline,
                byFormat = requestedPipelines
                    .mapNotNull { (key, pair) -> (key as? PipelineKey.Format)?.let { it.vertexFormat to pair.first } }
                    .toMap(),
                wireframeByFormat = buildMap {
                    primaryWireframePipeline?.let { put(vertexFormat, it) }
                    requestedPipelines.forEach { (key, pair) ->
                        if (key is PipelineKey.Format) pair.second?.let {
                            put(
                                key.vertexFormat,
                                it
                            )
                        }
                    }
                },
                instancedByFormat = buildMap {
                    requestedPipelines[PipelineKey.Instanced]?.first?.let { put(vertexFormat, it) }
                    requestedPipelines[PipelineKey.Particle]?.first?.let {
                        put(
                            VertexFormat.PositionUv,
                            it
                        )
                    }
                },
                skinnedInstancedByFormat = requestedPipelines[PipelineKey.SkinnedInstanced]?.first
                    ?.let { mapOf(VertexFormat.PositionNormalColorSkin to it) } ?: emptyMap(),
            ),
            renderFeatures = renderFeatures,
            transferContext = transferContext,
            uiShaderPairs = UiShaderPairs(
                quad = loadShaderPair(
                    UI_VERTEX_SHADER_RESOURCE_PATH,
                    UI_FRAGMENT_SHADER_RESOURCE_PATH
                ),
                glyph = loadShaderPair(
                    UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH,
                    UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH,
                ),
                texture = loadShaderPair(
                    UI_TEXTURE_VERTEX_SHADER_RESOURCE_PATH,
                    UI_TEXTURE_FRAGMENT_SHADER_RESOURCE_PATH,
                ),
                roundedQuad = loadShaderPair(
                    UI_ROUNDED_QUAD_VERTEX_SHADER_RESOURCE_PATH,
                    UI_ROUNDED_QUAD_FRAGMENT_SHADER_RESOURCE_PATH,
                ),
            ),
            maxFramesInFlight = MAX_FRAMES_IN_FLIGHT,
            shadowFeature = shadowFeature,
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
        // Not redundant with GameApplication.dispose()'s own renderer.waitIdle(): that one runs
        // before game.dispose(), which can itself submit work (a final readback, an offscreen
        // pass) after it.
        VulkanBuffers.vkDeviceWaitIdle(graphicsDevice.device)
        renderer.destroy()
        swapchainManager.destroy()
        swapchainManager.destroySyncObjects()
        transferContext.destroy()
        VulkanDescriptors.vkDestroyDescriptorSetLayout(
            graphicsDevice.device,
            pipelineDescriptorSetLayout.handle,
        )
        requestedPipelines.values.forEach { (fill, wireframe) ->
            fill.destroy()
            wireframe?.destroy()
        }
        skinnedInstanceDescriptorSetLayout?.let {
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, it.handle)
        }
        // shadowMap/shadowRenderPipeline/lineRenderPipeline/skyboxRenderPipeline are NOT
        // destroyed here: renderer.destroy() above tears down every RenderFeature (and the
        // ShadowFeature) it was handed, which owns all four. Destroying them again is a
        // double-free of live Vulkan handles.
        Vulkan.vkDestroyRenderPass(graphicsDevice.device, sceneRenderPass)
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

/** [PipelineRequest]/[loadShaderPair] still take plain resource-path strings -- every real
 * [ShaderSource] this backend loads is a [ShaderSource.ResourcePath] (confirmed: nothing in this
 * engine constructs a [ShaderSource.PrecompiledBinary]/`InlineText` today), so these two
 * extensions are the one place that assumption is made explicit, instead of every call site
 * below repeating an `as ShaderSource.ResourcePath` cast. */
private fun ShaderStages.resourcePath(stage: ShaderStage): String =
    (this[stage] as? ShaderSource.ResourcePath)?.path
        ?: error("Vulkan shader loading only supports ShaderSource.ResourcePath today (stage=$stage).")

private fun ShaderStages.entryPoint(stage: ShaderStage): String =
    this[stage]?.entryPoint
        ?: error("No $stage stage registered in this ShaderStages.")
