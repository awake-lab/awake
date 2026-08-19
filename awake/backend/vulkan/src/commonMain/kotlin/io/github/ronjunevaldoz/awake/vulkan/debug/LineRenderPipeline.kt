// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.debug

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
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
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputAttributeDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputBindingDescription
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.createShaderModule
import io.github.ronjunevaldoz.awake.vulkan.pipeline.toShaderIntArray
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * A `LINE_LIST` pipeline for world-space debug lines (e.g. a
 * [io.github.ronjunevaldoz.awake.core.math.Frustum] wireframe) -- reuses the existing 3D
 * [io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline]'s already-created
 * [renderPass] (same pattern `UiGlyphRenderPipeline` uses for `UiRenderPipeline`'s render
 * pass), so lines draw within the same render pass/depth attachment as scene geometry --
 * real depth-testing against the cube/ground, not an X-ray overlay. Bound and drawn right
 * after the 3D draw-call loop, before that pass ends (see `Renderer.recordCommandBuffer`).
 */
class LineRenderPipeline(
    graphicsDevice: GraphicsDevice,
    private val swapchainManager: SwapchainManager,
    private val renderPass: Long,
    shaders: ShaderPair,
    private val framesInFlight: Int = 1,
) {
    private val graphicsDevice = graphicsDevice
    private val device get() = graphicsDevice.device

    private var pipelineLayout: Long = 0
    private var pipelineCache: Long = 0
    private var graphicsPipeline: LongArray = longArrayOf()
    private lateinit var uniformSlots: PerFrameUniformSlots
    private val descriptorSetLayout get() = uniformSlots.descriptorSetLayout

    // See RenderPipeline's own init doc comment -- same partial-creation-leak guard.
    init {
        require(framesInFlight > 0) { "framesInFlight must be positive." }
        try {
            uniformSlots = PerFrameUniformSlots(
                graphicsDevice, MVP_UNIFORM_BYTES, VkShaderStageFlagBits.VERTEX.value, framesInFlight,
            )
            createGraphicsPipeline(shaders.vertex, shaders.fragment)
        } catch (e: Throwable) {
            destroy()
            throw e
        }
    }

    /** Writes this frame's view-projection matrix (lines are already in world space, so
     * model is implicitly identity -- mvp == viewProjection). */
    fun writeMvp(mvp: FloatArray) = writeMvp(frameIndex = 0, mvp = mvp)

    fun writeMvp(frameIndex: Int, mvp: FloatArray) = uniformSlots.write(frameIndex, mvp)

    private fun createGraphicsPipeline(vertShaderCode: ByteArray, fragShaderCode: ByteArray) {
        val fragShaderModule = createShaderModule(device, fragShaderCode.toShaderIntArray())
        val vertShaderModule = createShaderModule(device, vertShaderCode.toShaderIntArray())

        val shaderStages = arrayOf(
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.FRAGMENT,
                module = fragShaderModule,
                pName = "main",
            ),
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.VERTEX,
                module = vertShaderModule,
                pName = "main",
            ),
        )

        val vertexInputInfo = arrayOf(
            VkPipelineVertexInputStateCreateInfo(
                pVertexBindingDescriptions = arrayOf(
                    VkVertexInputBindingDescription(
                        binding = 0,
                        stride = LineMesh.FLOATS_PER_VERTEX * Float.SIZE_BYTES,
                        inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX,
                    ),
                ),
                pVertexAttributeDescriptions = arrayOf(
                    VkVertexInputAttributeDescription(
                        location = 0,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT,
                        offset = 0,
                    ),
                    VkVertexInputAttributeDescription(
                        location = 1,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT,
                        offset = 3 * Float.SIZE_BYTES,
                    ),
                ),
            ),
        )

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

        val depthStencil = arrayOf(VkPipelineDepthStencilStateCreateInfo())
        val multisamplingInfo = arrayOf(VkPipelineMultisampleStateCreateInfo())

        val inputAssemblyInfo = arrayOf(
            VkPipelineInputAssemblyStateCreateInfo(
                topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST,
                primitiveRestartEnable = false,
            ),
        )

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

    fun bind(commandBuffer: Long) = bind(commandBuffer, frameIndex = 0)

    fun bind(commandBuffer: Long, frameIndex: Int) {
        val slot = uniformSlots[frameIndex]
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            graphicsPipeline[0],
        )
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, slot.descriptorSet)
    }

    fun destroy() {
        graphicsPipeline.forEach { Vulkan.vkDestroyPipeline(device, it) }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
        if (::uniformSlots.isInitialized) uniformSlots.destroy()
    }

    private companion object {
        const val MVP_UNIFORM_BYTES = 16 * Float.SIZE_BYTES
    }
}
