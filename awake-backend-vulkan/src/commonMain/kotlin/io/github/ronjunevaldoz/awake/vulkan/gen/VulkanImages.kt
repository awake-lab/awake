// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

/**
 * Phase 1d image/sampler API surface, same jni-binding-generator `.gen` package/pipeline as
 * [VulkanBuffers]/[VulkanDescriptors]. `vkTransitionImageLayout` is deliberately narrow
 * (only the two transitions a texture upload actually needs: UNDEFINED -> TRANSFER_DST,
 * then TRANSFER_DST -> SHADER_READ_ONLY) rather than exposing a fully generic
 * `VkImageMemoryBarrier` -- the same simplification vulkan-tutorial.com's own reference
 * implementation uses, since the correct `srcAccessMask`/`dstAccessMask`/pipeline-stage
 * combination for every possible layout pair is a large lookup table this MVP doesn't need
 * yet. Generalize if/when a transition outside these two is actually needed.
 */
expect object VulkanImages {
    fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long
    fun vkDestroyImage(device: Long, image: Long)
    fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements
    fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long)
    fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long
    fun vkDestroySampler(device: Long, sampler: Long)

    /** `oldLayout`/`newLayout` use the plain-`Int` [io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2] values. */
    fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int
    )

    fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy
    )
}
