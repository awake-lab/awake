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
 * stores the texture binding. Per-frame/per-draw uniform buffers and descriptor sets are
 * then created lazily when the renderer knows which frame-in-flight slot and draw occurrence
 * this material is being used for.
 */
class Material(
    graphicsDevice: GraphicsDevice,
    private val uniformFloatCount: Int = DEFAULT_UNIFORM_FLOAT_COUNT
) : RenderMaterial {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device

    val descriptorSetLayout: DescriptorSetLayoutHandle

    var descriptorPool: DescriptorPoolHandle = DescriptorPoolHandle(0)
        private set
    var descriptorSet: DescriptorSetHandle = DescriptorSetHandle(0)
        private set
    var uniformBuffer: BufferHandle = BufferHandle(0)
        private set
    var uniformBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
        private set

    private val uniformSlotsByFrame = mutableListOf<MutableList<UniformSlot>>()

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
                        // Two separate bindings (image + sampler), not one combined-image-
                        // sampler -- WGSL has no combined-sampler type at all, so naga always
                        // compiles a `texture_2d` + `sampler` pair to two separate SPIR-V
                        // bindings (confirmed via spirv-dis on textured.wgsl's own output).
                        // Every material gets both regardless of whether its own shader
                        // actually samples a texture (triangle.wgsl/skinned.wgsl's pipeline
                        // layouts simply never read them) -- one shared Material shape is
                        // simpler than a per-shader descriptor-layout variant.
                        VkDescriptorSetLayoutBinding(
                            binding = 1,
                            descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE,
                            stageFlags = VkShaderStageFlagBits.FRAGMENT.value
                        ),
                        VkDescriptorSetLayoutBinding(
                            binding = 2,
                            descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER,
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
    }

    private fun createUniformSlot(): UniformSlot {
        require(samplerHandle != 0L && imageViewHandle != 0L) {
            "Material resources must be created before allocating uniform slots."
        }
        val (rawUniformBuffer, rawUniformBufferMemory) = createMaterialUniformBuffer(
            graphicsDevice,
            uniformFloatCount
        )
        val rawDescriptorPool = createMaterialDescriptorPool(device)
        val rawDescriptorSet = createMaterialDescriptorSet(
            device = device,
            descriptorPool = rawDescriptorPool,
            bindings = MaterialDescriptorSetBindings(
                descriptorSetLayout = descriptorSetLayout.handle,
                uniformBuffer = rawUniformBuffer,
                uniformFloatCount = uniformFloatCount,
                sampler = samplerHandle,
                imageView = imageViewHandle
            )
        )
        return UniformSlot(
            descriptorPool = DescriptorPoolHandle(rawDescriptorPool),
            descriptorSet = DescriptorSetHandle(rawDescriptorSet),
            uniformBuffer = BufferHandle(rawUniformBuffer),
            uniformBufferMemory = DeviceMemoryHandle(rawUniformBufferMemory)
        )
    }

    private fun uniformSlot(frameIndex: Int, drawSlotIndex: Int): UniformSlot {
        require(frameIndex >= 0) { "frameIndex must be non-negative." }
        require(drawSlotIndex >= 0) { "drawSlotIndex must be non-negative." }
        while (uniformSlotsByFrame.size <= frameIndex) uniformSlotsByFrame.add(mutableListOf())
        val frameSlots = uniformSlotsByFrame[frameIndex]
        while (frameSlots.size <= drawSlotIndex) frameSlots += createUniformSlot()
        val slot = frameSlots[drawSlotIndex]
        if (frameIndex == 0 && drawSlotIndex == 0) {
            descriptorPool = slot.descriptorPool
            descriptorSet = slot.descriptorSet
            uniformBuffer = slot.uniformBuffer
            uniformBufferMemory = slot.uniformBufferMemory
        }
        return slot
    }

    /** Rewrites the whole uniform buffer with a new MVP matrix (column-major `FloatArray`,
     * as produced by `Mat4.data`). This compatibility overload targets frame/draw slot 0;
     * the renderer's frame path uses [updateUniformBuffer] with explicit frame/draw slots so
     * one shared material can be drawn multiple times without later draws overwriting earlier
     * uniforms before the GPU consumes them. */
    override fun updateUniformBuffer(mvp: FloatArray) {
        updateUniformBuffer(frameIndex = 0, drawSlotIndex = 0, values = mvp)
    }

    fun updateUniformBuffer(frameIndex: Int, drawSlotIndex: Int, values: FloatArray) {
        val slot = uniformSlot(frameIndex, drawSlotIndex)
        VulkanBuffers.writeBufferMemoryFloats(device, slot.uniformBufferMemory.handle, 0, values)
    }

    override fun bind(commandBuffer: Long, pipelineLayout: Long) {
        bind(commandBuffer, pipelineLayout, frameIndex = 0, drawSlotIndex = 0)
    }

    fun bind(commandBuffer: Long, pipelineLayout: Long, frameIndex: Int, drawSlotIndex: Int) {
        VulkanDescriptors.vkCmdBindDescriptorSet(
            commandBuffer,
            pipelineLayout,
            0,
            uniformSlot(frameIndex, drawSlotIndex).descriptorSet.handle
        )
    }

    override fun destroy() {
        uniformSlotsByFrame.forEach { frameSlots ->
            frameSlots.forEach { slot ->
                VulkanBuffers.vkDestroyBuffer(device, slot.uniformBuffer.handle)
                VulkanBuffers.vkFreeMemory(device, slot.uniformBufferMemory.handle)
                VulkanDescriptors.vkDestroyDescriptorPool(device, slot.descriptorPool.handle)
            }
        }
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout.handle)
    }

    private data class UniformSlot(
        val descriptorPool: DescriptorPoolHandle,
        val descriptorSet: DescriptorSetHandle,
        val uniformBuffer: BufferHandle,
        val uniformBufferMemory: DeviceMemoryHandle
    )

    private companion object {
        /** A bare MVP matrix -- every material before skinning existed. A skinned material
         * requests `16 + 16 * jointCount` (MVP + joint palette) instead, see
         * `Renderer.createMaterial`'s own `uniformFloatCount` parameter. */
        const val DEFAULT_UNIFORM_FLOAT_COUNT = 16
    }
}

private fun createMaterialUniformBuffer(graphicsDevice: GraphicsDevice, uniformFloatCount: Int): Pair<Long, Long> {
    val device = graphicsDevice.device
    val rawUniformBuffer = VulkanBuffers.vkCreateBuffer(
        device,
        VkBufferCreateInfo(
            size = (uniformFloatCount * Float.SIZE_BYTES).toLong(),
            usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
        )
    )
    val memRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, rawUniformBuffer)
    val memoryTypeIndex = VulkanBuffers.findMemoryType(
        graphicsDevice.physicalDevice,
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
    return rawUniformBuffer to rawUniformBufferMemory
}

private fun createMaterialDescriptorPool(device: Long): Long = VulkanDescriptors.vkCreateDescriptorPool(
    device,
    VkDescriptorPoolCreateInfo(
        maxSets = 1,
        pPoolSizes = arrayOf(
            VkDescriptorPoolSize(
                type = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                descriptorCount = 1
            ),
            VkDescriptorPoolSize(
                type = VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE,
                descriptorCount = 1
            ),
            VkDescriptorPoolSize(
                type = VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER,
                descriptorCount = 1
            )
        )
    )
)

private fun createMaterialDescriptorSet(
    device: Long,
    descriptorPool: Long,
    bindings: MaterialDescriptorSetBindings
): Long {
    val rawDescriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(
        device,
        descriptorPool,
        bindings.descriptorSetLayout
    )
    VulkanDescriptors.vkUpdateDescriptorSetBuffer(
        device,
        rawDescriptorSet,
        0,
        VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
        VkDescriptorBufferInfo(
            buffer = bindings.uniformBuffer,
            range = (bindings.uniformFloatCount * Float.SIZE_BYTES).toLong()
        )
    )
    // Two separate writes -- see the descriptor set layout's own comment for why. Vulkan
    // ignores whichever of sampler/imageView doesn't apply to a given descriptorType, so
    // passing 0 (VK_NULL_HANDLE) for the other field each time is correct, not just harmless.
    VulkanDescriptors.vkUpdateDescriptorSetImage(
        device,
        rawDescriptorSet,
        1,
        VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE,
        VkDescriptorImageInfo(
            sampler = 0L,
            imageView = bindings.imageView
        )
    )
    VulkanDescriptors.vkUpdateDescriptorSetImage(
        device,
        rawDescriptorSet,
        2,
        VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER,
        VkDescriptorImageInfo(
            sampler = bindings.sampler,
            imageView = 0L
        )
    )
    return rawDescriptorSet
}

private data class MaterialDescriptorSetBindings(
    val descriptorSetLayout: Long,
    val uniformBuffer: Long,
    val uniformFloatCount: Int,
    val sampler: Long,
    val imageView: Long
)
