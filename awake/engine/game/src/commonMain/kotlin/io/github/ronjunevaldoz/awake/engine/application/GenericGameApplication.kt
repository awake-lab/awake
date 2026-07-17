// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Backend-neutral render bootstrap shared by `VulkanGameApplication` (`awake-backend-vulkan`)
 * and `WebGpuGameApplication` (`awake-backend-webgpu`) -- knows nothing about ECS/scene
 * graphs or UI (see docs/MVP_PLAN.md's Decision Log, "GenericGameApplication a standalone
 * render bootstrap", for the full rationale behind this class's narrowed scope). Each backend
 * implements exactly two things: [createBackendResources] (construct its concrete
 * `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Renderer`, return the handful of
 * interface-typed objects this class needs) and [destroyBackend] (GPU teardown). Everything
 * else -- what to render, scene/ECS handling, UI -- is the injected [game]'s job, not this
 * class's: every [Application] lifecycle callback either builds/tears down backend resources
 * or forwards verbatim to [game], with zero game-specific logic living here.
 */
abstract class GenericGameApplication(
    protected val vertexShaderResourcePath: String,
    protected val fragmentShaderResourcePath: String,
    protected val vertexStride: Int,
    private val game: Game
) : Application {
    /** Same "create() stays synchronous, launch internally" reasoning the original
     * `VulkanApplication`/`WebGpuApplication` used -- [update] is a no-op until
     * [createBackendResources] (and [Game.ready]) finish. */
    private var isReady = false

    /** Populated by [createBackendResources] -- `protected` (not `private`) so each
     * backend's [destroyBackend] override can still reach it for teardown. */
    protected lateinit var renderer: Renderer
        private set
    private lateinit var viewportSize: () -> Pair<Float, Float>

    /** The current swapchain's width/height aspect ratio -- for a subclass computing a
     * [io.github.ronjunevaldoz.awake.core.math.Frustum]'s corners to visualize via
     * [drawDebugLines]. */
    protected val aspectRatio: Float
        get() = viewportSize().let { (width, height) -> width / height }

    final override fun create(surface: Any?) {
        // NOT MainScope(): its Dispatchers.Main can resolve to a Swing/AWT dispatcher on
        // desktop, deadlocking against -XstartOnFirstThread (already claimed by GLFW).
        // Dispatchers.Unconfined keeps every call on this calling thread instead.
        surface?.let { window -> CoroutineScope(Dispatchers.Unconfined).launch { setupCommon(window) } }
    }

    final override fun update(delta: Float) {
        if (!isReady) return
        val (width, height) = viewportSize()
        game.render(delta, width, height)
    }

    final override fun pause() {
        game.pause()
    }

    final override fun resume() {
        game.resume()
    }

    final override fun resize(x: Int, y: Int, width: Int, height: Int) {
        game.resize(width.toFloat(), height.toFloat())
    }

    /** [game] first, [destroyBackend] second -- reversed from an earlier version of this
     * class. `game.dispose()` (e.g. `helloCubeGame()`'s scene runtime) tears down GAME-OWNED GPU
     * resources (meshes/materials/render targets, all created via `Renderer.createMesh`/
     * `createMaterial`/`createRenderTarget`) that were allocated against the *live*
     * `GraphicsDevice` -- those teardown calls (`vkDestroyBuffer`/`vkDestroyImage`/etc.) are
     * undefined behavior once [destroyBackend] has already destroyed the `VkDevice` they
     * belong to. Confirmed as a **real, reproducible SIGSEGV** (not theoretical) via a
     * Vulkan-validation-layer-instrumented run: `game.dispose() -> Mesh.destroy() ->
     * VulkanBuffers.vkDestroyBuffer` crashed the JVM natively inside `libvulkan.dylib`, with
     * the validation layer's own log immediately prior showing 14 objects "couldn't find"/
     * leaked against a device that had *already* been torn down by the old
     * `destroyBackend()`-then-`game.dispose()` order. See docs/MVP_PLAN.md's D24 minimap-
     * crash investigation entry for the full account (this was found while chasing a
     * different, still-unresolved bug -- the two are not confirmed to be the same root
     * cause, but this ordering bug is independently real and now fixed regardless). */
    final override fun dispose() {
        game.dispose()
        if (isReady) destroyBackend()
    }

    /** Draws world-space debug lines (e.g. a frustum wireframe) this frame -- see
     * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.drawDebugLines]'s doc comment
     * for the staging/depth-testing details. */
    protected fun drawDebugLines(lines: List<LineSegment>) {
        renderer.drawDebugLines(lines)
    }

    private suspend fun setupCommon(window: Any) {
        val backend = createBackendResources(window)
        renderer = backend.renderer
        viewportSize = backend.viewportSize

        game.ready(renderer)

        isReady = true
    }

    /** Backend-specific: construct `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/
     * etc., and return the handful of interface-typed objects the shared bootstrap above
     * needs. Called once, off [create]'s calling thread only in the "hasn't suspended yet"
     * sense -- see [create]'s own doc comment. */
    protected abstract suspend fun createBackendResources(window: Any): BackendResources

    /** Backend-specific GPU teardown -- reads [renderer] (this class's own protected field)
     * plus whatever backend-local pipeline objects the override's own
     * [createBackendResources] stashed in its own fields. */
    protected abstract fun destroyBackend()

    /** The interface-typed objects [createBackendResources] must produce -- everything the
     * shared bootstrap touches generically, and nothing more (backend-concrete types like
     * `GraphicsDevice`/`SwapchainManager`/`RenderPipeline` stay entirely inside each
     * backend's own subclass). [viewportSize] is queried live each frame (not cached),
     * since the swapchain/canvas can resize. */
    protected data class BackendResources(
        val renderer: Renderer,
        val viewportSize: () -> Pair<Float, Float>
    )
}
