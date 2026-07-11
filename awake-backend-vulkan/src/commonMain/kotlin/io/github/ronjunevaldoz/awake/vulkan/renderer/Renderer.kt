// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
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
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
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
    private val uiRenderPipeline: UiRenderPipeline,
    private val uiGlyphRenderPipeline: UiGlyphRenderPipeline,
    commandPool: Long,
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
    private var framebuffers: List<Long> = emptyList()
    private var uiFramebuffers: List<Long> = emptyList()
    private var commandBuffers: LongArray = LongArray(maxFramesInFlight)

    // Rewritten every frame by drawUi() (called before draw() -- see VulkanGameApplication
    // .onRender()'s ordering) so recordCommandBuffer's UI pass, later in the SAME command
    // buffer as the 3D pass, always draws this frame's widgets, not last frame's.
    private val uiMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS)
    private val uiGlyphMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)

    init {
        createDepthResources()
        createFramebuffers()
        createUiFramebuffers()
        createCommandBuffers(commandPool, maxFramesInFlight)
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

    /** Separate from [createFramebuffers]: the UI pass's render pass object differs from
     * the 3D pass's (single color attachment, no depth), so Vulkan requires its own
     * framebuffer even though both wrap the identical swapchain image view. */
    private fun createUiFramebuffers() {
        uiFramebuffers = swapchainManager.imageViews.map { imageView ->
            Vulkan.vkCreateFramebuffer(
                device,
                VkFramebufferCreateInfo(
                    renderPass = uiRenderPipeline.renderPass,
                    pAttachments = arrayOf(imageView),
                    width = swapchainManager.extent.width,
                    height = swapchainManager.extent.height,
                    layers = 1
                )
            )
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
        Vulkan.vkResetFences(device, longArrayOf(swapchainManager.inFlightFences[currentFrame]))

        val imageIndex = Vulkan.vkAcquireNextImageKHR(
            device,
            swapchainManager.swapChain,
            Int.MAX_VALUE.toLong(),
            swapchainManager.imageAvailableSemaphores[currentFrame],
            0
        )

        val aspect = swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat()
        val viewProjection = camera.viewProjectionMatrix(aspect)
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
     * widgets rather than lagging a frame behind. */
    override fun drawUi(primitives: List<UiDrawPrimitive>) {
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

    private fun recreateSwapChain() {
        // TODO process recreation of swapchain here
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

        var drawIndex = 0
        val drawCount = drawCalls.size
        while (drawIndex < drawCount) {
            val drawCall = drawCalls[drawIndex]
            drawCall.mesh.bind(commandBuffer)
            drawCall.material.bind(commandBuffer, renderPipeline.pipelineLayout)
            drawCall.mesh.draw(commandBuffer)
            drawIndex += 1
        }

        Vulkan.vkCmdEndRenderPass(commandBuffer)

        // Second pass, same command buffer, drawn on top of the 3D pass's output (that
        // pass's finalLayout leaves the image in COLOR_ATTACHMENT_OPTIMAL specifically so
        // this pass can pick up from there -- see RenderPipeline.kt's createRenderPass).
        val uiRenderPassInfo = VkRenderPassBeginInfo(
            renderPass = uiRenderPipeline.renderPass,
            framebuffer = uiFramebuffers[acquiredImageIndex],
            renderArea = VkRect2D(extent = swapchainManager.extent)
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, uiRenderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        uiRenderPipeline.bind(commandBuffer)
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        uiMesh.bind(commandBuffer)
        uiMesh.draw(commandBuffer)

        // Phase B: glyph quads drawn with a second, textured pipeline, same render pass/
        // subpass, after the colored quads (so text composites on top of button fills).
        uiGlyphRenderPipeline.bind(commandBuffer)
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        uiGlyphMesh.bind(commandBuffer)
        uiGlyphMesh.draw(commandBuffer)

        Vulkan.vkCmdEndRenderPass(commandBuffer)

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
        uiMesh.destroy()
        uiGlyphMesh.destroy()
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
    }

    companion object {
        const val DEPTH_FORMAT = 126 // VkFormat.VK_FORMAT_D32_SFLOAT.value
        const val MAX_UI_QUADS = 256
        val clearColorValue = VkClearColorValue.rgba(0f, 0f, 0f, 1f)
        val clearDepthValue = VkClearDepthStencilValue(depth = 1f, stencil = 0)
    }
}
