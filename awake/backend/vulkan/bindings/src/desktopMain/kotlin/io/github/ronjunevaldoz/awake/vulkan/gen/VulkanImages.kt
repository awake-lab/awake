// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

// Phase 1b (desktop native build) has not landed yet — see docs/MVP_PLAN.md.
actual object VulkanImages {
    actual external fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long

    actual external fun vkDestroyImage(device: Long, image: Long)

    actual external fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements

    actual external fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long)

    actual external fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long

    actual external fun vkDestroySampler(device: Long, sampler: Long)

    actual external fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int,
        levelCount: Int,
    )

    actual external fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy,
    )

    actual external fun vkCmdCopyImageToBuffer(
        commandBuffer: Long,
        srcImage: Long,
        dstBuffer: Long,
        copy: VkBufferImageCopy,
    )
}
