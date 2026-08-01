// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.vulkan.VK_SUBPASS_EXTERNAL
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentLoadOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkAccessFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentDescription
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference
import io.github.ronjunevaldoz.awake.vulkan.models.VkSubpassDependency
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubpassDescription

/** Swapchain-sized resource lifecycle -- depth buffer, framebuffers, the present-transition
 * fallback pass, per-frame command buffers, and [recreateSwapChain]'s full teardown/rebuild
 * after `VK_SUBOPTIMAL_KHR`/`VK_ERROR_OUT_OF_DATE_KHR`. See [Renderer]'s class doc comment for
 * why this lives here as `internal` extension functions rather than as members. */

internal fun Renderer.createDepthResources() {
    depthImage = VulkanImages.vkCreateImage(
        device,
        VkImageCreateInfo(
            width = swapchainManager.extent.width,
            height = swapchainManager.extent.height,
            format = Renderer.DEPTH_FORMAT,
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

internal fun Renderer.createFramebuffers() {
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

/** Empty render pass used only when no UI overlay pass was recorded for the frame. It
 * preserves the 3D color attachment contents and performs the swapchain image's final
 * COLOR_ATTACHMENT_OPTIMAL -> PRESENT_SRC_KHR transition before present. */
internal fun Renderer.createPresentTransitionResources() {
    presentTransitionRenderPass = Vulkan.vkCreateRenderPass(
        device,
        VkRenderPassCreateInfo(
            pAttachments = arrayOf(
                VkAttachmentDescription(
                    format = swapchainManager.imageFormat,
                    loadOp = VkAttachmentLoadOp.LOAD,
                    initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
                )
            ),
            pSubpasses = arrayOf(
                VkSubpassDescription(
                    pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pColorAttachments = arrayOf(
                        VkAttachmentReference(
                            attachment = 0,
                            layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                        )
                    )
                )
            ),
            pDependencies = arrayOf(
                VkSubpassDependency(
                    srcSubpass = VK_SUBPASS_EXTERNAL,
                    dstSubpass = 0,
                    srcStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value,
                    srcAccessMask = 0,
                    dstStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value,
                    dstAccessMask = VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT.value or
                        VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.value
                )
            )
        )
    )
    presentTransitionFramebuffers = buildPresentTransitionFramebuffers()
}

internal fun Renderer.buildPresentTransitionFramebuffers(): List<Long> = swapchainManager.imageViews.map { imageView ->
    Vulkan.vkCreateFramebuffer(
        device,
        VkFramebufferCreateInfo(
            renderPass = presentTransitionRenderPass,
            pAttachments = arrayOf(imageView),
            width = swapchainManager.extent.width,
            height = swapchainManager.extent.height,
            layers = 1
        )
    )
}.toList()

internal fun Renderer.createCommandBuffers(commandPool: Long, maxFramesInFlight: Int) {
    val allocInfo = VkCommandBufferAllocateInfo(
        commandPool = commandPool,
        level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
        commandBufferCount = 1
    )
    for (i in 0 until maxFramesInFlight) {
        commandBuffers[i] = Vulkan.vkAllocateCommandBuffers(device, allocInfo)
    }
}

/** Rebuilds the swapchain (and everything sized off it -- depth image, main + UI
 * framebuffers) after `vkAcquireNextImageKHR`/`vkQueuePresentKHR` reports
 * `VK_SUBOPTIMAL_KHR`/`VK_ERROR_OUT_OF_DATE_KHR` (typically a window resize/maximize).
 * Doesn't touch any graphics pipeline (main, UI-quad, glyph, or texture-quad): all of
 * them declare `VK_DYNAMIC_STATE_VIEWPORT`/`SCISSOR`, so `recordCommandBuffer` already
 * sets the correct viewport/scissor from the live (post-recreate) [Renderer.swapchainManager]
 * `.extent` every frame -- only the UI pipelines' screen-size uniform buffers (used for
 * their vertex shaders' pixel-to-NDC math, not dynamic state) need rewriting here. */
internal fun Renderer.recreateSwapChain() {
    // Nothing may still be reading/writing swapchain-derived resources (framebuffers,
    // depth image, image views) while they're torn down and rebuilt below.
    VulkanBuffers.vkDeviceWaitIdle(device)

    var index = 0
    while (index < framebuffers.size) {
        Vulkan.vkDestroyFramebuffer(device, framebuffers[index])
        index += 1
    }
    presentTransitionFramebuffers.forEach { Vulkan.vkDestroyFramebuffer(device, it) }
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
    presentTransitionFramebuffers = buildPresentTransitionFramebuffers()

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
    uiRoundedQuadRenderPipeline?.writeScreenSize(
        swapchainManager.extent.width.toFloat(),
        swapchainManager.extent.height.toFloat()
    )
}
