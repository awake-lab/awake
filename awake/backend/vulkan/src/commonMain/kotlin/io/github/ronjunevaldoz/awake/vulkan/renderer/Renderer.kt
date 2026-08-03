// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRoundedQuadRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiTextureRenderPipeline

/**
 * Phase 2 (renderer abstraction): the `Renderer.draw(camera, List<DrawCall>)` entry point --
 * owns the depth buffer, framebuffers, and per-frame command buffers, and orchestrates a
 * whole frame (wait/acquire -> update each [DrawCall]'s uniform buffer -> record -> submit ->
 * present), extracted verbatim from `VulkanApplication`'s `createDepthResources`/
 * `createFramebuffers`/`createCommandBuffer`/`drawFrame`/`recordCommandBuffer` functions.
 *
 * Lives in `awake-core`, not `awake-vulkan`: it needs [Camera] and `Mat4` (both `awake-core`
 * math, backend-agnostic) to combine a camera's view/projection with each draw call's model
 * matrix, and `awake-core` already depends on `awake-vulkan` (the reverse dependency doesn't
 * exist) -- putting `Renderer` here avoids a cycle.
 *
 * Takes a raw command-pool handle rather than a
 * [io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext] instance: it only needs
 * *a* pool to allocate per-frame command buffers from (the same pool `VulkanApplication`
 * already shared with one-time upload commands), not the one-time-command machinery itself.
 *
 * Still only renders to a single render pass / graphics pipeline (this demo has one of
 * each) -- multiple pipelines per frame is a real future need (once there's more than one
 * `Material`/shader combination) but out of scope for this pass.
 *
 * This class is deliberately just the class body (fields, constructor, `init`, the 3D
 * resource API, and [destroy]) -- the rest of its behavior lives in sibling files as
 * `internal` extension functions on `Renderer`, all in this same package:
 * [RendererUiPipelines.kt] (lazy UI pipeline construction), [RendererSwapchain.kt] (swapchain
 * lifecycle), [RendererDraw3D.kt] (the 3D frame/command-buffer path), [RendererDrawUi.kt]
 * (UI primitive staging), and [RendererVertexWriters.kt] (pure vertex-buffer writers). Every
 * field a moved function touches is `internal`, not `private`, to stay accessible from those
 * extension files -- `internal` stays module-scoped (`awake:backend:vulkan` only), so this is
 * not a real encapsulation loss.
 */
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    internal val lineRenderPipeline: LineRenderPipeline,
    internal val transferContext: TransferContext,
    internal val uiShaders: ShaderPair,
    internal val uiGlyphShaders: ShaderPair,
    internal val uiTextureShaders: ShaderPair,
    internal val uiRoundedQuadShaders: ShaderPair,
    maxFramesInFlight: Int
) : RenderRenderer {
    override val flipYForClipSpace: Boolean = true

    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)

    /** [clearColor] converted to this backend's clear-value type -- read fresh every render
     * pass (not cached), so a [clearColor] mutation takes effect on the very next frame. */
    internal val clearColorValue: VkClearColorValue
        get() = VkClearColorValue.rgba(clearColor[0], clearColor[1], clearColor[2], clearColor[3])

    internal val graphicsDevice = graphicsDevice
    internal val swapchainManager = swapchainManager
    internal val renderPipeline = renderPipeline
    internal val device get() = graphicsDevice.device
    internal val physicalDevice get() = graphicsDevice.physicalDevice
    internal val graphicsQueue get() = graphicsDevice.graphicsQueue
    internal val presentQueue get() = graphicsDevice.presentQueue

    internal var depthImage: Long = 0
    internal var depthImageMemory: Long = 0
    internal var depthImageView: Long = 0
    internal var presentTransitionRenderPass: Long = 0

    // Reused every call by renderToTexture()/readPixels() -- NOT transferContext
    // .runOneTimeCommands(), which allocates a fresh command buffer from the shared pool
    // and never frees it (fine for its own callers, a handful of one-time uploads at
    // startup, but renderToTexture is expected to run every frame for e.g. a minimap, and
    // that leak destabilized the whole Vulkan instance within under a minute of real
    // testing -- confirmed by a real desktop run crashing with VK_SUBOPTIMAL_KHR after
    // ~1500-3000 leaked command buffers). Built lazily on first use.
    internal var offscreenCommandBuffer: Long = 0
    internal var offscreenFence: Long = 0
    internal var framebuffers: List<Long> = emptyList()
    internal var commandBuffers: LongArray = LongArray(maxFramesInFlight)

    // Lazily built on the first drawUi() call of any kind (uiRenderPipeline/uiFramebuffers)
    // and on the first call that passes a non-null font (uiGlyphRenderPipeline/fontTexture)
    // -- see ensureUiQuadPipeline()/ensureGlyphPipeline()'s doc comments. A game that never
    // calls drawUi never builds either pipeline at all.
    internal var uiRenderPipeline: UiRenderPipeline? = null
    internal var uiFramebuffers: List<Long> = emptyList()
    internal var presentTransitionFramebuffers: List<Long> = emptyList()
    internal var uiGlyphRenderPipeline: UiGlyphRenderPipeline? = null
    internal var offscreenGlyphRenderPipeline: UiGlyphRenderPipeline? = null
    internal var fontTexture: Texture? = null
    internal var currentUiFont: UiFont? = null

    // Offscreen counterparts of uiRenderPipeline/uiRoundedQuadRenderPipeline, bound to
    // renderPipeline.renderPass (same pass OffscreenRenderTarget's framebuffer is built
    // against, see createRenderTarget()) instead of the swapchain UI pass -- used by
    // Renderer.renderUiToTexture()'s headless/offscreen full-UI capture path, which has no
    // swapchain UI pipeline to reuse. Same reasoning as offscreenGlyphRenderPipeline.
    internal var offscreenQuadRenderPipeline: UiRenderPipeline? = null
    internal var offscreenRoundedQuadRenderPipeline: UiRoundedQuadRenderPipeline? = null

    // Lazily built on the first drawUi() call that has any Texture primitives -- see
    // ensureTextureQuadPipeline()'s doc comment. Reused every frame after that; a game that
    // never composites a RenderTarget never pays for this pipeline or textureQuadMesh.
    internal var uiTextureRenderPipeline: UiTextureRenderPipeline? = null
    internal var textureQuadMesh: DynamicMesh? = null

    // Lazily built on the first drawUi() call that has any RoundedQuad primitives -- see
    // ensureRoundedQuadPipeline()'s doc comment. Same lazy-pay-only-if-used pattern as the
    // texture/glyph pipelines above.
    internal var uiRoundedQuadRenderPipeline: UiRoundedQuadRenderPipeline? = null

    // Textures created on demand by createMaterial() -- Renderer (not Material) owns their
    // teardown, mirroring how it already owns the UI mesh pools/lineMesh.
    private val createdTextures = mutableListOf<Texture>()

    // RenderTargets created on demand by createRenderTarget() -- same ownership pattern as
    // createdTextures.
    private val createdRenderTargets = mutableListOf<OffscreenRenderTarget>()

    // One DynamicMesh per contiguous same-type run in a frame's primitive list (see
    // drawUi()'s run-coalescing) rather than one mesh per type -- this is what makes
    // cross-type paint order (e.g. a dropdown's overlay quad drawn after a sibling button's
    // glyph, per the original list order) possible: a single "all quads, then all glyphs"
    // mesh pair can only ever draw every quad before every glyph, regardless of the source
    // list's order. Grown on demand, reused every frame (never destroyed/shrunk per-frame),
    // same "stage in drawUi(), consume in recordCommandBuffer()" pattern the old single
    // uiMesh/uiGlyphMesh fields used.
    private val uiQuadMeshPool = mutableListOf<DynamicMesh>()
    private val uiGlyphMeshPool = mutableListOf<DynamicMesh>()
    private val uiRoundedQuadMeshPool = mutableListOf<DynamicMesh>()

    /** One coalesced same-type run of a frame's UI primitives, in original paint order --
     * see `drawUi`'s doc comment (in [RendererDrawUi.kt]) for why runs (not "all quads, then
     * all glyphs") are needed. */
    internal sealed class UiRun {
        class QuadRun(val mesh: DynamicMesh) : UiRun()
        class RoundedQuadRun(val mesh: DynamicMesh) : UiRun()
        class GlyphRun(val mesh: DynamicMesh) : UiRun()
        class TextureRun(val primitives: List<TexturedPrimitiveRun>) : UiRun()

        /** Not a real draw call -- [rect] is already fully resolved (see `UiContext`'s clip
         * stack), so consuming this just means "set the scissor to this rect" at the point
         * in the command sequence where it was originally emitted, same as any other run. */
        class ClipRun(val rect: UiBounds) : UiRun()
    }

    internal data class TexturedPrimitiveRun(
        val material: Any,
        val vertices: FloatArray,
        val indices: IntArray
    )

    /** This frame's runs, in paint order -- staged by `drawUi`, consumed by
     * `recordCommandBuffer` (both in sibling files, see this class's doc comment). */
    internal var uiRuns: List<UiRun> = emptyList()

    internal fun quadMeshForRun(index: Int): DynamicMesh {
        while (uiQuadMeshPool.size <= index) uiQuadMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS)
        return uiQuadMeshPool[index]
    }

    internal fun roundedQuadMeshForRun(index: Int): DynamicMesh {
        while (uiRoundedQuadMeshPool.size <= index) {
            uiRoundedQuadMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX)
        }
        return uiRoundedQuadMeshPool[index]
    }

    internal fun glyphMeshForRun(index: Int): DynamicMesh {
        while (uiGlyphMeshPool.size <= index) {
            uiGlyphMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
        }
        return uiGlyphMeshPool[index]
    }

    // Rewritten every frame by drawDebugLines() (staged before draw(), same pattern as
    // uiMesh/uiGlyphMesh) -- world-space, so draw() writes lineRenderPipeline's MVP uniform
    // from the same viewProjection it already computes for the 3D draw calls.
    internal val lineMesh = LineMesh(graphicsDevice, MAX_DEBUG_LINES)

    init {
        createDepthResources()
        createFramebuffers()
        createPresentTransitionResources()
        createCommandBuffers(transferContext.commandPool.handle, maxFramesInFlight)
    }

    /** Uploads [geometry] as a GPU mesh, on demand -- see [RenderRenderer.createMesh]'s doc
     * comment. */
    override fun createMesh(geometry: MeshGeometry): RenderMesh =
        Mesh(graphicsDevice, transferContext::runOneTimeCommands, geometry.vertices, geometry.indices)

    /** Builds a [Material] bound to this [renderPipeline], uploading [texture] (or a 1x1
     * white placeholder when both [texture]/[renderTarget] are null) -- see
     * [RenderRenderer.createMaterial]'s doc comment. The created [Texture] is tracked in
     * [createdTextures] for teardown in [destroy]; a [renderTarget] is NOT re-tracked here
     * (it's already tracked in [createdRenderTargets] from its own [createRenderTarget]
     * call). */
    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): RenderMaterial {
        require(texture == null || renderTarget == null) { "Pass at most one of texture/renderTarget." }
        val material = Material(graphicsDevice)
        if (renderTarget != null) {
            val offscreen = renderTarget as OffscreenRenderTarget
            material.createResourcesFromRenderTarget(offscreen.sampler, offscreen.colorImageView)
        } else {
            val effectiveTexture = texture ?: PLACEHOLDER_TEXTURE
            val textureInstance = Texture(
                graphicsDevice,
                transferContext::runOneTimeCommands,
                effectiveTexture.data,
                effectiveTexture.width,
                effectiveTexture.height
            )
            createdTextures += textureInstance
            material.createResources(textureInstance)
        }
        return material
    }

    /** Creates an offscreen [width]x[height] color+depth render destination -- see
     * [RenderRenderer.createRenderTarget]'s doc comment. Reuses this [renderPipeline]'s own
     * render pass unmodified (see [OffscreenRenderTarget]'s doc comment for why that's
     * possible without a second graphics pipeline). Tracked in [createdRenderTargets] for
     * teardown in [destroy]. */
    override fun createRenderTarget(width: Int, height: Int): RenderTarget {
        val target = OffscreenRenderTarget(
            graphicsDevice,
            renderPipeline.renderPass,
            width,
            height,
            swapchainManager.imageFormat.value
        )
        createdRenderTargets += target
        return target
    }

    /** Renders [drawCalls] against [camera] into [target] -- see
     * [RenderRenderer.renderToTexture]'s doc comment. Uses a one-time command buffer (this
     * isn't part of the swapchain frame-in-flight cadence [draw] serializes around), and
     * leaves [target] in its [OffscreenRenderTarget] resting layout (`SHADER_READ_ONLY_OPTIMAL`)
     * afterward. */
    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) {
        val offscreen = target as OffscreenRenderTarget
        val aspect = offscreen.width.toFloat() / offscreen.height.toFloat()
        val viewProjection = camera.viewProjectionMatrix(aspect)
        var drawIndex = 0
        while (drawIndex < drawCalls.size) {
            val drawCall = drawCalls[drawIndex]
            val mvp = drawCall.model * viewProjection
            drawCall.material.updateUniformBuffer(mvp.data)
            drawIndex += 1
        }

        runOffscreenCommands { commandBuffer ->
            val renderPassInfo = VkRenderPassBeginInfo(
                renderPass = renderPipeline.renderPass,
                framebuffer = offscreen.framebuffer,
                renderArea = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height)),
                pClearValues = arrayOf(clearColorValue, clearDepthValue)
            )
            Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
            renderPipeline.bind(commandBuffer)
            val viewport = VkViewport(width = offscreen.width.toFloat(), height = offscreen.height.toFloat())
            Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
            val scissor = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height))
            Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
            recordDrawCalls(commandBuffer, drawCalls)
            Vulkan.vkCmdEndRenderPass(commandBuffer)
            offscreen.transitionToShaderReadOnly(commandBuffer)
        }
    }

    /** Reads [target]'s color attachment back to the CPU -- see
     * [RenderRenderer.readPixels]'s doc comment. Synchronous under the hood (a staging
     * buffer + [TransferContext.runOneTimeCommands]'s own fence wait), a valid `suspend fun`
     * implementation even though it never actually suspends (see that method's doc comment
     * for why WebGPU's implementation genuinely needs to). */
    override suspend fun readPixels(target: RenderTarget): TextureAsset {
        val offscreen = target as OffscreenRenderTarget
        val byteSize = (offscreen.width * offscreen.height * 4).toLong()
        val stagingBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(size = byteSize, usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
        )
        val stagingRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, stagingBuffer)
        val stagingMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            stagingRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val stagingMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = stagingRequirements.size, memoryTypeIndex = stagingMemoryTypeIndex)
        )
        VulkanBuffers.vkBindBufferMemory(device, stagingBuffer, stagingMemory, 0)

        val pixels: ByteArray
        try {
            runOffscreenCommands { commandBuffer ->
                VulkanImages.vkTransitionImageLayout(
                    commandBuffer,
                    offscreen.colorImage,
                    VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL
                )
                VulkanImages.vkCmdCopyImageToBuffer(
                    commandBuffer,
                    offscreen.colorImage,
                    stagingBuffer,
                    VkBufferImageCopy(imageWidth = offscreen.width, imageHeight = offscreen.height)
                )
                VulkanImages.vkTransitionImageLayout(
                    commandBuffer,
                    offscreen.colorImage,
                    VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
                )
            }
            pixels = VulkanBuffers.readBufferMemoryBytes(device, stagingMemory, 0, byteSize.toInt())
        } finally {
            VulkanBuffers.vkDestroyBuffer(device, stagingBuffer)
            VulkanBuffers.vkFreeMemory(device, stagingMemory)
        }
        return TextureAsset(pixels, offscreen.width, offscreen.height)
    }

    /** Delegates to [performDraw] ([RendererDraw3D.kt]) -- see that function's doc comment
     * for why this can't just be the extracted body under the same name. */
    override fun draw(camera: Camera, drawCalls: List<DrawCall>) = performDraw(camera, drawCalls)

    /** Delegates to [performDrawUi] ([RendererDrawUi.kt]) -- see [performDraw]'s doc comment
     * for why. */
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = performDrawUi(primitives, font)

    /** Delegates to [performDrawDebugLines] ([RendererDraw3D.kt]) -- see [performDraw]'s doc
     * comment for why. */
    override fun drawDebugLines(lines: List<LineSegment>) = performDrawDebugLines(lines)

    override fun destroy() {
        var index = 0
        val count = framebuffers.size
        while (index < count) {
            Vulkan.vkDestroyFramebuffer(device, framebuffers[index])
            index += 1
        }
        presentTransitionFramebuffers.forEach { Vulkan.vkDestroyFramebuffer(device, it) }
        uiFramebuffers.forEach { Vulkan.vkDestroyFramebuffer(device, it) }
        Vulkan.vkDestroyRenderPass(device, presentTransitionRenderPass)
        uiRenderPipeline?.destroy()
        uiGlyphRenderPipeline?.destroy()
        offscreenGlyphRenderPipeline?.destroy()
        offscreenQuadRenderPipeline?.destroy()
        offscreenRoundedQuadRenderPipeline?.destroy()
        uiTextureRenderPipeline?.destroy()
        uiRoundedQuadRenderPipeline?.destroy()
        textureQuadMesh?.destroy()
        fontTexture?.destroy()
        createdTextures.forEach { it.destroy() }
        createdRenderTargets.forEach { it.destroy() }
        if (offscreenFence != 0L) Vulkan.vkDestroyFence(device, offscreenFence)
        uiQuadMeshPool.forEach { it.destroy() }
        uiGlyphMeshPool.forEach { it.destroy() }
        uiRoundedQuadMeshPool.forEach { it.destroy() }
        lineMesh.destroy()
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
    }

    companion object {
        internal const val DEPTH_FORMAT = 126 // VkFormat.VK_FORMAT_D32_SFLOAT.value
        internal const val MAX_UI_QUADS = 256
        internal const val MAX_DEBUG_LINES = 64
        internal val clearDepthValue = VkClearDepthStencilValue(depth = 1f, stencil = 0)

        internal val WHITE_RGBA = Color.White

        /** [createMaterial]'s fallback when called with a null texture -- same 1x1 white
         * pixel `VulkanGameApplication` used to bind unconditionally. */
        private val PLACEHOLDER_TEXTURE = TextureAsset(byteArrayOf(-1, -1, -1, -1), 1, 1)
    }
}
