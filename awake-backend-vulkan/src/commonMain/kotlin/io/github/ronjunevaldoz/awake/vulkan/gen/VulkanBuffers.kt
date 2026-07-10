/*
 * Awake
 * Awake.awake-vulkan.commonMain
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

/**
 * Phase 1d Vulkan API surface generated via jni-binding-generator (vendored in
 * tools/jni-binding-generator), not the legacy awake-vulkan-generator that backs
 * io.github.ronjunevaldoz.awake.vulkan.Vulkan. Kept in a separate package/object
 * deliberately: the generateJniBindings Gradle task uses `--package-filter` scoped to
 * this package, so it never touches the legacy Vulkan.kt (which has several return/param
 * shapes jni-binding-generator doesn't support as *function-level* types yet, e.g.
 * `Array<VkLayerProperties>` as a return type — only as a struct *field*, which is what
 * Phase 1a's D10 fix actually added). See docs/decisions/D10-codegen-derisk-findings.md.
 *
 * `vkMapMemory`'s natural signature returns a raw pointer/`java.nio.ByteBuffer`, but
 * `java.nio` doesn't exist on Kotlin/Native — it can't be a commonMain `expect` type across
 * every platform. Instead of hand-writing a whole separate map/unmap object,
 * `writeBufferMemoryFloats` does map→memcpy→unmap as one call taking a plain `FloatArray`
 * (universal across every KMP target, and already in jni-binding-generator's supported-type
 * table) — a safer API besides (no manual unmap-forgetting lifecycle bug possible) and one
 * that still fits the auto-generated model with a hand-written native body, exactly like
 * `vkCreateBuffer`.
 */
expect object VulkanBuffers {
    fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long
    fun vkDestroyBuffer(device: Long, buffer: Long)
    fun vkGetBufferMemoryRequirements(device: Long, buffer: Long): VkMemoryRequirements
    fun findMemoryType(physicalDevice: Long, typeFilter: Int, properties: Int): Int
    fun vkAllocateMemory(device: Long, allocateInfo: VkMemoryAllocateInfo): Long
    fun vkFreeMemory(device: Long, memory: Long)
    fun vkBindBufferMemory(device: Long, buffer: Long, memory: Long, memoryOffset: Long)
    fun writeBufferMemoryFloats(device: Long, memory: Long, offset: Long, data: FloatArray)

    /** Same map->memcpy->unmap pattern as [writeBufferMemoryFloats], for raw byte data
     * (e.g. texture pixels) instead of float uniform/vertex data. */
    fun writeBufferMemoryBytes(device: Long, memory: Long, offset: Long, data: ByteArray)

    /** `bindingCount` is implicit (`buffers.size`); `offsets` must be the same size. */
    fun vkCmdBindVertexBuffers(
        commandBuffer: Long,
        firstBinding: Int,
        buffers: LongArray,
        offsets: LongArray
    )

    /** `indexType` uses the plain-`Int` [io.github.ronjunevaldoz.awake.vulkan.models.info.VkIndexType] values. */
    fun vkCmdBindIndexBuffer(commandBuffer: Long, buffer: Long, offset: Long, indexType: Int)

    /** Single-region copy (`srcOffset`/`dstOffset` both 0) -- the staging-buffer upload
     * pattern (a HOST_VISIBLE staging buffer written via [writeBufferMemoryFloats]/
     * [writeBufferMemoryBytes], then copied into a DEVICE_LOCAL destination buffer) never
     * needs more than one region, same simplification as [VulkanImages.vkTransitionImageLayout]. */
    fun vkCmdCopyBuffer(commandBuffer: Long, srcBuffer: Long, dstBuffer: Long, size: Long)

    fun vkCmdDrawIndexed(
        commandBuffer: Long,
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int,
        vertexOffset: Int,
        firstInstance: Int
    )

    /** Blocks until all queues on [device] are idle. Used to fully serialize frames so a
     * single (not per-frame-in-flight) uniform buffer can be safely rewritten every frame
     * without racing the GPU's read of the previous frame -- a deliberate simplification;
     * see the MVP-matrix uniform buffer usage in the demo for the full rationale. */
    fun vkDeviceWaitIdle(device: Long)
}
