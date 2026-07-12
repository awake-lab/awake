// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.ui

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkBlendFactor
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorImageInfo
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
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDynamicStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineInputAssemblyStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineMultisampleStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineRasterizationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineShaderStageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineVertexInputStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineViewportStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputAttributeDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputBindingDescription
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture

/**
 * Phase B (see docs/MVP_PLAN.md's custom-UI decision log): a second UI-overlay pipeline,
 * drawn in the same render pass as [UiRenderPipeline]'s colored quads (subpass unchanged --
 * this just binds a different pipeline + a textured [DynamicMesh] before the pass ends), for
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Glyph]s sampling a
 * [io.github.ronjunevaldoz.awake.ui.font.BitmapFont] atlas. Vertex layout is pos(vec2) +
 * uv(vec2) + color(vec4) -- see [DynamicMesh.GLYPH_FLOATS_PER_VERTEX].
 */
class UiGlyphRenderPipeline(
    graphicsDevice: GraphicsDevice,
    private val swapchainManager: SwapchainManager,
    private val renderPass: Long,
    vertShaderCode: ByteArray,
    fragShaderCode: ByteArray,
    private val fontTexture: Texture
) {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device

    private var descriptorSetLayout: Long = 0
    private var descriptorPool: Long = 0
    private var descriptorSet: Long = 0
    private var screenSizeBuffer: Long = 0
    private var screenSizeBufferMemory: Long = 0
    private var pipelineLayout: Long = 0
    private var pipelineCache: Long = 0
    private var graphicsPipeline: LongArray = longArrayOf()

    init {
        descriptorSetLayout = createDescriptorSetLayout()
        createScreenSizeUniformBuffer()
        createDescriptorSet()
        createGraphicsPipeline(vertShaderCode, fragShaderCode)
        writeScreenSize(swapchainManager.extent.width.toFloat(), swapchainManager.extent.height.toFloat())
    }

    private fun createDescriptorSetLayout(): Long = VulkanDescriptors.vkCreateDescriptorSetLayout(
        device,
        VkDescriptorSetLayoutCreateInfo(
            pBindings = arrayOf(
                VkDescriptorSetLayoutBinding(
                    binding = 0,
                    descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
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

    private fun createScreenSizeUniformBuffer() {
        screenSizeBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = SCREEN_SIZE_UNIFORM_BYTES.toLong(),
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT
            )
        )
        val requirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, screenSizeBuffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            graphicsDevice.physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        screenSizeBufferMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = requirements.size, memoryTypeIndex = memoryTypeIndex)
        )
        VulkanBuffers.vkBindBufferMemory(device, screenSizeBuffer, screenSizeBufferMemory, 0)
    }

    private fun createDescriptorSet() {
        descriptorPool = VulkanDescriptors.vkCreateDescriptorPool(
            device,
            VkDescriptorPoolCreateInfo(
                maxSets = 1,
                pPoolSizes = arrayOf(
                    VkDescriptorPoolSize(type = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, descriptorCount = 1),
                    VkDescriptorPoolSize(type = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, descriptorCount = 1)
                )
            )
        )
        descriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(device, descriptorPool, descriptorSetLayout)
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            descriptorSet,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            VkDescriptorBufferInfo(buffer = screenSizeBuffer, range = SCREEN_SIZE_UNIFORM_BYTES.toLong())
        )
        VulkanDescriptors.vkUpdateDescriptorSetImage(
            device,
            descriptorSet,
            1,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            VkDescriptorImageInfo(sampler = fontTexture.sampler.handle, imageView = fontTexture.imageView.handle)
        )
    }

    fun writeScreenSize(width: Float, height: Float) {
        VulkanBuffers.writeBufferMemoryFloats(device, screenSizeBufferMemory, 0, floatArrayOf(width, height, 0f, 0f))
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
            VkPipelineShaderStageCreateInfo(stage = VkShaderStageFlagBits.FRAGMENT, module = fragShaderModule, pName = "main"),
            VkPipelineShaderStageCreateInfo(stage = VkShaderStageFlagBits.VERTEX, module = vertShaderModule, pName = "main")
        )

        val vertexInputInfo = arrayOf(
            VkPipelineVertexInputStateCreateInfo(
                pVertexBindingDescriptions = arrayOf(
                    VkVertexInputBindingDescription(
                        binding = 0,
                        stride = DynamicMesh.GLYPH_FLOATS_PER_VERTEX * Float.SIZE_BYTES,
                        inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX
                    )
                ),
                pVertexAttributeDescriptions = arrayOf(
                    VkVertexInputAttributeDescription(
                        location = 0,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32_SFLOAT,
                        offset = 0
                    ),
                    VkVertexInputAttributeDescription(
                        location = 1,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32_SFLOAT,
                        offset = 2 * Float.SIZE_BYTES
                    ),
                    VkVertexInputAttributeDescription(
                        location = 2,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT,
                        offset = 4 * Float.SIZE_BYTES
                    )
                )
            )
        )

        val dynamicInfo = arrayOf(
            VkPipelineDynamicStateCreateInfo(
                pDynamicStates = arrayOf(VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT, VkDynamicState.VK_DYNAMIC_STATE_SCISSOR)
            )
        )

        val viewportInfo = arrayOf(
            VkPipelineViewportStateCreateInfo(
                pViewports = arrayOf(
                    VkViewport(
                        width = swapchainManager.extent.width.toFloat(),
                        height = swapchainManager.extent.height.toFloat()
                    )
                ),
                pScissors = arrayOf(VkRect2D(offset = VkOffset2D(), extent = swapchainManager.extent))
            )
        )

        val multisamplingInfo = arrayOf(VkPipelineMultisampleStateCreateInfo())

        val inputAssemblyInfo = arrayOf(
            VkPipelineInputAssemblyStateCreateInfo(
                topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                primitiveRestartEnable = false
            )
        )

        val rasterizationInfo = arrayOf(
            VkPipelineRasterizationStateCreateInfo(
                cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                lineWidth = 1f
            )
        )

        val blendAttachment = VkPipelineColorBlendAttachmentState(
            blendEnable = true,
            srcColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA,
            dstColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
            srcAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA,
            dstAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
            colorWriteMask = VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT.value
        )

        val colorBlendInfo = arrayOf(VkPipelineColorBlendStateCreateInfo(pAttachments = arrayOf(blendAttachment)))

        pipelineLayout = Vulkan.vkCreatePipelineLayout(
            device,
            VkPipelineLayoutCreateInfo(pSetLayouts = arrayOf(descriptorSetLayout))
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
                pDepthStencilState = null,
                pDynamicState = dynamicInfo,
                layout = pipelineLayout,
                renderPass = renderPass,
                subpass = 0,
                basePipelineHandle = 0,
                basePipelineIndex = -1
            )
        )
        pipelineCache = Vulkan.vkCreatePipelineCache(device, VkPipelineCacheCreateInfo())
        graphicsPipeline = Vulkan.vkCreateGraphicsPipelines(device, pipelineCache, createInfos)

        Vulkan.vkDestroyShaderModule(device, fragShaderModule)
        Vulkan.vkDestroyShaderModule(device, vertShaderModule)
    }

    fun bind(commandBuffer: Long) {
        Vulkan.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline[0])
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, descriptorSet)
    }

    fun destroy() {
        graphicsPipeline.forEach { Vulkan.vkDestroyPipeline(device, it) }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
        VulkanBuffers.vkDestroyBuffer(device, screenSizeBuffer)
        VulkanBuffers.vkFreeMemory(device, screenSizeBufferMemory)
        VulkanDescriptors.vkDestroyDescriptorPool(device, descriptorPool)
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout)
    }

    private companion object {
        const val SCREEN_SIZE_UNIFORM_BYTES = 4 * Float.SIZE_BYTES
    }
}
