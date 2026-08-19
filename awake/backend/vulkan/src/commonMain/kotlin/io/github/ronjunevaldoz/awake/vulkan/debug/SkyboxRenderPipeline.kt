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
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.createShaderModule
import io.github.ronjunevaldoz.awake.vulkan.pipeline.toShaderIntArray
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
                graphicsDevice,
                UNIFORM_BYTES,
                VkShaderStageFlagBits.VERTEX.value or VkShaderStageFlagBits.FRAGMENT.value,
                framesInFlight,
            )
            createGraphicsPipeline(shaders.vertex, shaders.fragment)
        } catch (e: Throwable) {
            destroy()
            throw e
        }
    }

    /** [uniforms] must be exactly [UNIFORM_FLOATS] long, in `skybox.wgsl`'s field order. */
    fun writeUniforms(frameIndex: Int, uniforms: FloatArray) {
        require(uniforms.size == UNIFORM_FLOATS) {
            "Skybox uniform block must be $UNIFORM_FLOATS floats, got ${uniforms.size}."
        }
        uniformSlots.write(frameIndex, uniforms)
    }

    private fun createGraphicsPipeline(vertShaderCode: ByteArray, fragShaderCode: ByteArray) {
        val fragShaderModule = createShaderModule(device, fragShaderCode.toShaderIntArray())
        val vertShaderModule = createShaderModule(device, vertShaderCode.toShaderIntArray())

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
        val slot = uniformSlots[frameIndex]
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
        if (::uniformSlots.isInitialized) uniformSlots.destroy()
    }

    companion object {
        /** inverseViewProjection(16) + cameraEye(4) + sunDirection(4) + horizon/zenith/sun/moon
         * colors(4 each) -- `skybox.wgsl`'s Uniforms, in order. */
        const val UNIFORM_FLOATS = 40
        private const val UNIFORM_BYTES = UNIFORM_FLOATS * Float.SIZE_BYTES
        private const val FULLSCREEN_TRIANGLE_VERTICES = 3
    }
}
