/*
 * Awake
 * Awake.awake-core.commonMain
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

package io.github.ronjunevaldoz.awake.core.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
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
    private val graphicsDevice: GraphicsDevice,
    private val swapchainManager: SwapchainManager,
    private val renderPipeline: RenderPipeline,
    commandPool: Long,
    maxFramesInFlight: Int
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice
    private val graphicsQueue get() = graphicsDevice.graphicsQueue
    private val presentQueue get() = graphicsDevice.presentQueue

    private var depthImage: Long = 0
    private var depthImageMemory: Long = 0
    private var depthImageView: Long = 0
    private var framebuffers: List<Long> = emptyList()
    private var commandBuffers: LongArray = LongArray(maxFramesInFlight)

    init {
        createDepthResources()
        createFramebuffers()
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
    fun draw(camera: Camera, drawCalls: List<DrawCall>) {
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
        drawCalls.forEach { drawCall ->
            // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
            // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
            // order) gives the conventional `projection * view * model`.
            val mvp = drawCall.model * viewProjection
            drawCall.material.updateUniformBuffer(mvp.data)
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

        drawCalls.forEach { drawCall ->
            drawCall.mesh.bind(commandBuffer)
            drawCall.material.bind(commandBuffer, renderPipeline.pipelineLayout)
            drawCall.mesh.draw(commandBuffer)
        }

        Vulkan.vkCmdEndRenderPass(commandBuffer)
        Vulkan.vkEndCommandBuffer(commandBuffer)
    }

    fun destroy() {
        framebuffers.forEach { framebuffer ->
            Vulkan.vkDestroyFramebuffer(device, framebuffer)
        }
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
    }

    companion object {
        const val DEPTH_FORMAT = 126 // VkFormat.VK_FORMAT_D32_SFLOAT.value
        val clearColorValue = VkClearColorValue.rgba(0f, 0f, 0f, 1f)
        val clearDepthValue = VkClearDepthStencilValue(depth = 1f, stencil = 0)
    }
}
