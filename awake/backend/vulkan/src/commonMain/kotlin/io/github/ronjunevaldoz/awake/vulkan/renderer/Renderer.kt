// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

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
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiTextureRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException

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
 */
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    private val lineRenderPipeline: LineRenderPipeline,
    private val transferContext: TransferContext,
    private val uiVertexShaderCode: ByteArray,
    private val uiFragmentShaderCode: ByteArray,
    private val uiGlyphVertexShaderCode: ByteArray,
    private val uiGlyphFragmentShaderCode: ByteArray,
    private val uiTextureVertexShaderCode: ByteArray,
    private val uiTextureFragmentShaderCode: ByteArray,
    maxFramesInFlight: Int
) : RenderRenderer {
    private val graphicsDevice = graphicsDevice
    private val swapchainManager = swapchainManager
    private val renderPipeline = renderPipeline
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice
    private val graphicsQueue get() = graphicsDevice.graphicsQueue
    private val presentQueue get() = graphicsDevice.presentQueue

    private var depthImage: Long = 0
    private var depthImageMemory: Long = 0
    private var depthImageView: Long = 0

    // Reused every call by renderToTexture()/readPixels() -- NOT transferContext
    // .runOneTimeCommands(), which allocates a fresh command buffer from the shared pool
    // and never frees it (fine for its own callers, a handful of one-time uploads at
    // startup, but renderToTexture is expected to run every frame for e.g. a minimap, and
    // that leak destabilized the whole Vulkan instance within under a minute of real
    // testing -- confirmed by a real desktop run crashing with VK_SUBOPTIMAL_KHR after
    // ~1500-3000 leaked command buffers). Built lazily on first use.
    private var offscreenCommandBuffer: Long = 0
    private var offscreenFence: Long = 0
    private var framebuffers: List<Long> = emptyList()
    private var commandBuffers: LongArray = LongArray(maxFramesInFlight)

    // Lazily built on the first drawUi() call of any kind (uiRenderPipeline/uiFramebuffers)
    // and on the first call that passes a non-null font (uiGlyphRenderPipeline/fontTexture)
    // -- see ensureUiQuadPipeline()/ensureGlyphPipeline()'s doc comments. A game that never
    // calls drawUi never builds either pipeline at all.
    private var uiRenderPipeline: UiRenderPipeline? = null
    private var uiFramebuffers: List<Long> = emptyList()
    private var uiGlyphRenderPipeline: UiGlyphRenderPipeline? = null
    private var fontTexture: Texture? = null

    // Lazily built on the first drawUi() call that has any Texture primitives -- see
    // ensureTextureQuadPipeline()'s doc comment. Reused every frame after that; a game that
    // never composites a RenderTarget never pays for this pipeline or textureQuadMesh.
    private var uiTextureRenderPipeline: UiTextureRenderPipeline? = null
    private var textureQuadMesh: DynamicMesh? = null
    private var texturePrimitives: List<UiDrawPrimitive.Texture> = emptyList()

    // Textures created on demand by createMaterial() -- Renderer (not Material) owns their
    // teardown, mirroring how it already owns uiMesh/uiGlyphMesh/lineMesh.
    private val createdTextures = mutableListOf<Texture>()

    // RenderTargets created on demand by createRenderTarget() -- same ownership pattern as
    // createdTextures.
    private val createdRenderTargets = mutableListOf<OffscreenRenderTarget>()

    // Rewritten every frame by drawUi() (called before draw() -- see VulkanGameApplication
    // .onRender()'s ordering) so recordCommandBuffer's UI pass, later in the SAME command
    // buffer as the 3D pass, always draws this frame's widgets, not last frame's.
    private val uiMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS)
    private val uiGlyphMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)

    // Rewritten every frame by drawDebugLines() (staged before draw(), same pattern as
    // uiMesh/uiGlyphMesh) -- world-space, so draw() writes lineRenderPipeline's MVP uniform
    // from the same viewProjection it already computes for the 3D draw calls.
    private val lineMesh = LineMesh(graphicsDevice, MAX_DEBUG_LINES)

    init {
        createDepthResources()
        createFramebuffers()
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

    /** Builds [uiRenderPipeline]/[uiFramebuffers] on the first [drawUi] call of any kind --
     * quad rendering doesn't need a font, so this doesn't wait for one. Cached after the
     * first build (a game calls [drawUi] every frame). */
    private fun ensureUiQuadPipeline() {
        if (uiRenderPipeline != null) return
        val pipeline = UiRenderPipeline(graphicsDevice, swapchainManager, uiVertexShaderCode, uiFragmentShaderCode)
        uiRenderPipeline = pipeline
        uiFramebuffers = buildUiFramebuffers(pipeline.renderPass)
    }

    /** One framebuffer per swapchain image view, against [renderPass] -- shared by
     * [ensureUiQuadPipeline] (first build) and [recreateSwapChain] (rebuild after the
     * swapchain's image views change). */
    private fun buildUiFramebuffers(renderPass: Long): List<Long> = swapchainManager.imageViews.map { imageView ->
        Vulkan.vkCreateFramebuffer(
            device,
            VkFramebufferCreateInfo(
                renderPass = renderPass,
                pAttachments = arrayOf(imageView),
                width = swapchainManager.extent.width,
                height = swapchainManager.extent.height,
                layers = 1
            )
        )
    }.toList()

    /** Builds [uiGlyphRenderPipeline]/[fontTexture] on the first [drawUi] call that passes a
     * non-null [font] -- cached after that (a game calls [drawUi] with the same font every
     * frame). Requires [ensureUiQuadPipeline] to already have run (needs its render pass). */
    private fun ensureGlyphPipeline(font: BitmapFont) {
        if (uiGlyphRenderPipeline != null) return
        val renderPass = requireNotNull(uiRenderPipeline) { "ensureUiQuadPipeline() must run first." }.renderPass
        val texture = Texture(
            graphicsDevice,
            transferContext::runOneTimeCommands,
            font.atlasPixelsRgba,
            font.atlasWidth,
            font.atlasHeight
        )
        fontTexture = texture
        uiGlyphRenderPipeline = UiGlyphRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPass,
            uiGlyphVertexShaderCode,
            uiGlyphFragmentShaderCode,
            texture
        )
    }

    /** Builds [uiTextureRenderPipeline]/[textureQuadMesh] on the first [drawUi] call that
     * has any [UiDrawPrimitive.Texture] primitives -- cached after that. Requires
     * [ensureUiQuadPipeline] to already have run (needs its render pass), same precondition
     * [ensureGlyphPipeline] has. */
    private fun ensureTextureQuadPipeline() {
        if (uiTextureRenderPipeline != null) return
        val renderPass = requireNotNull(uiRenderPipeline) { "ensureUiQuadPipeline() must run first." }.renderPass
        uiTextureRenderPipeline = UiTextureRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPass,
            uiTextureVertexShaderCode,
            uiTextureFragmentShaderCode
        )
        textureQuadMesh = DynamicMesh(graphicsDevice, 1, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
    }

    private fun createDepthResources() {
        depthImage = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = swapchainManager.extent.width,
                height = swapchainManager.extent.height,
                format = DEPTH_FORMAT,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
            )
        )
        val requirements = VulkanImages.vkGetImageMemoryRequirements(device, depthImage)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        depthImageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = requirements.size,
                memoryTypeIndex = memoryTypeIndex
            )
        )
        VulkanImages.vkBindImageMemory(device, depthImage, depthImageMemory, 0)
        depthImageView = Vulkan.vkCreateImageView(
            device,
            VkImageViewCreateInfo(
                image = depthImage,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = VkFormat.VK_FORMAT_D32_SFLOAT,
                subresourceRange = VkImageSubresourceRange(
                    aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_DEPTH_BIT.value,
                    baseMipLevel = 0,
                    levelCount = 1,
                    baseArrayLayer = 0,
                    layerCount = 1
                )
            )
        )
    }

    private fun createFramebuffers() {
        framebuffers = swapchainManager.imageViews.map { imageView ->
            val frameBufferInfo = VkFramebufferCreateInfo(
                renderPass = renderPipeline.renderPass,
                // depthImageView is shared across every framebuffer -- only one frame is
                // ever actually in the depth-write phase at a time given draw()'s
                // vkDeviceWaitIdle serialization, so this is safe (a "real" per-frame-in-
                // flight setup would need one depth image per frame-in-flight instead).
                pAttachments = arrayOf(imageView, depthImageView),
                width = swapchainManager.extent.width,
                height = swapchainManager.extent.height,
                layers = 1
            )
            Vulkan.vkCreateFramebuffer(device, frameBufferInfo)
        }.toList()
    }

    private fun createCommandBuffers(commandPool: Long, maxFramesInFlight: Int) {
        val allocInfo = VkCommandBufferAllocateInfo(
            commandPool = commandPool,
            level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            commandBufferCount = 1
        )
        for (i in 0 until maxFramesInFlight) {
            commandBuffers[i] = Vulkan.vkAllocateCommandBuffers(device, allocInfo)
        }
    }

    /** [renderToTexture]/[readPixels]'s own one-time-command runner -- see
     * [offscreenCommandBuffer]'s doc comment for why this doesn't use
     * `transferContext.runOneTimeCommands`. Allocates its command buffer/fence once, on
     * first use, then resets and reuses both every call after that. */
    private fun runOffscreenCommands(block: (Long) -> Unit) {
        if (offscreenCommandBuffer == 0L) {
            offscreenCommandBuffer = Vulkan.vkAllocateCommandBuffers(
                device,
                VkCommandBufferAllocateInfo(
                    commandPool = transferContext.commandPool.handle,
                    level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
                    commandBufferCount = 1
                )
            )
            offscreenFence = Vulkan.vkCreateFence(device, VkFenceCreateInfo())
        }
        Vulkan.vkResetCommandBuffer(offscreenCommandBuffer, 0)
        Vulkan.vkBeginCommandBuffer(
            offscreenCommandBuffer,
            VkCommandBufferBeginInfo(flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value)
        )
        block(offscreenCommandBuffer)
        Vulkan.vkEndCommandBuffer(offscreenCommandBuffer)

        Vulkan.vkResetFences(device, longArrayOf(offscreenFence))
        Vulkan.vkQueueSubmit(
            graphicsQueue,
            arrayOf(VkSubmitInfo(pCommandBuffers = arrayOf(offscreenCommandBuffer))),
            offscreenFence
        )
        Vulkan.vkWaitForFences(device, longArrayOf(offscreenFence), true, Long.MAX_VALUE)
    }

    /** Renders one frame: waits for this frame-in-flight slot, acquires a swapchain image,
     * writes each [DrawCall]'s MVP matrix (model combined with [camera]'s view/projection)
     * into its own material's uniform buffer, records and submits a command buffer that
     * draws every call in order, then presents. Fully serializes frames afterward (see the
     * `vkDeviceWaitIdle` call below) so each material's single (not per-frame-in-flight)
     * uniform buffer can be safely rewritten every frame -- a real engine would double-buffer
     * those per frame-in-flight instead of paying this full-pipeline stall; deferred as a
     * later Phase 2 concern, unchanged from before this extraction. */
    override fun draw(camera: Camera, drawCalls: List<DrawCall>) {
        val currentFrame = swapchainManager.currentFrame
        Vulkan.vkWaitForFences(
            device,
            longArrayOf(swapchainManager.inFlightFences[currentFrame]),
            true,
            Long.MAX_VALUE
        )
        // Reset only AFTER a successful acquire (not before) -- if acquire throws and this
        // frame bails out early (see the catch below), an already-reset-but-never-submitted
        // fence would stay unsignaled forever, hanging the NEXT draw() call's
        // vkWaitForFences on this same frame-in-flight slot indefinitely.
        val imageIndex: Int
        try {
            imageIndex = Vulkan.vkAcquireNextImageKHR(
                device,
                swapchainManager.swapChain,
                Int.MAX_VALUE.toLong(),
                swapchainManager.imageAvailableSemaphores[currentFrame],
                0
            )
        } catch (e: VkResultException) {
            when (e.result) {
                // A resized/minimized/moved-to-another-display window makes the swapchain
                // stale before this frame's acquire even runs (not just after present, see
                // the catch around vkQueuePresentKHR below) -- recreate and skip this frame
                // entirely: there's no valid acquired image to record/submit/present against.
                VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> {
                    recreateSwapChain()
                    return
                }
                else -> throw e
            }
        }
        Vulkan.vkResetFences(device, longArrayOf(swapchainManager.inFlightFences[currentFrame]))

        val aspect = swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat()
        val viewProjection = camera.viewProjectionMatrix(aspect)
        // Debug lines are already in world space (no per-line model matrix), so their MVP
        // is exactly this frame's viewProjection.
        lineRenderPipeline.writeMvp(viewProjection.data)
        var drawIndex = 0
        val drawCount = drawCalls.size
        while (drawIndex < drawCount) {
            val drawCall = drawCalls[drawIndex]
            // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
            // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
            // order) gives the conventional `projection * view * model`.
            val mvp = drawCall.model * viewProjection
            drawCall.material.updateUniformBuffer(mvp.data)
            drawIndex += 1
        }

        Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
        recordCommandBuffer(commandBuffers[currentFrame], imageIndex, drawCalls)

        val waitSemaphores = arrayOf(swapchainManager.imageAvailableSemaphores[currentFrame])
        val waitStages =
            intArrayOf(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value)
        val signalSemaphores = arrayOf(swapchainManager.renderFinishedSemaphores[currentFrame])

        val submitInfo = VkSubmitInfo(
            pWaitSemaphores = waitSemaphores,
            pWaitDstStageMask = waitStages,
            pCommandBuffers = arrayOf(commandBuffers[currentFrame]),
            pSignalSemaphores = signalSemaphores
        )

        Vulkan.vkQueueSubmit(graphicsQueue, arrayOf(submitInfo), swapchainManager.inFlightFences[currentFrame])

        val presentInfo = VkPresentInfoKHR(
            pWaitSemaphores = signalSemaphores,
            pSwapchains = arrayOf(swapchainManager.swapChain),
            pImageIndices = intArrayOf(imageIndex),
            pResults = VkResult.values()
        )

        try {
            Vulkan.vkQueuePresentKHR(presentQueue, presentInfo)
        } catch (e: VkResultException) {
            when (e.result) {
                VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> recreateSwapChain()
                else -> throw e
            }
        }

        swapchainManager.currentFrame = (currentFrame + 1) % commandBuffers.size

        VulkanBuffers.vkDeviceWaitIdle(device)
    }

    /** Stages this frame's UI overlay content -- rewrites [uiMesh]'s buffers but issues no
     * GPU commands itself. Must be called BEFORE [draw] (see `VulkanGameApplication
     * .onRender()`'s ordering) so [recordCommandBuffer]'s UI pass, appended to the SAME
     * command buffer as the 3D pass inside that same [draw] call, draws this frame's
     * widgets rather than lagging a frame behind. Lazily builds the (quad) UI pipeline on
     * first call, and the glyph pipeline on the first call that passes a non-null [font] --
     * see [ensureUiQuadPipeline]/[ensureGlyphPipeline]'s doc comments. */
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont?) {
        ensureUiQuadPipeline()
        if (font != null) ensureGlyphPipeline(font)
        texturePrimitives = primitives.filterIsInstance<UiDrawPrimitive.Texture>()
        if (texturePrimitives.isNotEmpty()) ensureTextureQuadPipeline()
        val quads = primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        require(quads.size <= MAX_UI_QUADS) {
            "UI quad count (${quads.size}) exceeds Renderer's DynamicMesh capacity ($MAX_UI_QUADS)."
        }
        val vertices = FloatArray(quads.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX)
        val indices = IntArray(quads.size * DynamicMesh.INDICES_PER_QUAD)
        var quadIndex = 0
        while (quadIndex < quads.size) {
            val quad = quads[quadIndex]
            val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX
            // Triangle-list quad, corners in TL/TR/BR/BL order (pixel space, Y-down).
            writeVertex(vertices, vertexBase + 0 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y, quad.color)
            writeVertex(vertices, vertexBase + 1 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.color)
            writeVertex(vertices, vertexBase + 2 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.color)
            writeVertex(vertices, vertexBase + 3 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.color)

            val vertexOffset = quadIndex * DynamicMesh.VERTICES_PER_QUAD
            val indexBase = quadIndex * DynamicMesh.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        uiMesh.update(vertices, indices)

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        require(glyphs.size <= MAX_UI_QUADS) {
            "UI glyph count (${glyphs.size}) exceeds Renderer's DynamicMesh capacity ($MAX_UI_QUADS)."
        }
        val glyphVertices = FloatArray(glyphs.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
        val glyphIndices = IntArray(glyphs.size * DynamicMesh.INDICES_PER_QUAD)
        var glyphIndex = 0
        while (glyphIndex < glyphs.size) {
            val glyph = glyphs[glyphIndex]
            val vertexBase = glyphIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX
            writeGlyphVertex(glyphVertices, vertexBase + 0 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y, glyph.u0, glyph.v0, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 1 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y, glyph.u1, glyph.v0, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 2 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y + glyph.h, glyph.u1, glyph.v1, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 3 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y + glyph.h, glyph.u0, glyph.v1, glyph.color)

            val vertexOffset = glyphIndex * DynamicMesh.VERTICES_PER_QUAD
            val indexBase = glyphIndex * DynamicMesh.INDICES_PER_QUAD
            glyphIndices[indexBase] = vertexOffset
            glyphIndices[indexBase + 1] = vertexOffset + 1
            glyphIndices[indexBase + 2] = vertexOffset + 2
            glyphIndices[indexBase + 3] = vertexOffset + 2
            glyphIndices[indexBase + 4] = vertexOffset + 3
            glyphIndices[indexBase + 5] = vertexOffset
            glyphIndex += 1
        }
        uiGlyphMesh.update(glyphVertices, glyphIndices)
    }

    /** Stages this frame's world-space debug lines (e.g. a frustum wireframe) -- rewrites
     * [lineMesh]'s buffer but issues no GPU commands itself, same "stage now, consume on
     * next draw" pattern as [drawUi]. Call before [draw] each frame. */
    override fun drawDebugLines(lines: List<LineSegment>) {
        require(lines.size <= MAX_DEBUG_LINES) {
            "Debug line count (${lines.size}) exceeds Renderer's LineMesh capacity ($MAX_DEBUG_LINES)."
        }
        val vertices = FloatArray(lines.size * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX)
        var lineIndex = 0
        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            val vertexBase = lineIndex * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX
            writeLineVertex(vertices, vertexBase, line.start.x, line.start.y, line.start.z, line.color)
            writeLineVertex(vertices, vertexBase + LineMesh.FLOATS_PER_VERTEX, line.end.x, line.end.y, line.end.z, line.color)
            lineIndex += 1
        }
        lineMesh.update(vertices)
    }

    private fun writeLineVertex(out: FloatArray, offset: Int, x: Float, y: Float, z: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = z
        out[offset + 3] = color[0]
        out[offset + 4] = color[1]
        out[offset + 5] = color[2]
        out[offset + 6] = if (color.size > 3) color[3] else 1f
    }

    private fun writeVertex(out: FloatArray, offset: Int, x: Float, y: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = color[0]
        out[offset + 3] = color[1]
        out[offset + 4] = color[2]
        out[offset + 5] = if (color.size > 3) color[3] else 1f
    }

    private fun writeGlyphVertex(out: FloatArray, offset: Int, x: Float, y: Float, u: Float, v: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = u
        out[offset + 3] = v
        out[offset + 4] = color[0]
        out[offset + 5] = color[1]
        out[offset + 6] = color[2]
        out[offset + 7] = if (color.size > 3) color[3] else 1f
    }

    /** Rebuilds the swapchain (and everything sized off it -- depth image, main + UI
     * framebuffers) after `vkAcquireNextImageKHR`/`vkQueuePresentKHR` reports
     * `VK_SUBOPTIMAL_KHR`/`VK_ERROR_OUT_OF_DATE_KHR` (typically a window resize/maximize).
     * Doesn't touch any graphics pipeline (main, UI-quad, glyph, or texture-quad): all of
     * them declare `VK_DYNAMIC_STATE_VIEWPORT`/`SCISSOR`, so [recordCommandBuffer] already
     * sets the correct viewport/scissor from the live (post-recreate) [swapchainManager]
     * .extent] every frame -- only the UI pipelines' screen-size uniform buffers (used for
     * their vertex shaders' pixel-to-NDC math, not dynamic state) need rewriting here. */
    private fun recreateSwapChain() {
        // Nothing may still be reading/writing swapchain-derived resources (framebuffers,
        // depth image, image views) while they're torn down and rebuilt below.
        VulkanBuffers.vkDeviceWaitIdle(device)

        var index = 0
        while (index < framebuffers.size) {
            Vulkan.vkDestroyFramebuffer(device, framebuffers[index])
            index += 1
        }
        uiFramebuffers.forEach { Vulkan.vkDestroyFramebuffer(device, it) }
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)

        swapchainManager.destroy()
        // oldSwapchain (read by swapchainManager.create() below) must not reference an
        // already-destroyed handle -- VK_NULL_HANDLE is the well-defined "no chaining" case.
        swapchainManager.swapChain = 0

        swapchainManager.create()
        createDepthResources()
        createFramebuffers()

        val uiPipeline = uiRenderPipeline
        if (uiPipeline != null) {
            uiFramebuffers = buildUiFramebuffers(uiPipeline.renderPass)
            uiPipeline.writeScreenSize(swapchainManager.extent.width.toFloat(), swapchainManager.extent.height.toFloat())
        }
        uiGlyphRenderPipeline?.writeScreenSize(
            swapchainManager.extent.width.toFloat(),
            swapchainManager.extent.height.toFloat()
        )
        uiTextureRenderPipeline?.writeScreenSize(
            swapchainManager.extent.width.toFloat(),
            swapchainManager.extent.height.toFloat()
        )
    }

    /** Binds+draws each [drawCalls] entry against whatever render pass/pipeline is already
     * bound on [commandBuffer] -- shared by [recordCommandBuffer] (the swapchain frame) and
     * [renderToTexture] (an offscreen frame), so the two don't duplicate this loop. */
    private fun recordDrawCalls(commandBuffer: Long, drawCalls: List<DrawCall>) {
        var drawIndex = 0
        val drawCount = drawCalls.size
        while (drawIndex < drawCount) {
            val drawCall = drawCalls[drawIndex]
            drawCall.mesh.bind(commandBuffer)
            drawCall.material.bind(commandBuffer, renderPipeline.pipelineLayout)
            drawCall.mesh.draw(commandBuffer)
            drawIndex += 1
        }
    }

    private fun recordCommandBuffer(commandBuffer: Long, acquiredImageIndex: Int, drawCalls: List<DrawCall>) {
        val beginInfo = VkCommandBufferBeginInfo(
            flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value,
        )
        Vulkan.vkBeginCommandBuffer(commandBuffer, beginInfo)

        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
            framebuffer = framebuffers[acquiredImageIndex],
            renderArea = VkRect2D(
                extent = swapchainManager.extent
            ),
            pClearValues = arrayOf(clearColorValue, clearDepthValue)
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            renderPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE
        )

        renderPipeline.bind(commandBuffer)
        val viewport = VkViewport(
            width = swapchainManager.extent.width.toFloat(),
            height = swapchainManager.extent.height.toFloat(),
        )
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(
            extent = swapchainManager.extent
        )
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))

        recordDrawCalls(commandBuffer, drawCalls)

        // Debug lines (e.g. a frustum wireframe), same render pass/depth attachment as
        // the 3D draw calls above -- real depth-testing against scene geometry, not an
        // X-ray overlay. Still inside this pass, before it ends.
        lineRenderPipeline.bind(commandBuffer)
        lineMesh.bind(commandBuffer)
        lineMesh.draw(commandBuffer)

        Vulkan.vkCmdEndRenderPass(commandBuffer)

        // Second pass, same command buffer, drawn on top of the 3D pass's output (that
        // pass's finalLayout leaves the image in COLOR_ATTACHMENT_OPTIMAL specifically so
        // this pass can pick up from there -- see RenderPipeline.kt's createRenderPass).
        // Only recorded once drawUi() has actually built a UI pipeline at least once (a game
        // that never calls drawUi never pays for this pass at all) -- mirrors WebGPU's
        // Renderer.draw()'s equivalent `uiMesh.drawIndexCount > 0` guard.
        val uiPipeline = uiRenderPipeline
        if (uiPipeline != null) {
            val uiRenderPassInfo = VkRenderPassBeginInfo(
                renderPass = uiPipeline.renderPass,
                framebuffer = uiFramebuffers[acquiredImageIndex],
                renderArea = VkRect2D(extent = swapchainManager.extent)
            )
            Vulkan.vkCmdBeginRenderPass(commandBuffer, uiRenderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
            uiPipeline.bind(commandBuffer)
            Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
            Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
            uiMesh.bind(commandBuffer)
            uiMesh.draw(commandBuffer)

            // Phase B: glyph quads drawn with a second, textured pipeline, same render
            // pass/subpass, after the colored quads (so text composites on top of button
            // fills) -- only bound once a caller has actually passed a font to drawUi().
            val glyphPipeline = uiGlyphRenderPipeline
            if (glyphPipeline != null) {
                glyphPipeline.bind(commandBuffer)
                Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
                Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
                uiGlyphMesh.bind(commandBuffer)
                uiGlyphMesh.draw(commandBuffer)
            }

            // Render-target-backed textured quads (e.g. a minimap), one draw call per
            // primitive -- each rewrites the texture pipeline's one shared descriptor set
            // (see UiTextureRenderPipeline's doc comment for why that's safe here).
            val texturePipeline = uiTextureRenderPipeline
            val quadMesh = textureQuadMesh
            if (texturePipeline != null && quadMesh != null) {
                Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
                Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
                var textureIndex = 0
                while (textureIndex < texturePrimitives.size) {
                    val primitive = texturePrimitives[textureIndex]
                    val material = primitive.material as Material
                    texturePipeline.bindMaterial(commandBuffer, material.samplerHandle, material.imageViewHandle)
                    val vertices = floatArrayOf(
                        primitive.x, primitive.y, 0f, 0f, 1f, 1f, 1f, 1f,
                        primitive.x + primitive.w, primitive.y, 1f, 0f, 1f, 1f, 1f, 1f,
                        primitive.x + primitive.w, primitive.y + primitive.h, 1f, 1f, 1f, 1f, 1f, 1f,
                        primitive.x, primitive.y + primitive.h, 0f, 1f, 1f, 1f, 1f, 1f
                    )
                    quadMesh.update(vertices, TEXTURE_QUAD_INDICES)
                    quadMesh.bind(commandBuffer)
                    quadMesh.draw(commandBuffer)
                    textureIndex += 1
                }
            }

            Vulkan.vkCmdEndRenderPass(commandBuffer)
        }

        Vulkan.vkEndCommandBuffer(commandBuffer)
    }

    override fun destroy() {
        var index = 0
        val count = framebuffers.size
        while (index < count) {
            Vulkan.vkDestroyFramebuffer(device, framebuffers[index])
            index += 1
        }
        uiFramebuffers.forEach { Vulkan.vkDestroyFramebuffer(device, it) }
        uiRenderPipeline?.destroy()
        uiGlyphRenderPipeline?.destroy()
        uiTextureRenderPipeline?.destroy()
        textureQuadMesh?.destroy()
        fontTexture?.destroy()
        createdTextures.forEach { it.destroy() }
        createdRenderTargets.forEach { it.destroy() }
        if (offscreenFence != 0L) Vulkan.vkDestroyFence(device, offscreenFence)
        uiMesh.destroy()
        uiGlyphMesh.destroy()
        lineMesh.destroy()
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
    }

    companion object {
        const val DEPTH_FORMAT = 126 // VkFormat.VK_FORMAT_D32_SFLOAT.value
        const val MAX_UI_QUADS = 256
        const val MAX_DEBUG_LINES = 64
        val clearColorValue = VkClearColorValue.rgba(0f, 0f, 0f, 1f)
        val clearDepthValue = VkClearDepthStencilValue(depth = 1f, stencil = 0)

        /** Triangle-list quad, corners in TL/TR/BR/BL order -- same winding [drawUi]'s own
         * colored/glyph quads use. */
        private val TEXTURE_QUAD_INDICES = intArrayOf(0, 1, 2, 2, 3, 0)

        /** [createMaterial]'s fallback when called with a null texture -- same 1x1 white
         * pixel `VulkanGameApplication` used to bind unconditionally. */
        private val PLACEHOLDER_TEXTURE = TextureAsset(byteArrayOf(-1, -1, -1, -1), 1, 1)
    }
}
