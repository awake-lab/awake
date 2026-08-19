// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.core.graphics.WindowApplication
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Backend-neutral render bootstrap shared by `VulkanGameApplication` (`awake-backend-vulkan`)
 * and `WebGpuGameApplication` (`awake-backend-webgpu`)
 */
abstract class GraphicsEngine(
    protected val vertexShaderResourcePath: String,
    protected val fragmentShaderResourcePath: String,
    protected val vertexFormat: VertexFormat,
    protected val appLifecycle: AppLifecycle,
) : WindowApplication {

    /** The session's input accumulator. Guaranteed to exist since [appLifecycle] is an [io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle]. */
    override val input: Input get() = (appLifecycle as AwakeAppLifecycle).input

    /** Same "create() stays synchronous, launch internally" reasoning the original
     * `VulkanApplication`/`WebGpuApplication` used -- [update] is a no-op until
     * [createBackendResources] (and [AppLifecycle.ready]) finish. */
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
        input.updateSnapshot()
        appLifecycle.update(delta, width, height)
    }

    final override fun pause() {
        appLifecycle.pause()
    }

    final override fun resume() {
        appLifecycle.resume()
    }

    final override fun resize(x: Int, y: Int, width: Int, height: Int) {
        appLifecycle.resize(width.toFloat(), height.toFloat())
    }

    final override fun dispose() {
        // Before game.dispose(), not just before destroyBackend(): a game frees its own
        // meshes/materials in dispose(), and the last frame's command buffers can still be
        // pending on them at that point.
        if (isReady) renderer.waitIdle()
        appLifecycle.dispose()
        if (isReady) destroyBackend()
    }

    /** Draws world-space debug lines (e.g. a frustum wireframe) this frame -- see
     * [Renderer.drawDebugLines]'s doc comment
     * for the staging/depth-testing details. */
    protected fun drawDebugLines(lines: List<LineSegment>) {
        renderer.drawDebugLines(lines)
    }

    private suspend fun setupCommon(window: Any) {
        val backend = createBackendResources(window)
        renderer = backend.renderer
        viewportSize = backend.viewportSize

        appLifecycle.ready(renderer)

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
        val viewportSize: () -> Pair<Float, Float>,
    )
}
