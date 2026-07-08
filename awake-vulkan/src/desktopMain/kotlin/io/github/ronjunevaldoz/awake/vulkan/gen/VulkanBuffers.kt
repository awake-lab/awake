/*
 * Awake
 * Awake.awake-vulkan.desktopMain
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
