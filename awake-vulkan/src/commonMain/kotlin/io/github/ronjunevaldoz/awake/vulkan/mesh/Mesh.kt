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

package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkIndexType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

/**
 * Phase 2 (renderer abstraction): owns a single mesh's vertex/index buffer upload -- the
 * standard Vulkan staging-buffer pattern extracted verbatim from `VulkanApplication`'s
 * `createDeviceLocalBuffer`/`createVertexBuffer`/`createIndexBuffer` functions and their
 * backing fields. A HOST_VISIBLE staging buffer is written and copied into a real
 * DEVICE_LOCAL buffer via a one-time command buffer, same pattern already proven for
 * texture upload.
 *
 * [runOneTimeCommands] is injected rather than owned here: it needs a command pool and
 * graphics queue, neither of which is extracted into a dedicated class yet (they're still
 * pipeline/render-pass-adjacent state on `VulkanApplication`) -- passing the mechanism in
 * avoids taking on that scope here.
 *
 * Only owns vertex+index buffer bind/draw, not descriptor-set binding: that's a
 * Material/Shader concern (interleaved between [bind] and [draw] by the caller), not part
 * of what a mesh is.
 */
class Mesh(
    private val graphicsDevice: GraphicsDevice,
    private val runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    vertices: FloatArray,
    indices: IntArray
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    var vertexBuffer: Long = 0
        private set
    var vertexBufferMemory: Long = 0
        private set
    var indexBuffer: Long = 0
        private set
    var indexBufferMemory: Long = 0
        private set
    val indexCount: Int = indices.size

    init {
        val vertexBufferSize = (vertices.size * Float.SIZE_BYTES).toLong()
        val (vBuffer, vMemory) = createDeviceLocalBuffer(
            byteSize = vertexBufferSize,
            usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            write = { stagingMemory ->
                VulkanBuffers.writeBufferMemoryFloats(device, stagingMemory, 0, vertices)
            }
        )
        vertexBuffer = vBuffer
        vertexBufferMemory = vMemory

        val indexBytes = indices.toByteArrayLE()
        val indexBufferSize = indexBytes.size.toLong()
        val (iBuffer, iMemory) = createDeviceLocalBuffer(
            byteSize = indexBufferSize,
            usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
            write = { stagingMemory ->
                VulkanBuffers.writeBufferMemoryBytes(device, stagingMemory, 0, indexBytes)
            }
        )
        indexBuffer = iBuffer
        indexBufferMemory = iMemory
    }

    /** Allocates a HOST_VISIBLE staging buffer, writes into it via [write], copies it into a
     * new DEVICE_LOCAL buffer (usage = [usage] or TRANSFER_DST) using a one-time command
     * buffer + `vkCmdCopyBuffer`, then frees the staging buffer. */
    private fun createDeviceLocalBuffer(
        byteSize: Long,
        usage: Int,
        write: (memory: Long) -> Unit
    ): Pair<Long, Long> {
        val stagingBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            )
        )
        val stagingRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, stagingBuffer)
        val stagingMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            stagingRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val stagingMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = stagingRequirements.size,
                memoryTypeIndex = stagingMemoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, stagingBuffer, stagingMemory, 0)
        write(stagingMemory)

        val destBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
                usage = usage or VkBufferUsageFlagBits.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
            )
        )
        val destRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, destBuffer)
        val destMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            destRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        val destMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = destRequirements.size,
                memoryTypeIndex = destMemoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, destBuffer, destMemory, 0)

        runOneTimeCommands { commandBuffer ->
            VulkanBuffers.vkCmdCopyBuffer(commandBuffer, stagingBuffer, destBuffer, byteSize)
        }

        VulkanBuffers.vkDestroyBuffer(device, stagingBuffer)
        VulkanBuffers.vkFreeMemory(device, stagingMemory)

        return destBuffer to destMemory
    }

    private fun IntArray.toByteArrayLE(): ByteArray {
        val out = ByteArray(size * 4)
        for (i in indices) {
            val v = this[i]
            out[i * 4] = (v and 0xFF).toByte()
            out[i * 4 + 1] = ((v shr 8) and 0xFF).toByte()
            out[i * 4 + 2] = ((v shr 16) and 0xFF).toByte()
            out[i * 4 + 3] = ((v shr 24) and 0xFF).toByte()
        }
        return out
    }

    /** Binds this mesh's vertex + index buffers. Descriptor-set binding (a Material/Shader
     * concern) happens in between this and [draw], not here. */
    fun bind(commandBuffer: Long) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            0,
            longArrayOf(vertexBuffer),
            longArrayOf(0L)
        )
        VulkanBuffers.vkCmdBindIndexBuffer(
            commandBuffer,
            indexBuffer,
            0,
            VkIndexType.VK_INDEX_TYPE_UINT32
        )
    }

    fun draw(commandBuffer: Long) {
        VulkanBuffers.vkCmdDrawIndexed(commandBuffer, indexCount, 1, 0, 0, 0)
    }

    fun destroy() {
        VulkanBuffers.vkDestroyBuffer(device, vertexBuffer)
        VulkanBuffers.vkFreeMemory(device, vertexBufferMemory)
        VulkanBuffers.vkDestroyBuffer(device, indexBuffer)
        VulkanBuffers.vkFreeMemory(device, indexBufferMemory)
    }
}
