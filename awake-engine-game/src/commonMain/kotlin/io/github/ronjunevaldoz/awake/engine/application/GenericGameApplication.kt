// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Backend-neutral game bootstrap shared by `VulkanGameApplication` (`awake-backend-vulkan`)
 * and `WebGpuGameApplication` (`awake-backend-webgpu`) -- extracted after those two classes
 * were found to duplicate ~90% of each other's fields/lifecycle/hooks (see docs/MVP_PLAN.md's
 * decision log for the full before/after). Each backend implements exactly two things:
 * [createBackendResources] (construct its concrete `GraphicsDevice`/`SwapchainManager`/
 * `RenderPipeline`/`Mesh`/`Material`/`Renderer`, upload [meshes]/[texture], return the handful
 * of interface-typed objects this class needs) and [destroyBackend] (GPU teardown). Everything
 * else -- scene loading, the fixed-timestep loop, the `onSceneReady`/`onFixedUpdate`/
 * `onDrawUi`/`resolveRenderable` hook contract -- lives here, once.
 */
abstract class GenericGameApplication(
    protected val vertexShaderResourcePath: String,
    protected val fragmentShaderResourcePath: String,
    protected val vertexStride: Int,
    protected val meshes: Map<String, MeshGeometry>,
    protected val texture: TextureAsset? = null,
    protected val scenePath: String
) : Application {
    /** Immediate-mode debug/catalog UI overlay -- see [onDrawUi]. */
    protected val ui = UiContext()

    /** Phase B (see docs/MVP_PLAN.md's custom-UI decision log): the minimal bitmap font
     * atlas shared by [ui]'s `text()` calls. */
    protected val font = BitmapFont()

    /** The ECS world the scene at [scenePath] was instantiated into -- exposed (alongside
     * [scene]) so a subclass can look up its own entities (e.g. a player/camera) once, in
     * an overridden [onSceneReady]. */
    protected lateinit var world: World
        private set

    /** The instantiated scene graph (root node names/entities) -- see [world]'s doc
     * comment. */
    protected lateinit var scene: SceneInstance
        private set

    private val transformSystem = TransformSystem()
    private lateinit var renderSystem: RenderSystem
    private val fixedTimestepLoop = FixedTimestepLoop()

    /** Same "create() stays synchronous, launch internally" reasoning the original
     * `VulkanApplication`/`WebGpuApplication` used -- [update] is a no-op until
     * [createBackendResources] and scene instantiation finish. */
    private var isReady = false

    /** Populated by [createBackendResources] -- `protected` (not `private`) so each
     * backend's [destroyBackend] override can still reach them for teardown. */
    protected lateinit var renderer: Renderer
        private set
    protected lateinit var meshInstances: Map<String, Mesh>
        private set
    protected lateinit var material: Material
        private set
    private lateinit var viewportSize: () -> Pair<Float, Float>

    /** The current swapchain's width/height aspect ratio -- for a subclass computing a
     * [io.github.ronjunevaldoz.awake.core.math.Frustum]'s corners to visualize via
     * [drawDebugLines]. */
    protected val aspectRatio: Float
        get() = viewportSize().let { (width, height) -> width / height }

    final override fun create(surface: Any?) {
        // NOT MainScope(): its Dispatchers.Main can resolve to a Swing/AWT dispatcher on
        // desktop, which deadlocks against -XstartOnFirstThread (already claimed by GLFW) --
        // see awake-backend-vulkan's VulkanGameApplication git history for the incident this
        // avoids. createBackendResources()/scene loading never touch Swing/AWT, so
        // Dispatchers.Unconfined keeps every call on this calling thread, per this project's
        // "one thread owns the render backend" rule.
        surface?.let { window -> CoroutineScope(Dispatchers.Unconfined).launch { setupCommon(window) } }
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
        if (isReady) destroyBackend()
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
        val (width, height) = viewportSize()
        ui.beginFrame(width, height)
        onDrawUi(ui)
        renderer.drawUi(ui.endFrame())
        renderSystem.update(world, 0f)
    }

    /** Override to declare this frame's UI widgets (buttons/toggles/dropdowns) via [ui] --
     * called once per real frame, before the 3D scene is actually submitted to the GPU (see
     * [onRender]'s doc comment for why that ordering matters). Mirrors [onSceneReady]/
     * [onFixedUpdate]'s "subclass declares behavior, base class drives the call" shape. */
    protected open fun onDrawUi(ui: UiContext) {}

    /** Draws world-space debug lines (e.g. a frustum wireframe) this frame -- see
     * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.drawDebugLines]'s doc comment
     * for the staging/depth-testing details. Call from an overridden [onDrawUi] (or
     * [onRender]), before [onDrawUi]'s caller submits the frame. */
    protected fun drawDebugLines(lines: List<LineSegment>) {
        renderer.drawDebugLines(lines)
    }

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

    private suspend fun setupCommon(window: Any) {
        val backend = createBackendResources(window)
        renderer = backend.renderer
        meshInstances = backend.meshInstances
        material = backend.material
        viewportSize = backend.viewportSize

        world = World()
        scene = SceneLoader.loadFromResource(scenePath).instantiate(world)
        scene.attachRenderableComponents(::resolveRenderable)
        renderSystem = RenderSystem(renderer)
        onSceneReady()

        isReady = true
    }

    /** Backend-specific: construct `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/
     * etc., upload [meshes]/[texture], and return the handful of interface-typed objects
     * the shared bootstrap above needs. Called once, off [create]'s calling thread only in
     * the "hasn't suspended yet" sense -- see [create]'s own doc comment. */
    protected abstract suspend fun createBackendResources(window: Any): BackendResources

    /** Backend-specific GPU teardown -- reads [renderer]/[meshInstances]/[material] (this
     * class's own protected fields) plus whatever backend-local pipeline objects the
     * override's own [createBackendResources] stashed in its own fields. */
    protected abstract fun destroyBackend()

    /** The interface-typed objects [createBackendResources] must produce -- everything the
     * shared bootstrap touches generically, and nothing more (backend-concrete types like
     * `GraphicsDevice`/`SwapchainManager`/`RenderPipeline` stay entirely inside each
     * backend's own subclass). [viewportSize] is queried live each frame (not cached),
     * since the swapchain/canvas can resize. */
    protected data class BackendResources(
        val renderer: Renderer,
        val meshInstances: Map<String, Mesh>,
        val material: Material,
        val viewportSize: () -> Pair<Float, Float>
    )
}
