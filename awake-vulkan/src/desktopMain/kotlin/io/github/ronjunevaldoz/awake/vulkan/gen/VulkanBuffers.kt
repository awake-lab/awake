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
    actual fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyBuffer(device: Long, buffer: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkGetBufferMemoryRequirements(device: Long, buffer: Long): VkMemoryRequirements {
        TODO("Not yet implemented")
    }

    actual fun findMemoryType(physicalDevice: Long, typeFilter: Int, properties: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun vkAllocateMemory(device: Long, allocateInfo: VkMemoryAllocateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkFreeMemory(device: Long, memory: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkBindBufferMemory(device: Long, buffer: Long, memory: Long, memoryOffset: Long) {
        TODO("Not yet implemented")
    }

    actual fun writeBufferMemoryFloats(device: Long, memory: Long, offset: Long, data: FloatArray) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdBindVertexBuffers(
        commandBuffer: Long,
        firstBinding: Int,
        buffers: LongArray,
        offsets: LongArray
    ) {
        TODO("Not yet implemented")
    }
}
