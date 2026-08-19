// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.core.math.Mat4
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
 * The per-instance model matrices behind one instanced draw call -- an instance-rate vertex
 * buffer bound at binding 1, alongside the mesh's own per-vertex buffer at binding 0 (see
 * `RenderPipeline`'s `instanced` parameter, which declares the matching vertex input state).
 *
 * Same HOST_VISIBLE|HOST_COHERENT, fixed-capacity, rewritten-every-frame lifecycle as
 * [io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh] (whose `allocateHostVisibleBuffer` this
 * mirrors) rather than `Mesh`'s DEVICE_LOCAL one-time staging copy: the transform list changes
 * per frame, so there is nothing to upload once. Simpler than `DynamicMesh` though -- one
 * vertex buffer, no index buffer, no quad vertex layout.
 */
class InstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    /** Hard ceiling on instances per draw call. Raise it here (or per call site) rather than
     * silently truncating -- [update] fails loudly with a message naming this knob. */
    private val maxInstances: Int = DEFAULT_MAX_INSTANCES,
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
        val (buffer, memory) = allocateHostVisibleBuffer(
            (maxInstances * FLOATS_PER_INSTANCE * Float.SIZE_BYTES).toLong(),
        )
        FrameResources(BufferHandle(buffer), DeviceMemoryHandle(memory))
    }

    // Reused across frames so a steady instance count allocates nothing per frame; reallocated
    // only when the count actually changes (writeBufferMemoryFloats writes the whole array, so
    // it has to be exactly instance-count sized rather than capacity sized).
    private var packed: FloatArray = FloatArray(0)

    /** Packs [models] into this frame slot's buffer. `Mat4.data` is already column-major, which
     * is the order `instanced.wgsl`'s 4 `vec4` attributes reassemble into a `mat4x4` -- so this
     * is a straight copy, no transpose. */
    fun update(frameIndex: Int, models: List<Mat4>) {
        require(models.size <= maxInstances) {
            "Instance count (${models.size}) exceeds InstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (models.isEmpty()) return
        if (packed.size != models.size * FLOATS_PER_INSTANCE) {
            packed = FloatArray(models.size * FLOATS_PER_INSTANCE)
        }
        var index = 0
        while (index < models.size) {
            models[index].data.copyInto(packed, index * FLOATS_PER_INSTANCE)
            index += 1
        }
        VulkanBuffers.writeBufferMemoryFloats(device, resourcesFor(frameIndex).memory.handle, 0, packed)
    }

    /** This frame slot's buffer, for the shared opaque feature to bind at binding 1. */
    fun binding(frameIndex: Int): VulkanBufferBinding = resourcesFor(frameIndex).binding

    fun bind(frameIndex: Int, commandBuffer: Long) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            INSTANCE_BINDING,
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
            "InstanceBuffer frame index $frameIndex is outside 0..${frameResources.lastIndex}."
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

    companion object {
        /** One `mat4` per instance. */
        const val FLOATS_PER_INSTANCE = 16

        /** 4096 * 64 B = 256 KB per frame slot -- far above what any current demo scatters,
         * cheap enough not to need tuning, and overridable per call site when one does. */
        const val DEFAULT_MAX_INSTANCES = 4096

        private const val INSTANCE_BINDING = 1
    }
}
