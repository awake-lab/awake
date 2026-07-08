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

package io.github.ronjunevaldoz.awake.vulkan.material

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorImageInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolSize
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutBinding
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture

/**
 * Phase 2 (renderer abstraction): owns the MVP-matrix uniform buffer and the descriptor
 * set that binds it (plus a [Texture]'s sampler/view) to the pipeline -- extracted verbatim
 * from `VulkanApplication`'s `createDescriptorSetLayout`/`createUniformBuffer`/
 * `createDescriptorPool`/`createDescriptorSet`/`updateUniformBuffer` functions and their
 * backing fields.
 *
 * Split into two phases like [Mesh][io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh]/[Texture]
 * are lazily constructed relative to `VulkanApplication`'s other eager state, but for a
 * different reason here: [descriptorSetLayout] must exist *before* the graphics pipeline is
 * created (the pipeline layout references it), while the descriptor set itself can't be
 * written until a real [Texture] exists to read `sampler`/`imageView` from. So the
 * constructor only creates the layout; [createResources] (called once the texture is ready)
 * creates the uniform buffer, descriptor pool, and descriptor set.
 */
class Material(private val graphicsDevice: GraphicsDevice) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    val descriptorSetLayout: Long

    var descriptorPool: Long = 0
        private set
    var descriptorSet: Long = 0
        private set
    var uniformBuffer: Long = 0
        private set
    var uniformBufferMemory: Long = 0
        private set

    init {
        descriptorSetLayout = VulkanDescriptors.vkCreateDescriptorSetLayout(
            device,
            VkDescriptorSetLayoutCreateInfo(
                pBindings = arrayOf(
                    VkDescriptorSetLayoutBinding(
                        binding = 0,
                        descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                        // MVP matrix is only ever read in the vertex shader now.
                        stageFlags = VkShaderStageFlagBits.VERTEX.value
                    ),
                    VkDescriptorSetLayoutBinding(
                        binding = 1,
                        descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                        stageFlags = VkShaderStageFlagBits.FRAGMENT.value
                    )
                )
            )
        )
    }

    /** Creates the uniform buffer, descriptor pool, and descriptor set (written to bind
     * both [uniformBuffer] and [texture]'s sampler/view). Must be called once, after a real
     * [Texture] exists. */
    fun createResources(texture: Texture) {
        val bufferSize = (16 * Float.SIZE_BYTES).toLong()
        uniformBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = bufferSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            )
        )
        val memRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, uniformBuffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            memRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        uniformBufferMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = memRequirements.size,
                memoryTypeIndex = memoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, uniformBuffer, uniformBufferMemory, 0)

        descriptorPool = VulkanDescriptors.vkCreateDescriptorPool(
            device,
            VkDescriptorPoolCreateInfo(
                maxSets = 1,
                pPoolSizes = arrayOf(
                    VkDescriptorPoolSize(
                        type = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                        descriptorCount = 1
                    ),
                    VkDescriptorPoolSize(
                        type = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                        descriptorCount = 1
                    )
                )
            )
        )

        descriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(
            device,
            descriptorPool,
            descriptorSetLayout
        )
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            descriptorSet,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            VkDescriptorBufferInfo(
                buffer = uniformBuffer,
                range = (16 * Float.SIZE_BYTES).toLong()
            )
        )
        VulkanDescriptors.vkUpdateDescriptorSetImage(
            device,
            descriptorSet,
            1,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            VkDescriptorImageInfo(
                sampler = texture.sampler,
                imageView = texture.imageView
            )
        )
    }

    /** Rewrites the whole uniform buffer with a new MVP matrix (column-major `FloatArray`,
     * as produced by `Mat4.data`). Caller is responsible for the same serialization
     * `VulkanApplication.drawFrame` already does around this (`vkDeviceWaitIdle` after every
     * submit) -- this is a single shared buffer, not per-frame-in-flight. */
    fun updateUniformBuffer(mvp: FloatArray) {
        VulkanBuffers.writeBufferMemoryFloats(device, uniformBufferMemory, 0, mvp)
    }

    fun bind(commandBuffer: Long, pipelineLayout: Long) {
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, descriptorSet)
    }

    fun destroy() {
        VulkanBuffers.vkDestroyBuffer(device, uniformBuffer)
        VulkanBuffers.vkFreeMemory(device, uniformBufferMemory)
        VulkanDescriptors.vkDestroyDescriptorPool(device, descriptorPool)
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout)
    }
}
