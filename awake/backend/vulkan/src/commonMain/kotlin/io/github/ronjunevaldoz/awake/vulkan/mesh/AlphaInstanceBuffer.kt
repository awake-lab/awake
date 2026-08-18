// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

/**
 * The per-instance alpha values behind one particle-instanced draw call -- an instance-rate
 * vertex buffer bound at binding 2, alongside the mesh's own per-vertex buffer at binding 0
 * and [InstanceBuffer]'s model matrices at binding 1 (see `RenderPipeline`'s `instanceAlpha`
 * parameter, which declares the matching vertex input state). Same
 * HOST_VISIBLE|HOST_COHERENT, fixed-capacity, rewritten-every-frame lifecycle as
 * [InstanceBuffer] itself -- this is that same shape with `FLOATS_PER_INSTANCE = 1` and its
 * own binding, not a generalization of it, since widening [InstanceBuffer]'s own stride would
 * ripple into every OTHER instanced format that doesn't use per-instance alpha at all.
 */
class AlphaInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    /** Hard ceiling on instances per draw call -- see [InstanceBuffer]'s own doc comment for
     * why this fails loudly rather than silently truncating. */
    private val maxInstances: Int = InstanceBuffer.DEFAULT_MAX_INSTANCES,
    framesInFlight: Int = 1,
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    private data class FrameResources(
        val buffer: BufferHandle,
        val memory: DeviceMemoryHandle,
    )

    private val frameResources: Array<FrameResources> = Array(framesInFlight) {
        val (buffer, memory) = allocateHostVisibleBuffer((maxInstances * Float.SIZE_BYTES).toLong())
        FrameResources(BufferHandle(buffer), DeviceMemoryHandle(memory))
    }

    // Reused across frames so a steady instance count allocates nothing per frame -- same
    // "resize only when the count actually changes" shape InstanceBuffer's own `packed` uses.
    private var packed: FloatArray = FloatArray(0)

    fun update(frameIndex: Int, alphas: List<Float>) {
        require(alphas.size <= maxInstances) {
            "Instance count (${alphas.size}) exceeds AlphaInstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (alphas.isEmpty()) return
        if (packed.size != alphas.size) {
            packed = FloatArray(alphas.size)
        }
        var index = 0
        while (index < alphas.size) {
            packed[index] = alphas[index]
            index += 1
        }
        VulkanBuffers.writeBufferMemoryFloats(device, resourcesFor(frameIndex).memory.handle, 0, packed)
    }

    fun bind(frameIndex: Int, commandBuffer: Long) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            INSTANCE_ALPHA_BINDING,
            longArrayOf(resourcesFor(frameIndex).buffer.handle),
            longArrayOf(0L),
        )
    }

    fun destroy() {
        frameResources.forEach { frame ->
            VulkanBuffers.vkDestroyBuffer(device, frame.buffer.handle)
            VulkanBuffers.vkFreeMemory(device, frame.memory.handle)
        }
    }

    private fun resourcesFor(frameIndex: Int): FrameResources {
        require(frameIndex in frameResources.indices) {
            "AlphaInstanceBuffer frame index $frameIndex is outside 0..${frameResources.lastIndex}."
        }
        return frameResources[frameIndex]
    }

    private fun allocateHostVisibleBuffer(byteSize: Long): Pair<Long, Long> {
        val buffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            ),
        )
        val requirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, buffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
        )
        val memory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = requirements.size, memoryTypeIndex = memoryTypeIndex),
        )
        VulkanBuffers.vkBindBufferMemory(device, buffer, memory, 0)
        return buffer to memory
    }

    private companion object {
        const val INSTANCE_ALPHA_BINDING = 2
    }
}
