// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.debug

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolSize
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutBinding
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendAttachmentState
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDepthStencilStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDynamicStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineInputAssemblyStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineMultisampleStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineRasterizationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineShaderStageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineVertexInputStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineViewportStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * The procedural sky pipeline (`skybox.wgsl`) -- [LineRenderPipeline]'s shape minus the vertex
 * input entirely: the fragment covers the viewport via a full-screen triangle generated from
 * `vertex_index`, so there is no vertex buffer to bind and [draw] issues a bare 3-vertex call.
 *
 * Built against the EXISTING 3D pass's [renderPass] (same reuse [LineRenderPipeline] already
 * does), and recorded as the FIRST draw inside that pass, right after the clear. Depth test and
 * depth write are both off: nothing has drawn yet, so there is nothing to occlude or be
 * occluded by, and leaving the depth buffer at its cleared 1.0 lets all the scene geometry
 * recorded afterwards depth-test against each other exactly as it always did.
 *
 * The uniform block is visible to BOTH stages (unlike [LineRenderPipeline]'s vertex-only MVP):
 * the vertex stage needs nothing from it, but the fragment stage reads all 40 floats.
 */
class SkyboxRenderPipeline(
    graphicsDevice: GraphicsDevice,
    private val swapchainManager: SwapchainManager,
    private val renderPass: Long,
    shaders: ShaderPair,
    private val framesInFlight: Int = 1,
) {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device

    private var descriptorSetLayout: Long = 0
    private var pipelineLayout: Long = 0
    private var pipelineCache: Long = 0
    private var graphicsPipeline: LongArray = longArrayOf()
    private val uniformSlots = mutableListOf<UniformSlot>()

    private data class UniformSlot(
        val descriptorPool: Long,
        val descriptorSet: Long,
        val buffer: Long,
        val bufferMemory: Long,
    )

    init {
        require(framesInFlight > 0) { "framesInFlight must be positive." }
        descriptorSetLayout = createDescriptorSetLayout()
        repeat(framesInFlight) { uniformSlots += createUniformSlot() }
        createGraphicsPipeline(shaders.vertex, shaders.fragment)
    }

    private fun createDescriptorSetLayout(): Long = VulkanDescriptors.vkCreateDescriptorSetLayout(
        device,
        VkDescriptorSetLayoutCreateInfo(
            pBindings = arrayOf(
                VkDescriptorSetLayoutBinding(
                    binding = 0,
                    descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                    stageFlags = VkShaderStageFlagBits.VERTEX.value or
                        VkShaderStageFlagBits.FRAGMENT.value,
                ),
            ),
        ),
    )

    private fun createUniformBuffer(): Pair<Long, Long> {
        val buffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = UNIFORM_BYTES.toLong(),
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

    private fun createUniformSlot(): UniformSlot {
        val (buffer, bufferMemory) = createUniformBuffer()
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
            VkDescriptorBufferInfo(buffer = buffer, range = UNIFORM_BYTES.toLong()),
        )
        return UniformSlot(descriptorPool, descriptorSet, buffer, bufferMemory)
    }

    /** [uniforms] must be exactly [UNIFORM_FLOATS] long, in `skybox.wgsl`'s field order. */
    fun writeUniforms(frameIndex: Int, uniforms: FloatArray) {
        require(uniforms.size == UNIFORM_FLOATS) {
            "Skybox uniform block must be $UNIFORM_FLOATS floats, got ${uniforms.size}."
        }
        VulkanBuffers.writeBufferMemoryFloats(device, uniformSlot(frameIndex).bufferMemory, 0, uniforms)
    }

    private fun createShaderModule(code: IntArray): Long =
        Vulkan.vkCreateShaderModule(device, VkShaderModuleCreateInfo(pCode = code))

    private fun ByteArray.toIntArray(): IntArray = IntArray(size / 4) { i ->
        (this[i * 4].toInt() and 0xFF) or
            ((this[i * 4 + 1].toInt() and 0xFF) shl 8) or
            ((this[i * 4 + 2].toInt() and 0xFF) shl 16) or
            ((this[i * 4 + 3].toInt() and 0xFF) shl 24)
    }

    private fun createGraphicsPipeline(vertShaderCode: ByteArray, fragShaderCode: ByteArray) {
        val fragShaderModule = createShaderModule(fragShaderCode.toIntArray())
        val vertShaderModule = createShaderModule(vertShaderCode.toIntArray())

        val shaderStages = arrayOf(
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.FRAGMENT,
                module = fragShaderModule,
                pName = "fragmentMain",
            ),
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.VERTEX,
                module = vertShaderModule,
                pName = "vertexMain",
            ),
        )

        // No bindings and no attributes: the vertex shader synthesizes its three corners.
        val vertexInputInfo = arrayOf(VkPipelineVertexInputStateCreateInfo())

        val dynamicInfo = arrayOf(
            VkPipelineDynamicStateCreateInfo(
                pDynamicStates = arrayOf(
                    VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT,
                    VkDynamicState.VK_DYNAMIC_STATE_SCISSOR,
                ),
            ),
        )

        val viewportInfo = arrayOf(
            VkPipelineViewportStateCreateInfo(
                pViewports = arrayOf(
                    VkViewport(
                        width = swapchainManager.extent.width.toFloat(),
                        height = swapchainManager.extent.height.toFloat(),
                    ),
                ),
                pScissors = arrayOf(VkRect2D(offset = VkOffset2D(), extent = swapchainManager.extent)),
            ),
        )

        // Both off -- see this class's own doc comment.
        val depthStencil = arrayOf(
            VkPipelineDepthStencilStateCreateInfo(
                depthTestEnable = false,
                depthWriteEnable = false,
            ),
        )
        val multisamplingInfo = arrayOf(VkPipelineMultisampleStateCreateInfo())

        val inputAssemblyInfo = arrayOf(
            VkPipelineInputAssemblyStateCreateInfo(
                topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                primitiveRestartEnable = false,
            ),
        )

        // NONE: the full-screen triangle's winding flips with the clip space's Y direction, and
        // there is nothing to cull in a single screen-covering primitive anyway.
        val rasterizationInfo = arrayOf(
            VkPipelineRasterizationStateCreateInfo(
                cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                lineWidth = 1f,
            ),
        )

        val blendAttachment = VkPipelineColorBlendAttachmentState(blendEnable = false)
        val colorBlendInfo = arrayOf(VkPipelineColorBlendStateCreateInfo(pAttachments = arrayOf(blendAttachment)))

        pipelineLayout = Vulkan.vkCreatePipelineLayout(
            device,
            VkPipelineLayoutCreateInfo(pSetLayouts = arrayOf(descriptorSetLayout)),
        )

        val createInfos = arrayOf(
            VkGraphicsPipelineCreateInfo(
                pStages = shaderStages,
                pVertexInputState = vertexInputInfo,
                pInputAssemblyState = inputAssemblyInfo,
                pViewportState = viewportInfo,
                pRasterizationState = rasterizationInfo,
                pMultisampleState = multisamplingInfo,
                pColorBlendState = colorBlendInfo,
                pDepthStencilState = depthStencil,
                pDynamicState = dynamicInfo,
                layout = pipelineLayout,
                renderPass = renderPass,
                subpass = 0,
                basePipelineHandle = 0,
                basePipelineIndex = -1,
            ),
        )
        pipelineCache = Vulkan.vkCreatePipelineCache(device, VkPipelineCacheCreateInfo())
        graphicsPipeline = Vulkan.vkCreateGraphicsPipelines(device, pipelineCache, createInfos)

        Vulkan.vkDestroyShaderModule(device, fragShaderModule)
        Vulkan.vkDestroyShaderModule(device, vertShaderModule)
    }

    /** Binds and issues the whole sky in one call -- no vertex/index buffer to bind first. */
    fun draw(commandBuffer: Long, frameIndex: Int) {
        val slot = uniformSlot(frameIndex)
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            graphicsPipeline[0],
        )
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, slot.descriptorSet)
        Vulkan.vkCmdDraw(commandBuffer, FULLSCREEN_TRIANGLE_VERTICES, 1, 0, 0)
    }

    fun destroy() {
        graphicsPipeline.forEach { Vulkan.vkDestroyPipeline(device, it) }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
        uniformSlots.forEach { slot ->
            VulkanBuffers.vkDestroyBuffer(device, slot.buffer)
            VulkanBuffers.vkFreeMemory(device, slot.bufferMemory)
            VulkanDescriptors.vkDestroyDescriptorPool(device, slot.descriptorPool)
        }
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout)
    }

    private fun uniformSlot(frameIndex: Int): UniformSlot {
        require(frameIndex in uniformSlots.indices) {
            "SkyboxRenderPipeline frame index $frameIndex is outside 0..${uniformSlots.lastIndex}."
        }
        return uniformSlots[frameIndex]
    }

    companion object {
        /** inverseViewProjection(16) + cameraEye(4) + sunDirection(4) + horizon/zenith/sun/moon
         * colors(4 each) -- `skybox.wgsl`'s Uniforms, in order. */
        const val UNIFORM_FLOATS = 40
        private const val UNIFORM_BYTES = UNIFORM_FLOATS * Float.SIZE_BYTES
        private const val FULLSCREEN_TRIANGLE_VERTICES = 3
    }
}
