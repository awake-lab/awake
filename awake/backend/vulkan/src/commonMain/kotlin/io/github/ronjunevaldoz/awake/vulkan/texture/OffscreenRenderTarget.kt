// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

/**
 * An offscreen color+depth render destination (`Renderer.createRenderTarget`) -- a
 * [io.github.ronjunevaldoz.awake.render.texture.RenderTarget] implementation. The color
 * image is created with [colorFormat] equal to the swapchain's own image format (NOT
 * [Texture]'s fixed `R8G8B8A8_UNORM`) specifically so [renderPass] -- the SAME
 * `renderPipeline.renderPass`/pipeline the main swapchain pass already uses -- can be reused
 * unmodified: Vulkan requires a framebuffer's attachment image views to have the EXACT
 * format the render pass they're used with declares (not just a "compatible" one), so a
 * different color format here would force building an entire second render pass + graphics
 * pipeline (a real, but avoidable, amount of extra work for this feature). Whatever color
 * space interpretation the swapchain format implies (e.g. sRGB) applies identically when
 * this image is later sampled by a compositing [io.github.ronjunevaldoz.awake.vulkan.material.Material]
 * -- no different code path is needed either way, a descriptor binding doesn't care what
 * format the bound image was created with.
 *
 * Resting layout is `SHADER_READ_ONLY_OPTIMAL` (set once, right after the render pass ends,
 * by [transitionToShaderReadOnly]) -- both compositing (sampling) and readback (a temporary
 * detour through `TRANSFER_SRC_OPTIMAL` and back, see `Renderer.readPixels`) start from
 * there.
 */
class OffscreenRenderTarget(
    graphicsDevice: GraphicsDevice,
    private val renderPass: Long,
    override val width: Int,
    override val height: Int,
    colorFormat: Int,
) : RenderTarget {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    var colorImage: Long = 0
        private set
    var colorImageMemory: Long = 0
        private set
    var colorImageView: Long = 0
        private set
    var sampler: Long = 0
        private set

    private var depthImage: Long = 0
    private var depthImageMemory: Long = 0
    private var depthImageView: Long = 0

    var framebuffer: Long = 0
        private set

    init {
        colorImage = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = width,
                height = height,
                format = colorFormat,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or
                    VkImageUsageFlagBits2.VK_IMAGE_USAGE_SAMPLED_BIT or
                    VkImageUsageFlagBits2.VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
            ),
        )
        val colorRequirements = VulkanImages.vkGetImageMemoryRequirements(device, colorImage)
        val colorMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            colorRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
        )
        colorImageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = colorRequirements.size, memoryTypeIndex = colorMemoryTypeIndex),
        )
        VulkanImages.vkBindImageMemory(device, colorImage, colorImageMemory, 0)
        colorImageView = Vulkan.vkCreateImageView(
            device,
            VkImageViewCreateInfo(
                image = colorImage,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = VkFormat.entries.first { it.value == colorFormat },
                subresourceRange = VkImageSubresourceRange(
                    aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT.value,
                    baseMipLevel = 0,
                    levelCount = 1,
                    baseArrayLayer = 0,
                    layerCount = 1,
                ),
            ),
        )
        sampler = VulkanImages.vkCreateSampler(device, VkSamplerCreateInfo())

        depthImage = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = width,
                height = height,
                format = VkFormat.VK_FORMAT_D32_SFLOAT.value,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
            ),
        )
        val depthRequirements = VulkanImages.vkGetImageMemoryRequirements(device, depthImage)
        val depthMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            depthRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
        )
        depthImageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = depthRequirements.size, memoryTypeIndex = depthMemoryTypeIndex),
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
                    layerCount = 1,
                ),
            ),
        )

        framebuffer = Vulkan.vkCreateFramebuffer(
            device,
            VkFramebufferCreateInfo(
                renderPass = renderPass,
                pAttachments = arrayOf(colorImageView, depthImageView),
                width = width,
                height = height,
                layers = 1,
            ),
        )
    }

    /** Called once, right after the render pass that wrote [colorImage] ends -- transitions
     * `COLOR_ATTACHMENT_OPTIMAL` -> `SHADER_READ_ONLY_OPTIMAL`, this target's resting layout
     * (see this class's doc comment). Must run inside the same one-time command buffer
     * `Renderer.renderToTexture` already opened for the render pass itself. */
    fun transitionToShaderReadOnly(commandBuffer: Long) {
        VulkanImages.vkTransitionImageLayout(
            commandBuffer,
            colorImage,
            VkImageLayout2.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        )
    }

    override fun destroy() {
        Vulkan.vkDestroyFramebuffer(device, framebuffer)
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
        VulkanImages.vkDestroySampler(device, sampler)
        Vulkan.vkDestroyImageView(device, colorImageView)
        VulkanImages.vkDestroyImage(device, colorImage)
        VulkanBuffers.vkFreeMemory(device, colorImageMemory)
    }
}
