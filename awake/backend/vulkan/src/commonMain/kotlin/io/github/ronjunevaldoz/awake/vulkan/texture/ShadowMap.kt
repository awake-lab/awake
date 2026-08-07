// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.vulkan.VK_SUBPASS_EXTERNAL
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
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
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFilter
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerAddressMode
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubpassDescription

/**
 * A single depth-only render target for the directional shadow pass -- [Renderer]'s own
 * depth-pre-pass equivalent of [OffscreenRenderTarget], but with no color attachment (a
 * shadow map only ever needs depth) and a dedicated render pass whose `finalLayout` is
 * already `SHADER_READ_ONLY_OPTIMAL`, so no separate transition call is needed the way
 * [OffscreenRenderTarget.transitionToShaderReadOnly] is for its color image.
 *
 * Sampled as a plain (non-comparison) texture, not via a hardware shadow-comparison sampler:
 * this repo's `VkSamplerCreateInfo` JNI binding has no `compareEnable`/`compareOp` fields
 * today (extending the native binding generator is out of scope here), so `lit_shadow.wgsl`
 * does the depth comparison manually in the fragment shader instead. [sampler] is therefore
 * `NEAREST`-filtered -- linear-filtering raw (un-compared) depth values before comparing them
 * would blend depths across a shadow edge, which is meaningless.
 */
class ShadowMap(
    graphicsDevice: GraphicsDevice,
    val size: Int = DEFAULT_SIZE
) {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device

    var renderPass: Long = 0
        private set
    var image: Long = 0
        private set
    private var imageMemory: Long = 0
    var imageView: Long = 0
        private set
    var sampler: Long = 0
        private set
    var framebuffer: Long = 0
        private set

    init {
        renderPass = createRenderPass()
        image = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = size,
                height = size,
                format = DEPTH_FORMAT.value,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT or
                    VkImageUsageFlagBits2.VK_IMAGE_USAGE_SAMPLED_BIT
            )
        )
        val requirements = VulkanImages.vkGetImageMemoryRequirements(device, image)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            graphicsDevice.physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        imageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = requirements.size, memoryTypeIndex = memoryTypeIndex)
        )
        VulkanImages.vkBindImageMemory(device, image, imageMemory, 0)
        imageView = Vulkan.vkCreateImageView(
            device,
            VkImageViewCreateInfo(
                image = image,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = DEPTH_FORMAT,
                subresourceRange = VkImageSubresourceRange(
                    aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_DEPTH_BIT.value,
                    baseMipLevel = 0,
                    levelCount = 1,
                    baseArrayLayer = 0,
                    layerCount = 1
                )
            )
        )
        sampler = VulkanImages.vkCreateSampler(
            device,
            VkSamplerCreateInfo(
                magFilter = VkFilter.VK_FILTER_NEAREST,
                minFilter = VkFilter.VK_FILTER_NEAREST,
                addressModeU = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
                addressModeV = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
                addressModeW = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE
            )
        )
        framebuffer = Vulkan.vkCreateFramebuffer(
            device,
            VkFramebufferCreateInfo(
                renderPass = renderPass,
                pAttachments = arrayOf(imageView),
                width = size,
                height = size,
                layers = 1
            )
        )
    }

    private fun createRenderPass(): Long = Vulkan.vkCreateRenderPass(
        device,
        VkRenderPassCreateInfo(
            pAttachments = arrayOf(
                VkAttachmentDescription(
                    format = DEPTH_FORMAT,
                    storeOp = VkAttachmentStoreOp.STORE,
                    initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                    // Vulkan performs the DEPTH_STENCIL_ATTACHMENT_OPTIMAL -> SHADER_READ_ONLY
                    // transition implicitly at render-pass end, so the main pass can sample
                    // this image the instant this pass's fence is signaled -- no manual
                    // barrier call needed (unlike OffscreenRenderTarget's color image).
                    finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
                )
            ),
            pSubpasses = arrayOf(
                VkSubpassDescription(
                    pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pDepthStencilAttachment = arrayOf(
                        VkAttachmentReference(
                            attachment = 0,
                            layout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                        )
                    )
                )
            ),
            pDependencies = arrayOf(
                VkSubpassDependency(
                    srcSubpass = VK_SUBPASS_EXTERNAL,
                    dstSubpass = 0,
                    srcStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value or
                        VkPipelineStageFlagBits.VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT.value,
                    srcAccessMask = 0,
                    dstStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value or
                        VkPipelineStageFlagBits.VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT.value,
                    dstAccessMask = VkAccessFlagBits.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT.value
                )
            )
        )
    )

    fun destroy() {
        Vulkan.vkDestroyFramebuffer(device, framebuffer)
        VulkanImages.vkDestroySampler(device, sampler)
        Vulkan.vkDestroyImageView(device, imageView)
        VulkanImages.vkDestroyImage(device, image)
        VulkanBuffers.vkFreeMemory(device, imageMemory)
        Vulkan.vkDestroyRenderPass(device, renderPass)
    }

    companion object {
        /** 2048x2048 -- enough resolution for this demo's grid-sized scene without being a
         * real memory/bandwidth cost (a single D32 depth image, ~16MB). */
        const val DEFAULT_SIZE = 2048
        val DEPTH_FORMAT = VkFormat.VK_FORMAT_D32_SFLOAT
    }
}
