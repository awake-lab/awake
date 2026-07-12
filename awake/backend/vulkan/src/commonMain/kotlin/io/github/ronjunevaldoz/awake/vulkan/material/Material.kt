// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.material

import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
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
class Material(graphicsDevice: GraphicsDevice) : RenderMaterial {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    val descriptorSetLayout: DescriptorSetLayoutHandle

    var descriptorPool: DescriptorPoolHandle = DescriptorPoolHandle(0)
    var descriptorSet: DescriptorSetHandle = DescriptorSetHandle(0)
    var uniformBuffer: BufferHandle = BufferHandle(0)
    var uniformBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)

    /** The sampler/image view this material was built with -- exposed (read-only) so
     * `UiTextureRenderPipeline` can bind the SAME sampled image into its own (screen-space
     * quad) descriptor set for on-screen compositing, without re-deriving them from whatever
     * [Texture]/`OffscreenRenderTarget` this material was created from. */
    var samplerHandle: Long = 0
        private set
    var imageViewHandle: Long = 0
        private set

    init {
        descriptorSetLayout = DescriptorSetLayoutHandle(
            VulkanDescriptors.vkCreateDescriptorSetLayout(
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
        )
    }

    /** Creates the uniform buffer, descriptor pool, and descriptor set (written to bind
     * both [uniformBuffer] and [texture]'s sampler/view). Must be called once, after a real
     * [Texture] exists. */
    fun createResources(texture: Texture) {
        createResources(texture.sampler.handle, texture.imageView.handle)
    }

    /** Same as [createResources] but binds an
     * [io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget]'s color
     * attachment directly instead of a [Texture]'s -- the on-screen compositing/portal-camera
     * use case (`Renderer.createMaterial(renderTarget = ...)`). Same descriptor-writing code
     * either way: a `VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER` binding doesn't care whether
     * the sampler/image view it's given came from a CPU-uploaded texture or a GPU-only
     * render target. */
    fun createResourcesFromRenderTarget(sampler: Long, imageView: Long) {
        createResources(sampler, imageView)
    }

    private fun createResources(sampler: Long, imageView: Long) {
        samplerHandle = sampler
        imageViewHandle = imageView
        val bufferSize = (16 * Float.SIZE_BYTES).toLong()
        val rawUniformBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = bufferSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            )
        )
        val memRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, rawUniformBuffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            memRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val rawUniformBufferMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = memRequirements.size,
                memoryTypeIndex = memoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, rawUniformBuffer, rawUniformBufferMemory, 0)
        uniformBuffer = BufferHandle(rawUniformBuffer)
        uniformBufferMemory = DeviceMemoryHandle(rawUniformBufferMemory)

        val rawDescriptorPool = VulkanDescriptors.vkCreateDescriptorPool(
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
        descriptorPool = DescriptorPoolHandle(rawDescriptorPool)

        val rawDescriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(
            device,
            rawDescriptorPool,
            descriptorSetLayout.handle
        )
        descriptorSet = DescriptorSetHandle(rawDescriptorSet)
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            rawDescriptorSet,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            VkDescriptorBufferInfo(
                buffer = rawUniformBuffer,
                range = (16 * Float.SIZE_BYTES).toLong()
            )
        )
        VulkanDescriptors.vkUpdateDescriptorSetImage(
            device,
            rawDescriptorSet,
            1,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            VkDescriptorImageInfo(
                sampler = sampler,
                imageView = imageView
            )
        )
    }

    /** Rewrites the whole uniform buffer with a new MVP matrix (column-major `FloatArray`,
     * as produced by `Mat4.data`). Caller is responsible for the same serialization
     * `VulkanApplication.drawFrame` already does around this (`vkDeviceWaitIdle` after every
     * submit) -- this is a single shared buffer, not per-frame-in-flight. */
    override fun updateUniformBuffer(mvp: FloatArray) {
        VulkanBuffers.writeBufferMemoryFloats(device, uniformBufferMemory.handle, 0, mvp)
    }

    override fun bind(commandBuffer: Long, pipelineLayout: Long) {
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, descriptorSet.handle)
    }

    override fun destroy() {
        VulkanBuffers.vkDestroyBuffer(device, uniformBuffer.handle)
        VulkanBuffers.vkFreeMemory(device, uniformBufferMemory.handle)
        VulkanDescriptors.vkDestroyDescriptorPool(device, descriptorPool.handle)
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout.handle)
    }
}
