/*
 * Awake
 * Awake.awake-vulkan.wasmJsMain
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

// Phase 2.5 (Web/WebGPU, decision D7) scaffolding-only stub -- see
// io.github.ronjunevaldoz.awake.vulkan.Vulkan.kt's header comment in this same source set
// for the full rationale.
actual object VulkanBuffers {
    actual fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyBuffer(device: Long, buffer: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetBufferMemoryRequirements(device: Long, buffer: Long): VkMemoryRequirements =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun findMemoryType(physicalDevice: Long, typeFilter: Int, properties: Int): Int =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkAllocateMemory(device: Long, allocateInfo: VkMemoryAllocateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkFreeMemory(device: Long, memory: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkBindBufferMemory(device: Long, buffer: Long, memory: Long, memoryOffset: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun writeBufferMemoryFloats(device: Long, memory: Long, offset: Long, data: FloatArray): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun writeBufferMemoryBytes(device: Long, memory: Long, offset: Long, data: ByteArray): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdBindVertexBuffers(
        commandBuffer: Long,
        firstBinding: Int,
        buffers: LongArray,
        offsets: LongArray
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdBindIndexBuffer(commandBuffer: Long, buffer: Long, offset: Long, indexType: Int): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdCopyBuffer(commandBuffer: Long, srcBuffer: Long, dstBuffer: Long, size: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdDrawIndexed(
        commandBuffer: Long,
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int,
        vertexOffset: Int,
        firstInstance: Int
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDeviceWaitIdle(device: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
}
