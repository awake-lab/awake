// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

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
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Reusable Vulkan game bootstrap (see docs/MVP_PLAN.md's Decision Log: "reusable-Application
 * gap fix"). Extracted from `awake-demo`'s own `VulkanApplication.kt`, which used to hand-roll
 * this exact `GraphicsDevice` -> `SwapchainManager` -> `Material` -> `RenderPipeline` ->
 * `TransferContext` -> `Mesh`(es) -> `Texture` -> scene-loading -> `Renderer` sequence itself
 * -- confirmed via this session's research that the two existing `Application`
 * implementations (Vulkan here, WebGPU's own `WebGpuGameApplication`) shared this bootstrap
 * almost verbatim, differing only in concrete backend types. A new game supplies its mesh
 * geometry/texture/scene path via the constructor and, if it needs game-specific per-frame
 * logic (player movement, AI, camera control), overrides [onFixedUpdate]/[onRender] --
 * calling `super` first so [TransformSystem]/[RenderSystem] still run.
 *
 * [texture] is optional: when `null`, a trivial 1x1 white pixel is bound instead, so the
 * existing shader/descriptor-set contract (always expects a combined-image-sampler) doesn't
 * need a second no-texture pipeline variant.
 */
abstract class VulkanGameApplication(
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
    private lateinit var uiRenderPipeline: UiRenderPipeline
    private lateinit var uiGlyphRenderPipeline: UiGlyphRenderPipeline
    private lateinit var fontTexture: Texture
    private lateinit var transferContext: TransferContext
    private lateinit var renderer: Renderer
    private lateinit var material: Material
    private lateinit var textureInstance: Texture
    private val meshInstances = mutableMapOf<String, Mesh>()

    /** Immediate-mode debug/catalog UI overlay -- see [onDrawUi]. */
    protected val ui = UiContext()

    /** Phase B (see docs/MVP_PLAN.md's custom-UI decision log) -- pass to [ui]'s `text(...)`
     * calls in an overridden [onDrawUi]. */
    protected val font = BitmapFont()

    /** The ECS world the scene at [scenePath] was instantiated into -- exposed (alongside
     * [scene]) so a subclass can look up its own entities (e.g. a player/camera) once, in
     * an overridden [onSceneReady], the same way `awake-demo`'s `SceneRuntimeHost` does. */
    protected lateinit var world: World
        private set

    /** The instantiated scene graph (root node names/entities) -- see [world]'s doc
     * comment. */
    protected lateinit var scene: SceneInstance
        private set
    private val transformSystem = TransformSystem()
    private lateinit var renderSystem: RenderSystem
    private val fixedTimestepLoop = FixedTimestepLoop()

    /** Same "create() stays synchronous, launch internally" reasoning as the original
     * `VulkanApplication`/`WebGpuApplication` -- [update] is a no-op until [setupVulkan]
     * finishes. */
    private var isReady = false

    final override fun create(surface: Any?) {
        surface?.let { window -> MainScope().launch { setupVulkan(window) } }
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

    /** Runs at a fixed timestep -- override to add game-specific per-frame logic (movement,
     * AI, camera control), calling `super.onFixedUpdate(delta)` so [TransformSystem] still
     * propagates world matrices after any position changes this frame. */
    protected open fun onFixedUpdate(delta: Float) {
        transformSystem.update(world, delta)
    }

    /** Runs exactly once per frame, after zero or more [onFixedUpdate] calls. Override only
     * if a game needs to draw something beyond the standard scene-graph render pass. */
    protected open fun onRender() {
        // ui.beginFrame/onDrawUi/renderer.drawUi run BEFORE renderSystem.update (which
        // triggers the actual GPU submit) -- drawUi() only stages this frame's overlay
        // content into a CPU-side buffer, it issues no GPU commands itself. The 3D pass and
        // the UI pass both get recorded into the SAME command buffer inside the one
        // renderSystem.update -> renderer.draw call below, so staging the UI content first
        // is what makes it show up THIS frame instead of one frame late.
        ui.beginFrame(swapchainManager.extent.width.toFloat(), swapchainManager.extent.height.toFloat())
        onDrawUi(ui)
        renderer.drawUi(ui.endFrame())
        renderSystem.update(world, 0f)
    }

    /** Override to declare this frame's UI widgets (buttons/toggles/dropdowns) via [ui] --
     * called once per real frame, before the 3D scene is actually submitted to the GPU (see
     * [onRender]'s doc comment for why that ordering matters). Mirrors [onSceneReady]/
     * [onFixedUpdate]'s "subclass declares behavior, base class drives the call" shape. */
    protected open fun onDrawUi(ui: UiContext) {}

    /** Looks the mesh up by name in the map built from the constructor's [meshes] and pairs
     * it with the single shared [material] -- override for per-mesh materials. */
    protected open fun resolveRenderable(request: SceneRenderableRequest): MeshRenderer {
        val resolvedMesh = meshInstances[request.meshRenderer.mesh]
            ?: error("Unsupported scene mesh '${request.meshRenderer.mesh}'.")
        return MeshRenderer(resolvedMesh, material)
    }

    /** Called once, after the scene at [scenePath] has been instantiated into [world] and
     * renderable components attached, but before the app is marked ready -- override to
     * resolve game-specific entities (player, camera, NPC) and construct game-specific
     * systems, driven from an overridden [onFixedUpdate]. */
    protected open fun onSceneReady() {}

    private suspend fun setupVulkan(window: Any) {
        graphicsDevice.create(window)
        swapchainManager.create()
        material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            material.descriptorSetLayout,
            readResourceBytes(vertexShaderResourcePath),
            readResourceBytes(fragmentShaderResourcePath),
            vertexStride
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
        renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            uiRenderPipeline,
            uiGlyphRenderPipeline,
            transferContext.commandPool.handle,
            MAX_FRAMES_IN_FLIGHT
        )
        meshes.forEach { (name, geometry) ->
            meshInstances[name] = Mesh(
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
        swapchainManager.destroySyncObjects()
        transferContext.destroy()
        meshInstances.values.forEach { it.destroy() }
        textureInstance.destroy()
        material.destroy()
        renderPipeline.destroy()
        uiRenderPipeline.destroy()
        uiGlyphRenderPipeline.destroy()
        fontTexture.destroy()
        graphicsDevice.destroy()
    }

    private companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2
        val PLACEHOLDER_TEXTURE = TextureAsset(byteArrayOf(-1, -1, -1, -1), 1, 1)

        // Bundled per-consumer-app (not a base-class resource -- see docs/MVP_PLAN.md's
        // custom-UI decision log entry for why): every VulkanGameApplication subclass needs
        // its own copy of these two files at this exact resource path, same as awake-demo's
        // and sample-hello-cube's existing triangle.vert/.frag copies.
        const val UI_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.vert.spv"
        const val UI_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_quad.frag.spv"
        const val UI_GLYPH_VERTEX_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.vert.spv"
        const val UI_GLYPH_FRAGMENT_SHADER_RESOURCE_PATH = "assets/shader/vulkan/ui_glyph.frag.spv"
    }
}
