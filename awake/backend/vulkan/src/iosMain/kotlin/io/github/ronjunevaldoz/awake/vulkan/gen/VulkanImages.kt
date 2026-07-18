// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import cnames.structs.VkBuffer_T
import cnames.structs.VkDeviceMemory_T
import cnames.structs.VkImage_T
import cnames.structs.VkSampler_T
import platform.MoltenVK.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT
import platform.MoltenVK.VK_ACCESS_SHADER_READ_BIT
import platform.MoltenVK.VK_ACCESS_TRANSFER_READ_BIT
import platform.MoltenVK.VK_ACCESS_TRANSFER_WRITE_BIT
import platform.MoltenVK.VK_IMAGE_ASPECT_COLOR_BIT
import platform.MoltenVK.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
import platform.MoltenVK.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
import platform.MoltenVK.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT
import platform.MoltenVK.VK_PIPELINE_STAGE_TRANSFER_BIT
import platform.MoltenVK.VK_QUEUE_FAMILY_IGNORED
import platform.MoltenVK.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER
import platform.MoltenVK.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkImageVar
import platform.MoltenVK.VkSamplerVar
import platform.MoltenVK.vkBindImageMemory as nativeVkBindImageMemory
import platform.MoltenVK.vkCmdCopyBufferToImage as nativeVkCmdCopyBufferToImage
import platform.MoltenVK.vkCmdCopyImageToBuffer as nativeVkCmdCopyImageToBuffer
import platform.MoltenVK.vkCmdPipelineBarrier as nativeVkCmdPipelineBarrier
import platform.MoltenVK.vkCreateImage as nativeVkCreateImage
import platform.MoltenVK.vkCreateSampler as nativeVkCreateSampler
import platform.MoltenVK.vkDestroyImage as nativeVkDestroyImage
import platform.MoltenVK.vkDestroySampler as nativeVkDestroySampler
import platform.MoltenVK.vkGetImageMemoryRequirements as nativeVkGetImageMemoryRequirements
import platform.MoltenVK.VkBufferImageCopy as NativeVkBufferImageCopy
import platform.MoltenVK.VkImageCreateInfo as NativeVkImageCreateInfo
import platform.MoltenVK.VkImageMemoryBarrier as NativeVkImageMemoryBarrier
import platform.MoltenVK.VkSamplerCreateInfo as NativeVkSamplerCreateInfo

// Phase 6 (MoltenVK cinterop) is in progress -- see docs/MVP_PLAN.md.
@OptIn(ExperimentalForeignApi::class)
actual object VulkanImages {
    actual fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkImageCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            imageType = createInfo.imageType.toUInt()
            format = createInfo.format.toUInt()
            extent.apply {
                width = createInfo.width.toUInt()
                height = createInfo.height.toUInt()
                depth = 1u
            }
            mipLevels = createInfo.mipLevels.toUInt()
            arrayLayers = createInfo.arrayLayers.toUInt()
            samples = createInfo.samples.toUInt()
            tiling = createInfo.tiling.toUInt()
            usage = createInfo.usage.toUInt()
            sharingMode = createInfo.sharingMode.toUInt()
            queueFamilyIndexCount = 0u
            pQueueFamilyIndices = null
            initialLayout = createInfo.initialLayout.toUInt()
        }
        val imageVar = alloc<VkImageVar>()
        val result = nativeVkCreateImage(device.toCPointer(), nativeCreateInfo.ptr, null, imageVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateImage failed: $result" }
        imageVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyImage(device: Long, image: Long) {
        nativeVkDestroyImage(device.toCPointer(), image.toCPointer<VkImage_T>(), null)
    }

    actual fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements = memScoped {
        val native = alloc<platform.MoltenVK.VkMemoryRequirements>()
        nativeVkGetImageMemoryRequirements(device.toCPointer(), image.toCPointer<VkImage_T>(), native.ptr)
        VkMemoryRequirements(
            size = native.size.toLong(),
            alignment = native.alignment.toLong(),
            memoryTypeBits = native.memoryTypeBits.toInt()
        )
    }

    actual fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long) {
        val result = nativeVkBindImageMemory(
            device.toCPointer(),
            image.toCPointer<VkImage_T>(),
            memory.toCPointer<VkDeviceMemory_T>(),
            memoryOffset.toULong()
        )
        check(result == VK_SUCCESS) { "vkBindImageMemory failed: $result" }
    }

    actual fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkSamplerCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO
            pNext = null
            flags = 0u
            magFilter = createInfo.magFilter.toUInt()
            minFilter = createInfo.minFilter.toUInt()
            mipmapMode = createInfo.mipmapMode.toUInt()
            addressModeU = createInfo.addressModeU.toUInt()
            addressModeV = createInfo.addressModeV.toUInt()
            addressModeW = createInfo.addressModeW.toUInt()
            mipLodBias = 0f
            anisotropyEnable = if (createInfo.anisotropyEnable) 1u else 0u
            maxAnisotropy = createInfo.maxAnisotropy
            compareEnable = 0u
            compareOp = 0u
            minLod = createInfo.minLod
            maxLod = createInfo.maxLod
            borderColor = createInfo.borderColor.toUInt()
            unnormalizedCoordinates = if (createInfo.unnormalizedCoordinates) 1u else 0u
        }
        val samplerVar = alloc<VkSamplerVar>()
        val result = nativeVkCreateSampler(device.toCPointer(), nativeCreateInfo.ptr, null, samplerVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateSampler failed: $result" }
        samplerVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroySampler(device: Long, sampler: Long) {
        nativeVkDestroySampler(device.toCPointer(), sampler.toCPointer<VkSampler_T>(), null)
    }

    // Only the two transitions a texture upload needs -- see this actual object's expect
    // declaration doc comment for why this is deliberately not a generic barrier API.
    actual fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int,
        levelCount: Int
    ) = memScoped {
        val barrier = alloc<NativeVkImageMemoryBarrier>().apply {
            sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER
            pNext = null
            this.oldLayout = oldLayout.toUInt()
            this.newLayout = newLayout.toUInt()
            srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED
            dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED
            this.image = image.toCPointer()
            subresourceRange.apply {
                aspectMask = VK_IMAGE_ASPECT_COLOR_BIT.toUInt()
                baseMipLevel = 0u
                // Explicit level count, not VK_REMAINING_MIP_LEVELS -- see the JNI/desktop
                // actual's matching comment: that sentinel silently transitioned only level 0
                // on MoltenVK, confirmed via validation errors on every level above 0.
                this.levelCount = levelCount.toUInt()
                baseArrayLayer = 0u
                layerCount = 1u
            }
        }
        val srcStage: UInt
        val dstStage: UInt
        if (oldLayout == VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED &&
            newLayout == VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
        ) {
            barrier.srcAccessMask = 0u
            barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT.toUInt()
            srcStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT.toUInt()
            dstStage = VK_PIPELINE_STAGE_TRANSFER_BIT.toUInt()
        } else if (oldLayout == VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL &&
            newLayout == VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
        ) {
            barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT.toUInt()
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT.toUInt()
            srcStage = VK_PIPELINE_STAGE_TRANSFER_BIT.toUInt()
            dstStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT.toUInt()
        } else if (oldLayout == VkImageLayout2.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL &&
            newLayout == VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
        ) {
            // Offscreen render-target post-render transition (Renderer.renderToTexture).
            barrier.srcAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.toUInt()
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT.toUInt()
            srcStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.toUInt()
            dstStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT.toUInt()
        } else if (oldLayout == VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL &&
            newLayout == VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL
        ) {
            // Offscreen render-target readback (Renderer.readPixels): temporarily leave the
            // sampleable resting state to let vkCmdCopyImageToBuffer read the image.
            barrier.srcAccessMask = VK_ACCESS_SHADER_READ_BIT.toUInt()
            barrier.dstAccessMask = VK_ACCESS_TRANSFER_READ_BIT.toUInt()
            srcStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT.toUInt()
            dstStage = VK_PIPELINE_STAGE_TRANSFER_BIT.toUInt()
        } else if (oldLayout == VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL &&
            newLayout == VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
        ) {
            // Restores the sampleable resting state after readPixels' copy completes.
            barrier.srcAccessMask = VK_ACCESS_TRANSFER_READ_BIT.toUInt()
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT.toUInt()
            srcStage = VK_PIPELINE_STAGE_TRANSFER_BIT.toUInt()
            dstStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT.toUInt()
        } else {
            error("vkTransitionImageLayout: unsupported layout transition ($oldLayout -> $newLayout)")
        }
        nativeVkCmdPipelineBarrier(
            commandBuffer.toCPointer(),
            srcStage,
            dstStage,
            0u,
            0u,
            null,
            0u,
            null,
            1u,
            barrier.ptr
        )
    }

    actual fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy
    ) = memScoped {
        val region = alloc<NativeVkBufferImageCopy>().apply {
            bufferOffset = copy.bufferOffset.toULong()
            bufferRowLength = copy.bufferRowLength.toUInt()
            bufferImageHeight = copy.bufferImageHeight.toUInt()
            imageSubresource.apply {
                aspectMask = VK_IMAGE_ASPECT_COLOR_BIT.toUInt()
                mipLevel = copy.mipLevel.toUInt()
                baseArrayLayer = copy.baseArrayLayer.toUInt()
                layerCount = copy.layerCount.toUInt()
            }
            imageOffset.apply {
                x = 0
                y = 0
                z = 0
            }
            imageExtent.apply {
                width = copy.imageWidth.toUInt()
                height = copy.imageHeight.toUInt()
                depth = 1u
            }
        }
        nativeVkCmdCopyBufferToImage(
            commandBuffer.toCPointer(),
            srcBuffer.toCPointer<VkBuffer_T>(),
            dstImage.toCPointer<VkImage_T>(),
            VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL.toUInt(),
            1u,
            region.ptr
        )
    }

    /** Inverse of [vkCmdCopyBufferToImage] -- for offscreen render-target CPU readback
     * (`Renderer.readPixels`). [srcImage] is expected in `TRANSFER_SRC_OPTIMAL` layout (the
     * caller transitions it there before this call). */
    actual fun vkCmdCopyImageToBuffer(
        commandBuffer: Long,
        srcImage: Long,
        dstBuffer: Long,
        copy: VkBufferImageCopy
    ): Unit = memScoped {
        val region = alloc<NativeVkBufferImageCopy>().apply {
            bufferOffset = copy.bufferOffset.toULong()
            bufferRowLength = copy.bufferRowLength.toUInt()
            bufferImageHeight = copy.bufferImageHeight.toUInt()
            imageSubresource.apply {
                aspectMask = VK_IMAGE_ASPECT_COLOR_BIT.toUInt()
                mipLevel = copy.mipLevel.toUInt()
                baseArrayLayer = copy.baseArrayLayer.toUInt()
                layerCount = copy.layerCount.toUInt()
            }
            imageOffset.apply {
                x = 0
                y = 0
                z = 0
            }
            imageExtent.apply {
                width = copy.imageWidth.toUInt()
                height = copy.imageHeight.toUInt()
                depth = 1u
            }
        }
        nativeVkCmdCopyImageToBuffer(
            commandBuffer.toCPointer(),
            srcImage.toCPointer<VkImage_T>(),
            VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL.toUInt(),
            dstBuffer.toCPointer<VkBuffer_T>(),
            1u,
            region.ptr
        )
    }
}
