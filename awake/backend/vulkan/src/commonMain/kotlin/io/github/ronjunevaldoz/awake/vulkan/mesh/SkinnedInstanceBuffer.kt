// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorPoolHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolSize
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutBinding
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.VulkanMaterialBinding

/**
 * The per-instance JOINT PALETTES behind one animated instanced draw call -- [InstanceBuffer]'s
 * animated companion, with the same HOST_VISIBLE|HOST_COHERENT, fixed-capacity,
 * rewritten-wholesale-per-frame-slot lifecycle. Two things differ:
 *
 * - it is a STORAGE buffer bound through a descriptor set, not an instance-rate vertex buffer.
 *   A vertex attribute can't carry 64 matrices, and a uniform buffer can't hold enough of them
 *   (64 `mat4` = 4 KB per instance blows the 64 KB uniform limit after 16 copies). See
 *   `skinned_instanced.wgsl`, which reads it as `array<JointPalette>` indexed by
 *   `@builtin(instance_index)`.
 * - it owns its own descriptor set layout/pool/set, rather than binding through
 *   [io.github.ronjunevaldoz.awake.vulkan.material.Material]'s set. That set (set 0) is shared
 *   by every pipeline in the renderer; adding a palette binding to it would force every
 *   material -- textured, lit, UI -- to carry a storage descriptor it never writes. So this
 *   binds as set 1 ([PALETTE_SET]), the conventional per-draw-frequency split.
 */
class SkinnedInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    /** Hard ceiling on animated instances per draw call. Each one costs
     * [FLOATS_PER_INSTANCE] floats = 4 KB of host-visible memory PER FRAME SLOT (64 `mat4`
     * joints), 16x what a static instance costs in [InstanceBuffer] -- so raising this is a
     * real memory decision, not a free knob. [update] fails loudly naming it rather than
     * silently truncating. */
    private val maxInstances: Int = DEFAULT_MAX_INSTANCES,
    framesInFlight: Int = 1,
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    /** Layout-compatible with (a separate instance of) the one the skinned-instanced pipeline's
     * layout was built from -- see [createDescriptorSetLayout]. */
    private val descriptorSetLayout: DescriptorSetLayoutHandle =
        createDescriptorSetLayout(graphicsDevice)

    private data class FrameResources(
        val buffer: BufferHandle,
        val memory: DeviceMemoryHandle,
        val descriptorPool: DescriptorPoolHandle,
        val descriptorSet: DescriptorSetHandle,
    ) : VulkanMaterialBinding {
        override val descriptorSetHandle: Long get() = descriptorSet.handle
    }

    private val byteSize = (maxInstances.toLong() * FLOATS_PER_INSTANCE * Float.SIZE_BYTES)

    private val frameResources: Array<FrameResources> = Array(framesInFlight) {
        val (buffer, memory) = allocateHostVisibleBuffer(byteSize)
        val pool = createDescriptorPool()
        val set = VulkanDescriptors.vkAllocateDescriptorSet(device, pool, descriptorSetLayout.handle)
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            set,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
            VkDescriptorBufferInfo(buffer = buffer, range = byteSize),
        )
        FrameResources(
            BufferHandle(buffer),
            DeviceMemoryHandle(memory),
            DescriptorPoolHandle(pool),
            DescriptorSetHandle(set),
        )
    }

    // Reused across frames so a steady instance count allocates nothing per frame.
    private var packed: FloatArray = FloatArray(0)

    /** Packs [palettes] into this frame slot's buffer at a fixed [FLOATS_PER_INSTANCE] stride --
     * the shader indexes `palettes[instance_index]` as an array of fixed-size structs, so a
     * shorter palette (a skin with fewer than [MAX_JOINTS] joints, e.g. CesiumMan's 19) is
     * written at the start of its slot and the rest of the slot is simply never indexed. */
    fun update(frameIndex: Int, palettes: List<FloatArray>) {
        require(palettes.size <= maxInstances) {
            "Animated instance count (${palettes.size}) exceeds SkinnedInstanceBuffer capacity " +
                "($maxInstances) -- raise maxInstances or draw fewer instances."
        }
        if (palettes.isEmpty()) return
        if (packed.size != palettes.size * FLOATS_PER_INSTANCE) {
            packed = FloatArray(palettes.size * FLOATS_PER_INSTANCE)
        }
        var index = 0
        while (index < palettes.size) {
            val palette = palettes[index]
            require(palette.size <= FLOATS_PER_INSTANCE) {
                "Joint palette ${palette.size} floats exceeds MAX_JOINTS ($MAX_JOINTS) * 16."
            }
            palette.copyInto(packed, index * FLOATS_PER_INSTANCE)
            index += 1
        }
        VulkanBuffers.writeBufferMemoryFloats(device, resourcesFor(frameIndex).memory.handle, 0, packed)
    }

    /** Binds this frame slot's palette descriptor set as [PALETTE_SET]. The material's own set 0
     * is bound separately (see `RendererDraw3D.recordDrawCalls`). */
    /** This frame slot's palette descriptor set, for the shared opaque feature to bind at
     * [PALETTE_SET]. */
    fun binding(frameIndex: Int): VulkanMaterialBinding = resourcesFor(frameIndex)

    fun bind(frameIndex: Int, commandBuffer: Long, pipelineLayout: Long) {
        VulkanDescriptors.vkCmdBindDescriptorSet(
            commandBuffer,
            pipelineLayout,
            PALETTE_SET,
            resourcesFor(frameIndex).descriptorSet.handle,
        )
    }

    fun destroy() {
        frameResources.forEach { frame ->
            VulkanBuffers.vkDestroyBuffer(device, frame.buffer.handle)
            VulkanBuffers.vkFreeMemory(device, frame.memory.handle)
            VulkanDescriptors.vkDestroyDescriptorPool(device, frame.descriptorPool.handle)
        }
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout.handle)
    }

    private fun resourcesFor(frameIndex: Int): FrameResources {
        require(frameIndex in frameResources.indices) {
            "SkinnedInstanceBuffer frame index $frameIndex is outside 0..${frameResources.lastIndex}."
        }
        return frameResources[frameIndex]
    }

    private fun createDescriptorPool(): Long = VulkanDescriptors.vkCreateDescriptorPool(
        device,
        VkDescriptorPoolCreateInfo(
            maxSets = 1,
            pPoolSizes = arrayOf(
                VkDescriptorPoolSize(
                    type = VkDescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                    descriptorCount = 1,
                ),
            ),
        ),
    )

    private fun allocateHostVisibleBuffer(byteSize: Long): Pair<Long, Long> {
        val buffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
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
        /** `skinned_instanced.wgsl`'s own MAX_JOINTS -- see `skinned.wgsl` for why 64. */
        const val MAX_JOINTS = 64

        /** One fixed-size `JointPalette` struct: 64 `mat4` = 1024 floats = 4 KB per instance. */
        const val FLOATS_PER_INSTANCE = MAX_JOINTS * 16

        /** 256 * 4 KB = 1 MB per frame slot. Far lower than [InstanceBuffer]'s 4096 because an
         * animated instance costs 16x a static one -- 4096 here would be 16 MB per slot. */
        const val DEFAULT_MAX_INSTANCES = 256

        /** Descriptor set 1 -- set 0 is the material's. See this class's own doc comment. */
        const val PALETTE_SET = 1

        /** The one-binding storage layout `skinned_instanced.wgsl` declares at
         * `@group(1) @binding(0)`. Both this class (for its own descriptor sets) and whoever
         * builds the skinned-instanced pipeline layout call this and get their OWN handle:
         * Vulkan set layouts are compatible by identical declaration, not by object identity,
         * so nothing has to be threaded from the bootstrap into every pooled buffer. */
        fun createDescriptorSetLayout(graphicsDevice: GraphicsDevice): DescriptorSetLayoutHandle =
            DescriptorSetLayoutHandle(
                VulkanDescriptors.vkCreateDescriptorSetLayout(
                    graphicsDevice.device,
                    VkDescriptorSetLayoutCreateInfo(
                        pBindings = arrayOf(
                            VkDescriptorSetLayoutBinding(
                                binding = 0,
                                descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                                stageFlags = VkShaderStageFlagBits.VERTEX.value,
                            ),
                        ),
                    ),
                ),
            )
    }
}
