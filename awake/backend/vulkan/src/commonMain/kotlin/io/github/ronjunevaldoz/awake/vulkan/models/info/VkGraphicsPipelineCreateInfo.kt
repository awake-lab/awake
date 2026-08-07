// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.VkPointer
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineCreateFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDepthStencilStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDynamicStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineInputAssemblyStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineMultisampleStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineRasterizationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineShaderStageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineTessellationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineVertexInputStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineViewportStateCreateInfo

class VkGraphicsPipelineCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineCreateFlags = 0,
    @field:VkArray("stageCount")
    val pStages: Array<VkPipelineShaderStageCreateInfo> = emptyArray(),
    @VkPointer
    val pVertexInputState: Array<VkPipelineVertexInputStateCreateInfo> = arrayOf(
        VkPipelineVertexInputStateCreateInfo(),
    ),
    @VkPointer
    val pInputAssemblyState: Array<VkPipelineInputAssemblyStateCreateInfo> = arrayOf(
        VkPipelineInputAssemblyStateCreateInfo(),
    ),
    @VkPointer
    val pTessellationState: Array<VkPipelineTessellationStateCreateInfo> = arrayOf(
        VkPipelineTessellationStateCreateInfo(),
    ),
    @VkPointer
    val pViewportState: Array<VkPipelineViewportStateCreateInfo> = arrayOf(
        VkPipelineViewportStateCreateInfo(),
    ),
    @VkPointer
    val pRasterizationState: Array<VkPipelineRasterizationStateCreateInfo> = arrayOf(
        VkPipelineRasterizationStateCreateInfo(),
    ),
    @VkPointer
    val pMultisampleState: Array<VkPipelineMultisampleStateCreateInfo> = arrayOf(
        VkPipelineMultisampleStateCreateInfo(),
    ),
    @VkPointer
    val pDepthStencilState: Array<VkPipelineDepthStencilStateCreateInfo>? = null,
    @VkPointer
    val pColorBlendState: Array<VkPipelineColorBlendStateCreateInfo> = arrayOf(
        VkPipelineColorBlendStateCreateInfo(),
    ),
    @VkPointer
    val pDynamicState: Array<VkPipelineDynamicStateCreateInfo> = arrayOf(
        VkPipelineDynamicStateCreateInfo(),
    ),
    @field:VkHandleRef("VkPipelineLayout")
    val layout: VkHandle = 0, // VkPipelineLayout handle
    @field:VkHandleRef("VkRenderPass")
    val renderPass: VkHandle = 0, // VkRenderPass handle
    val subpass: Int = 0,
    @field:VkHandleRef("VkPipeline")
    val basePipelineHandle: VkHandle = 0, // VkPipeline handle
    val basePipelineIndex: Int = -1,
)
