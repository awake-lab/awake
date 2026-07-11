// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.debug

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

/**
 * A world-space `LINE_LIST` vertex buffer rewritten every frame -- same HOST_VISIBLE
 * rewrite-every-frame lifecycle as `ui.DynamicMesh`, but no index buffer at all: each
 * consecutive pair of vertices *is* one line segment under `VK_PRIMITIVE_TOPOLOGY_LINE_LIST`,
 * so there's nothing an index buffer would deduplicate.
 */
class LineMesh(
    private val graphicsDevice: GraphicsDevice,
    private val maxLines: Int
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    private val maxVertices = maxLines * VERTICES_PER_LINE
    private var vertexBuffer: BufferHandle
    private var vertexBufferMemory: DeviceMemoryHandle

    /** How many vertices this frame's [update] actually wrote -- [draw] only draws this
     * many, not the full buffer capacity. */
    var vertexCount: Int = 0
        private set

    init {
        val buffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = (maxVertices * FLOATS_PER_VERTEX * Float.SIZE_BYTES).toLong(),
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            )
        )
        val requirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, buffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val memory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = requirements.size, memoryTypeIndex = memoryTypeIndex)
        )
        VulkanBuffers.vkBindBufferMemory(device, buffer, memory, 0)
        vertexBuffer = BufferHandle(buffer)
        vertexBufferMemory = DeviceMemoryHandle(memory)
    }

    /** Overwrites this frame's vertex contents. [vertices] must be at most
     * `maxLines * VERTICES_PER_LINE * FLOATS_PER_VERTEX` floats -- callers build this from
     * `LineSegment`s (3 floats position + 4 floats color per vertex, 2 vertices per line). */
    fun update(vertices: FloatArray) {
        require(vertices.size <= maxVertices * FLOATS_PER_VERTEX) {
            "Debug line count exceeds LineMesh capacity ($maxLines lines) -- " +
                "raise maxLines or draw fewer lines this frame."
        }
        VulkanBuffers.writeBufferMemoryFloats(device, vertexBufferMemory.handle, 0, vertices)
        vertexCount = vertices.size / FLOATS_PER_VERTEX
    }

    fun bind(commandBuffer: Long) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            0,
            longArrayOf(vertexBuffer.handle),
            longArrayOf(0L)
        )
    }

    fun draw(commandBuffer: Long) {
        if (vertexCount == 0) return
        Vulkan.vkCmdDraw(commandBuffer, vertexCount, 1, 0, 0)
    }

    fun destroy() {
        VulkanBuffers.vkDestroyBuffer(device, vertexBuffer.handle)
        VulkanBuffers.vkFreeMemory(device, vertexBufferMemory.handle)
    }

    companion object {
        /** pos (vec3) + color (vec4) -- see `debug_line.vert`. */
        const val FLOATS_PER_VERTEX = 7
        const val VERTICES_PER_LINE = 2
    }
}
