// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
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

/**
 * The shadow depth pre-pass's graphics pipeline -- a colorless twin of [RenderPipeline]: same
 * vertex layout as the primary lit pipeline (so it draws the exact same meshes without a
 * second vertex buffer) and the SAME [descriptorSetLayout] ([Material][io.github.ronjunevaldoz.awake.vulkan.material.Material]'s
 * shared one), so its pipeline layout is binding-compatible with the per-draw-call descriptor
 * set the main pass already writes -- the shadow vertex shader just reads `lightMvp` out of
 * that same uniform buffer (see `shadow_depth.wgsl`). No pixel shader output, no color
 * attachment, no color-blend state: [ShadowMap]'s render pass has depth only.
 *
 * `cullMode = NONE` (not front-face culling): this demo's cube winding isn't guaranteed
 * outward-consistent per face (see [RenderPipeline]'s own rasterization-state comment), so
 * culling either winding risks punching holes in the shadow caster instead of fixing acne.
 * Acne mitigation here is the fragment-side constant bias in `lit_shadow.wgsl` instead.
 */
class ShadowRenderPipeline(
    graphicsDevice: GraphicsDevice,
    renderPass: Long,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    shaders: ShaderPair,
    vertexFormat: VertexFormat,
    mapSize: Int,
    vertexEntryPoint: String = "main",
    fragmentEntryPoint: String = "main",
) {
    private val device = graphicsDevice.device

    var pipelineLayout: Long = 0
        private set
    private var pipelineCache: Long = 0
    private var graphicsPipeline: LongArray = longArrayOf()

    // See RenderPipeline's own init doc comment -- same partial-creation-leak guard.
    init {
        try {
            val fragShaderModule = createShaderModule(device, shaders.fragment.toShaderIntArray())
            val vertShaderModule = createShaderModule(device, shaders.vertex.toShaderIntArray())

            val shaderStages = arrayOf(
                VkPipelineShaderStageCreateInfo(
                    stage = VkShaderStageFlagBits.FRAGMENT,
                    module = fragShaderModule,
                    pName = fragmentEntryPoint,
                ),
                VkPipelineShaderStageCreateInfo(
                    stage = VkShaderStageFlagBits.VERTEX,
                    module = vertShaderModule,
                    pName = vertexEntryPoint,
                ),
            )

            val vertexInputInfo = arrayOf(
                VkPipelineVertexInputStateCreateInfo(
                    pVertexBindingDescriptions = arrayOf(
                        VkVertexInputBindingDescription(
                            binding = 0,
                            stride = vertexFormat.strideBytes,
                            inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX,
                        ),
                    ),
                    pVertexAttributeDescriptions = vertexFormat.entries.map { entry ->
                        VkVertexInputAttributeDescription(
                            location = entry.attribute.location,
                            binding = 0,
                            format = entry.attribute.format.toVkFormat(),
                            offset = entry.offsetBytes,
                        )
                    }.toTypedArray(),
                ),
            )

            val dynamicInfo = arrayOf(
                VkPipelineDynamicStateCreateInfo(
                    pDynamicStates = arrayOf(VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT, VkDynamicState.VK_DYNAMIC_STATE_SCISSOR),
                ),
            )

            val viewportInfo = arrayOf(
                VkPipelineViewportStateCreateInfo(
                    pViewports = arrayOf(VkViewport(width = mapSize.toFloat(), height = mapSize.toFloat())),
                    pScissors = arrayOf(VkRect2D(offset = VkOffset2D(), extent = VkExtent2D(mapSize, mapSize))),
                ),
            )

            val depthStencil = arrayOf(VkPipelineDepthStencilStateCreateInfo())
            val multisamplingInfo = arrayOf(VkPipelineMultisampleStateCreateInfo())
            val inputAssemblyInfo = arrayOf(
                VkPipelineInputAssemblyStateCreateInfo(
                    topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                    primitiveRestartEnable = false,
                ),
            )
            val rasterizationInfo = arrayOf(
                VkPipelineRasterizationStateCreateInfo(
                    cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                    lineWidth = 1f,
                ),
            )
            // No color attachments in ShadowMap's render pass, so no blend attachments either.
            val colorBlendInfo = arrayOf(VkPipelineColorBlendStateCreateInfo(pAttachments = arrayOf()))

            pipelineLayout = Vulkan.vkCreatePipelineLayout(
                device,
                VkPipelineLayoutCreateInfo(pSetLayouts = arrayOf(descriptorSetLayout.handle)),
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
        } catch (e: Throwable) {
            destroy()
            throw e
        }
    }

    fun bind(commandBuffer: Long) {
        Vulkan.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline[0])
    }

    fun destroy() {
        graphicsPipeline.forEach { Vulkan.vkDestroyPipeline(device, it) }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
    }
}

private fun GpuDataShape.toVkFormat(): VkFormat = when (this) {
    GpuDataShape.Float -> VkFormat.VK_FORMAT_R32_SFLOAT
    GpuDataShape.Vec2 -> VkFormat.VK_FORMAT_R32G32_SFLOAT
    GpuDataShape.Vec3 -> VkFormat.VK_FORMAT_R32G32B32_SFLOAT
    GpuDataShape.Vec4 -> VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT
    GpuDataShape.UInt4 -> VkFormat.VK_FORMAT_R32G32B32A32_UINT
    GpuDataShape.Mat4 -> error("Mat4 is not a valid vertex-attribute format.")
}
