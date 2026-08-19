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
import io.github.ronjunevaldoz.awake.vulkan.pipeline.VulkanBufferBinding

/**
 * The per-instance sprite-strip frame index behind one particle-instanced draw call -- an
 * instance-rate vertex buffer bound at binding 3, alongside the mesh's own per-vertex buffer at
 * binding 0, [InstanceBuffer]'s model matrices at binding 1, and [AlphaInstanceBuffer]'s
 * color+alpha at binding 2 (see `RenderPipeline`'s `instanceFrame` parameter, which declares the
 * matching vertex input state). Same HOST_VISIBLE|HOST_COHERENT, fixed-capacity,
 * rewritten-every-frame lifecycle as [AlphaInstanceBuffer] itself, just `FLOATS_PER_INSTANCE = 1`
 * (a lone `f32`, not a `vec4f`) and its own binding.
 */
class FrameInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    private val maxInstances: Int = InstanceBuffer.DEFAULT_MAX_INSTANCES,
    framesInFlight: Int = 1,
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    private data class FrameResources(
        val buffer: BufferHandle,
        val memory: DeviceMemoryHandle,
    ) {
        /** This slot's buffer as the port's opaque handle -- built once, not per draw. */
        val binding = VulkanBufferBinding(buffer.handle)
    }

    private val frameResources: Array<FrameResources> = Array(framesInFlight) {
        val (buffer, memory) = allocateHostVisibleBuffer((maxInstances * FLOATS_PER_INSTANCE * Float.SIZE_BYTES).toLong())
        FrameResources(BufferHandle(buffer), DeviceMemoryHandle(memory))
    }

    private var packed: FloatArray = FloatArray(0)

    fun update(frameIndex: Int, frames: List<Float>) {
        require(frames.size <= maxInstances) {
            "Instance count (${frames.size}) exceeds FrameInstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (frames.isEmpty()) return
        if (packed.size != frames.size) {
            packed = FloatArray(frames.size)
        }
        var index = 0
        while (index < frames.size) {
            packed[index] = frames[index]
            index += 1
        }
        VulkanBuffers.writeBufferMemoryFloats(device, resourcesFor(frameIndex).memory.handle, 0, packed)
    }

    /** This frame slot's buffer, for the shared opaque feature to bind at binding 3. */
    fun binding(frameIndex: Int): VulkanBufferBinding = resourcesFor(frameIndex).binding

    fun bind(frameIndex: Int, commandBuffer: Long) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            INSTANCE_FRAME_BINDING,
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
            "FrameInstanceBuffer frame index $frameIndex is outside 0..${frameResources.lastIndex}."
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
        const val INSTANCE_FRAME_BINDING = 3
        const val FLOATS_PER_INSTANCE = 1
    }
}
