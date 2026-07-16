// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VK_SUBPASS_EXTERNAL
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFrontFace
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkAccessFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentDescription
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSubpassDependency
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubpassDescription
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
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * Phase 2 (renderer abstraction): owns the render pass + graphics pipeline -- extracted
 * verbatim from `VulkanApplication`'s `createRenderPass`/`createGraphicsPipeline`/
 * `createShaderModule` functions and their backing fields (`renderPass`, `pipelineLayout`,
 * `pipelineCache`, `graphicsPipeline`). Same two color/depth attachments, same fixed
 * position/color vertex attribute layout, same `NONE` cull mode (see the rasterization state's
 * comment for why) -- this is a structural move, not a behavior change.
 *
 * Takes compiled SPIR-V bytecode directly (`vertShaderCode`/`fragShaderCode`) rather than
 * loading it itself: reading a shader asset off disk is a platform/resource concern
 * (`readResourceBytes`), not a pipeline concern, matching how [Mesh][io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh]
 * takes raw vertex/index data instead of loading a model file itself.
 *
 * Takes [Material]'s [DescriptorSetLayoutHandle] (not the whole [Material] instance) because
 * that's the only thing the pipeline layout actually needs from it -- a real
 * `Material`/`Shader` system with more than one descriptor set layout would change this
 * signature, but there's only one material in this demo today.
 *
 * The vertex attribute layout (position vec3 + color vec3) is still hardcoded here. The
 * shared cube meshes currently keep an unused trailing uv vec2 in their interleaved stride so
 * Vulkan and WebGPU can stay aligned while the shared shader pipeline matures, but the active
 * sample shader only consumes locations 0 and 1. A real vertex-format abstraction (so
 * different meshes could use different layouts) is out of scope for this pass; only
 * [vertexStride] is parameterized so this class isn't hardcoded to the demo cube's specific
 * stride constant.
 */
class RenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    vertShaderCode: ByteArray,
    fragShaderCode: ByteArray,
    vertexStride: Int,
    vertexEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    fragmentEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT
) {
    private val graphicsDevice = graphicsDevice
    private val swapchainManager = swapchainManager
    private val device get() = graphicsDevice.device

    var renderPass: Long = 0
    var pipelineLayout: Long = 0
    var pipelineCache: Long = 0
    var graphicsPipeline: LongArray = longArrayOf()

    init {
        renderPass = createRenderPass()
        createGraphicsPipeline(
            descriptorSetLayout = descriptorSetLayout,
            vertShaderCode = vertShaderCode,
            fragShaderCode = fragShaderCode,
            vertexStride = vertexStride,
            vertexEntryPoint = vertexEntryPoint,
            fragmentEntryPoint = fragmentEntryPoint
        )
    }

    private fun createRenderPass(): Long {
        return Vulkan.vkCreateRenderPass(
            device,
            VkRenderPassCreateInfo(
                pAttachments = arrayOf(
                    VkAttachmentDescription(
                        format = swapchainManager.imageFormat,
                        initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                        // COLOR_ATTACHMENT_OPTIMAL, not PRESENT_SRC_KHR: the UI overlay pass
                        // (UiRenderPipeline) draws on top of this pass's output before the
                        // final present-layout transition happens, at the end of that pass
                        // instead. See docs/MVP_PLAN.md's custom-UI decision log entry.
                        finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                    ),
                    VkAttachmentDescription(
                        format = VkFormat.VK_FORMAT_D32_SFLOAT,
                        storeOp = VkAttachmentStoreOp.DONT_CARE,
                        initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                        finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                    )
                ),
                pSubpasses = arrayOf(
                    VkSubpassDescription(
                        pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                        pColorAttachments = arrayOf(
                            VkAttachmentReference(
                                attachment = 0,
                                layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                            )
                        ),
                        pDepthStencilAttachment = arrayOf(
                            VkAttachmentReference(
                                attachment = 1,
                                layout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                            )
                        )
                    )
                ),
                pDependencies = arrayOf(
                    VkSubpassDependency(
                        srcSubpass = VK_SUBPASS_EXTERNAL,
                        dstSubpass = 0,
                        srcStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                            VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                        srcAccessMask = 0,
                        dstStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                            VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                        dstAccessMask = VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT.value or
                            VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.value or
                            VkAccessFlagBits.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT.value,
                    )
                )
            )
        )
    }

    private fun createShaderModule(code: IntArray): Long {
        val createInfo = VkShaderModuleCreateInfo(pCode = code)
        return Vulkan.vkCreateShaderModule(device, createInfo)
    }

    private fun ByteArray.toIntArray(): IntArray {
        return IntArray(this.size / 4) { i ->
            (this[i * 4].toInt() and 0xFF) or
                ((this[i * 4 + 1].toInt() and 0xFF) shl 8) or
                ((this[i * 4 + 2].toInt() and 0xFF) shl 16) or
                ((this[i * 4 + 3].toInt() and 0xFF) shl 24)
        }
    }

    private fun createGraphicsPipeline(
        descriptorSetLayout: DescriptorSetLayoutHandle,
        vertShaderCode: ByteArray,
        fragShaderCode: ByteArray,
        vertexStride: Int,
        vertexEntryPoint: String,
        fragmentEntryPoint: String
    ) {
        // WARNING: make sure the .spv vulkan version match, this might cause out of memory
        val fragShaderModule = createShaderModule(fragShaderCode.toIntArray())
        val vertShaderModule = createShaderModule(vertShaderCode.toIntArray())

        // process shader
        val fragShaderStageInfo = VkPipelineShaderStageCreateInfo(
            stage = VkShaderStageFlagBits.FRAGMENT,
            module = fragShaderModule,
            pName = fragmentEntryPoint
        )
        val vertShaderStageInfo = VkPipelineShaderStageCreateInfo(
            stage = VkShaderStageFlagBits.VERTEX,
            module = vertShaderModule,
            pName = vertexEntryPoint
        )
        val shaderStages = arrayOf(fragShaderStageInfo, vertShaderStageInfo)

        val vertexInputInfo = arrayOf(
            VkPipelineVertexInputStateCreateInfo(
                pVertexBindingDescriptions = arrayOf(
                    VkVertexInputBindingDescription(
                        binding = 0,
                        stride = vertexStride,
                        inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX
                    )
                ),
                pVertexAttributeDescriptions = arrayOf(
                    VkVertexInputAttributeDescription(
                        location = 0,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT,
                        offset = 0
                    ),
                    VkVertexInputAttributeDescription(
                        location = 1,
                        binding = 0,
                        format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT,
                        offset = 3 * Float.SIZE_BYTES
                    ),
                )
            )
        )

        val dynamicInfo = arrayOf(
            VkPipelineDynamicStateCreateInfo(
                pDynamicStates = arrayOf(
                    VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT,
                    VkDynamicState.VK_DYNAMIC_STATE_SCISSOR,
                )
            )
        )

        val viewportInfo = arrayOf(
            VkPipelineViewportStateCreateInfo(
                pViewports = arrayOf(
                    VkViewport(
                        width = swapchainManager.extent.width.toFloat(),
                        height = swapchainManager.extent.height.toFloat(),
                    )
                ),
                pScissors = arrayOf(
                    VkRect2D(
                        offset = VkOffset2D(),
                        extent = swapchainManager.extent
                    )
                )
            )
        )

        val depthStencil = arrayOf(
            VkPipelineDepthStencilStateCreateInfo()
        )

        val multisamplingInfo = arrayOf(
            VkPipelineMultisampleStateCreateInfo()
        )
        // Specify we will use triangle lists to draw geometry.
        val inputAssemblyInfo = arrayOf(
            VkPipelineInputAssemblyStateCreateInfo(
                topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                primitiveRestartEnable = false
            )
        )
        // Specify rasterization state.
        val rasterizationInfo = arrayOf(
            VkPipelineRasterizationStateCreateInfo(
                // NONE, deliberately: the demo cube's index winding isn't guaranteed
                // outward-consistent per face -- depth testing alone resolves correct
                // occlusion regardless of triangle winding. Revisit once per-face vertex
                // duplication makes winding order meaningful.
                cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE,
                lineWidth = 1f
            )
        )

        val blendAttachment = VkPipelineColorBlendAttachmentState(
            blendEnable = false,
            colorWriteMask = VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT.value
        )

        val colorBlendInfo = arrayOf(
            VkPipelineColorBlendStateCreateInfo(
                pAttachments = arrayOf(blendAttachment)
            )
        )

        pipelineLayout = Vulkan.vkCreatePipelineLayout(
            device,
            VkPipelineLayoutCreateInfo(pSetLayouts = arrayOf(descriptorSetLayout.handle))
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
                basePipelineHandle = 0, // Optional
                basePipelineIndex = -1 // Optional
            )
        )
        pipelineCache = Vulkan.vkCreatePipelineCache(device, VkPipelineCacheCreateInfo())
        graphicsPipeline = Vulkan.vkCreateGraphicsPipelines(device, pipelineCache, createInfos)

        Vulkan.vkDestroyShaderModule(device, fragShaderModule)
        Vulkan.vkDestroyShaderModule(device, vertShaderModule)
    }

    /** Binds the (single) graphics pipeline. Render-pass begin/end and descriptor-set
     * binding (a [Material] concern) happen around/after this, not here. */
    fun bind(commandBuffer: Long) {
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            graphicsPipeline[0]
        )
    }

    fun destroy() {
        graphicsPipeline.forEach { pipeline ->
            Vulkan.vkDestroyPipeline(device, pipeline)
        }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyRenderPass(device, renderPass)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
    }

    private companion object {
        const val DEFAULT_SHADER_ENTRY_POINT = "main"
    }
}
