// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

/**
 * Phase 1d image/sampler API surface, same jni-binding-generator `.gen` package/pipeline as
 * [VulkanBuffers]/[VulkanDescriptors]. `vkTransitionImageLayout` is deliberately narrow --
 * only the specific transitions actual callers need (texture upload's UNDEFINED ->
 * TRANSFER_DST -> SHADER_READ_ONLY, plus offscreen render-target readback/compositing's
 * COLOR_ATTACHMENT_OPTIMAL <-> SHADER_READ_ONLY_OPTIMAL <-> TRANSFER_SRC_OPTIMAL round trip)
 * rather than exposing a fully generic `VkImageMemoryBarrier` -- the same simplification
 * vulkan-tutorial.com's own reference implementation uses, since the correct
 * `srcAccessMask`/`dstAccessMask`/pipeline-stage combination for every possible layout pair
 * is a large lookup table this MVP doesn't need yet. Generalize further if/when a
 * transition outside these five is actually needed.
 */
expect object VulkanImages {
    fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long
    fun vkDestroyImage(device: Long, image: Long)
    fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements
    fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long)
    fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long
    fun vkDestroySampler(device: Long, sampler: Long)

    /** Transitions `image`'s layout in `commandBuffer` between the plain-`Int`
     * [io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2] values `oldLayout`/`newLayout`.
     * `levelCount` defaults to `1` (every pre-existing caller is single-mip); a multi-mip
     * [io.github.ronjunevaldoz.awake.vulkan.texture.Texture] must pass its real level count --
     * `VK_REMAINING_MIP_LEVELS` silently transitions only level 0 on MoltenVK. */
    fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int,
        levelCount: Int = 1,
    )

    fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy,
    )

    /** Inverse of [vkCmdCopyBufferToImage] -- records into `commandBuffer` a copy of `srcImage`
     * (expected in `TRANSFER_SRC_OPTIMAL` layout) into `dstBuffer` per `copy`'s region, for
     * offscreen render-target CPU readback (`Renderer.readPixels`). */
    fun vkCmdCopyImageToBuffer(
        commandBuffer: Long,
        srcImage: Long,
        dstBuffer: Long,
        copy: VkBufferImageCopy,
    )
}
