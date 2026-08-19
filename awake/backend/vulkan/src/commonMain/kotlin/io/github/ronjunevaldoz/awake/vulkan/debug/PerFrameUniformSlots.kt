// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.debug

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolSize
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutBinding
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

/** One `VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER` binding (binding 0, one descriptor set layout) with
 * one buffer/descriptor-set pair per frame in flight -- was hand-rolled identically in both
 * [LineRenderPipeline] and [SkyboxRenderPipeline] (only [uniformBytes]/[stageFlags] differed).
 * Owns create/write/destroy for the whole per-frame set; the pipeline class that holds one of
 * these only ever reads [get]/[write] and calls [destroy] alongside its own teardown. */
internal class PerFrameUniformSlots(
    private val graphicsDevice: GraphicsDevice,
    private val uniformBytes: Int,
    stageFlags: Int,
    framesInFlight: Int,
) {
    class Slot(val descriptorPool: Long, val descriptorSet: Long, val buffer: Long, val bufferMemory: Long)

    private val device get() = graphicsDevice.device

    // var, not val: assigned inside init's try block below, and destroy() (called from the
    // catch path on partial-failure) must be able to read it even if construction never
    // reached the assignment -- 0 is Vulkan's documented null-handle, destroy() no-ops on it.
    var descriptorSetLayout: Long = 0
        private set
    private val slots = mutableListOf<Slot>()

    // Same partial-creation-leak guard as RenderPipeline's own init -- if slot N throws, slots
    // 0..N-1 (and the layout, if already built) would otherwise leak with nothing yet holding
    // this half-built instance to call destroy() on.
    init {
        require(framesInFlight > 0) { "framesInFlight must be positive." }
        try {
            descriptorSetLayout = VulkanDescriptors.vkCreateDescriptorSetLayout(
                device,
                VkDescriptorSetLayoutCreateInfo(
                    pBindings = arrayOf(
                        VkDescriptorSetLayoutBinding(
                            binding = 0,
                            descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                            stageFlags = stageFlags,
                        ),
                    ),
                ),
            )
            repeat(framesInFlight) { slots += createSlot() }
        } catch (e: Throwable) {
            destroy()
            throw e
        }
    }

    private fun createBuffer(): Pair<Long, Long> {
        val buffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = uniformBytes.toLong(),
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            ),
        )
        val requirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, buffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            graphicsDevice.physicalDevice,
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

    private fun createSlot(): Slot {
        val (buffer, bufferMemory) = createBuffer()
        val descriptorPool = VulkanDescriptors.vkCreateDescriptorPool(
            device,
            VkDescriptorPoolCreateInfo(
                maxSets = 1,
                pPoolSizes = arrayOf(
                    VkDescriptorPoolSize(type = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, descriptorCount = 1),
                ),
            ),
        )
        val descriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(device, descriptorPool, descriptorSetLayout)
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            descriptorSet,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            VkDescriptorBufferInfo(buffer = buffer, range = uniformBytes.toLong()),
        )
        return Slot(descriptorPool, descriptorSet, buffer, bufferMemory)
    }

    operator fun get(frameIndex: Int): Slot {
        require(frameIndex in slots.indices) {
            "PerFrameUniformSlots frame index $frameIndex is outside 0..${slots.lastIndex}."
        }
        return slots[frameIndex]
    }

    fun write(frameIndex: Int, floats: FloatArray) {
        VulkanBuffers.writeBufferMemoryFloats(device, this[frameIndex].bufferMemory, 0, floats)
    }

    fun destroy() {
        slots.forEach { slot ->
            VulkanBuffers.vkDestroyBuffer(device, slot.buffer)
            VulkanBuffers.vkFreeMemory(device, slot.bufferMemory)
            VulkanDescriptors.vkDestroyDescriptorPool(device, slot.descriptorPool)
        }
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout)
    }
}
