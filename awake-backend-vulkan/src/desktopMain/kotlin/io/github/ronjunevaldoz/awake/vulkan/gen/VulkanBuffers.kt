// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

// Phase 1b (desktop native build) has not landed yet — see docs/MVP_PLAN.md.
actual object VulkanBuffers {
    actual external fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long

    actual external fun vkDestroyBuffer(device: Long, buffer: Long)

    actual external fun vkGetBufferMemoryRequirements(device: Long, buffer: Long): VkMemoryRequirements

    actual external fun findMemoryType(physicalDevice: Long, typeFilter: Int, properties: Int): Int

    actual external fun vkAllocateMemory(device: Long, allocateInfo: VkMemoryAllocateInfo): Long

    actual external fun vkFreeMemory(device: Long, memory: Long)

    actual external fun vkBindBufferMemory(device: Long, buffer: Long, memory: Long, memoryOffset: Long)

    actual external fun writeBufferMemoryFloats(device: Long, memory: Long, offset: Long, data: FloatArray)

    actual external fun writeBufferMemoryBytes(device: Long, memory: Long, offset: Long, data: ByteArray)

    actual external fun vkCmdBindVertexBuffers(
        commandBuffer: Long,
        firstBinding: Int,
        buffers: LongArray,
        offsets: LongArray
    )

    actual external fun vkCmdBindIndexBuffer(commandBuffer: Long, buffer: Long, offset: Long, indexType: Int)

    actual external fun vkCmdCopyBuffer(commandBuffer: Long, srcBuffer: Long, dstBuffer: Long, size: Long)

    actual external fun vkCmdDrawIndexed(
        commandBuffer: Long,
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int,
        vertexOffset: Int,
        firstInstance: Int
    )

    actual external fun vkDeviceWaitIdle(device: Long)
}
