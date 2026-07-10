/*
 * Awake
 * Awake.awake-demo.shared.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class VulkanApplication : Application {
    /** Phase 2: instance/surface/physical-device/logical-device/queue lifecycle, extracted
     * into a reusable class -- see [GraphicsDevice]'s doc comment. */
    private val graphicsDevice = GraphicsDevice()
    /** Phase 2: swapchain + frames-in-flight sync, extracted into a reusable class -- see
     * [SwapchainManager]'s doc comment. */
    private val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
    /** Phase 2: render pass + graphics pipeline, extracted into a reusable class -- see
     * [RenderPipeline]'s doc comment. Constructed lazily in [setupVulkan] for the same
     * reason as [mesh]/[texture]/[material]: it needs [graphicsDevice] to already be
     * `.create()`-d, plus [material]'s descriptor set layout to exist first. */
    private lateinit var renderPipeline: RenderPipeline
    /** Phase 2: command pool + one-time (upload) command submission, extracted into a
     * reusable class -- see [TransferContext]'s doc comment. Constructed lazily in
     * [setupVulkan] for the same reason as [mesh]/[texture]/[material]: it needs
     * [graphicsDevice] to already be `.create()`-d. */
    private lateinit var transferContext: TransferContext
    /** Phase 2: depth buffer + framebuffers + per-frame command buffers + the whole
     * draw/submit/present loop, extracted into a reusable class -- see [Renderer]'s doc
     * comment. Constructed lazily in [setupVulkan] since it needs [renderPipeline] and
     * [transferContext] to exist first (for the render pass and a command pool,
     * respectively). */
    private lateinit var renderer: Renderer
    /** Phase 2: vertex/index buffer upload, extracted into a reusable class -- see
     * [Mesh]'s doc comment. Constructed lazily in [setupVulkan] (needs a command pool and
     * graphics queue to already exist for its one-time upload commands), not eagerly like
     * [graphicsDevice]/[swapchainManager]. */
    private lateinit var mesh: Mesh
    /** Phase 2: uniform buffer + descriptor set/pool/layout, extracted into a reusable
     * class -- see [Material]'s doc comment for why it's constructed in two phases. */
    private lateinit var material: Material
    private lateinit var texture: Texture
    private lateinit var sceneHost: SceneRuntimeHost
    /** Phase 4: decouples [SceneRuntimeHost.fixedUpdate] (framerate-independent simulation)
     * from [SceneRuntimeHost.render] (once per frame) -- see [FixedTimestepLoop]'s doc
     * comment for why this matters ahead of Phase 8 physics. */
    private val fixedTimestepLoop = FixedTimestepLoop()
    /** Web demo (see docs/MVP_PLAN.md's decision log): [setupVulkan] now runs in its own
     * coroutine (launched from [create], which must stay synchronous -- see its doc
     * comment), so [create] returns before setup finishes. The platform render loop can
     * start calling [update] before [sceneHost] is initialized; this flag makes [update] a
     * no-op until setup actually completes instead of throwing on the `lateinit` access. */
    private var isReady = false

    companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2

        // interleaved position(vec3) + color(vec3) + uv(vec2), matching triangle.vert's
        // location 0 / location 1 / location 2 inputs. 8 unique corners of a unit cube,
        // colored with the classic RGB-cube palette (black/red/yellow/green/blue/magenta/
        // white/cyan) so every face is visually distinguishable; UVs are approximate
        // (shared corners can't have per-face-correct UVs without duplicating vertices,
        // out of scope for this MVP proof of indexed drawing + a real MVP matrix).
        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        // 12 triangles, 2 per face. cullMode is set to NONE in RenderPipeline specifically
        // so this winding order doesn't need to be outward-consistent per face -- depth
        // testing alone resolves correct occlusion.
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )

        // A tiny 2x2 RGBA8 checkerboard (white/black) -- proves real texture sampling
        // without needing an image file loader (out of scope for this MVP phase).
        const val TEXTURE_WIDTH = 2
        const val TEXTURE_HEIGHT = 2
        val textureData = byteArrayOf(
            // white, black
            -1, -1, -1, -1, 0, 0, 0, -1,
            // black, white
            0, 0, 0, -1, -1, -1, -1, -1,
        )
    }


    // Web demo (see docs/MVP_PLAN.md's decision log): Application.create() stays
    // synchronous everywhere -- it's called from platform lifecycle callbacks (Android's
    // SurfaceHolder.surfaceCreated, iOS's layoutSubviews) that can't become suspend.
    // setupVulkan needs suspend (SceneRuntimeHost.create() loads the scene JSON via a
    // suspend readResourceBytes), so it's launched in its own coroutine instead.
    override fun create(surface: Any?) {
        surface?.let { window -> MainScope().launch { setupVulkan(window) } }
    }

    override fun update(delta: Float) {
        if (!isReady) return
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = sceneHost::fixedUpdate,
            render = { sceneHost.render() }
        )
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(x: Int, y: Int, width: Int, height: Int) {

    }

    override fun dispose() {
        if (isReady) destroy()
    }

    private suspend fun setupVulkan(window: Any) {
        graphicsDevice.create(window)
        // create swap chain
        swapchainManager.create()
        material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            material.descriptorSetLayout,
            readResourceBytes("assets/shader/vulkan/triangle.vert.spv"),
            readResourceBytes("assets/shader/vulkan/triangle.frag.spv"),
            VERTEX_STRIDE
        )
        transferContext = TransferContext(graphicsDevice)
        renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            transferContext.commandPool.handle,
            MAX_FRAMES_IN_FLIGHT
        )
        mesh = Mesh(graphicsDevice, transferContext::runOneTimeCommands, cubeVertices, cubeIndices)
        texture = Texture(
            graphicsDevice,
            transferContext::runOneTimeCommands,
            textureData,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        )
        material.createResources(texture)
        swapchainManager.createSyncObjects()
        sceneHost = SceneRuntimeHost.create(renderer) { request ->
            resolveRenderable(request)
        }
        isReady = true
    }

    private fun destroy() {
        renderer.destroy()
        swapchainManager.destroy()

        swapchainManager.destroySyncObjects()
        transferContext.destroy()

        mesh.destroy()
        texture.destroy()
        material.destroy()

        renderPipeline.destroy()

        graphicsDevice.destroy()
    }

    private fun resolveRenderable(request: io.github.ronjunevaldoz.awake.scene.runtime.SceneRenderableRequest): MeshRenderer {
        require(request.meshRenderer.mesh == "cube") {
            "Unsupported scene mesh '${request.meshRenderer.mesh}'."
        }
        require(request.meshRenderer.material == "textured-default") {
            "Unsupported scene material '${request.meshRenderer.material}'."
        }
        return MeshRenderer(mesh, material)
    }
}
