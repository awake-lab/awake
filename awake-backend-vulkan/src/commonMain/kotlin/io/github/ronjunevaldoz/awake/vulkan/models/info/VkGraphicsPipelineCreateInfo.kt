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
        VkPipelineVertexInputStateCreateInfo()
    ),
    @VkPointer
    val pInputAssemblyState: Array<VkPipelineInputAssemblyStateCreateInfo> = arrayOf(
        VkPipelineInputAssemblyStateCreateInfo()
    ),
    @VkPointer
    val pTessellationState: Array<VkPipelineTessellationStateCreateInfo> = arrayOf(
        VkPipelineTessellationStateCreateInfo()
    ),
    @VkPointer
    val pViewportState: Array<VkPipelineViewportStateCreateInfo> = arrayOf(
        VkPipelineViewportStateCreateInfo()
    ),
    @VkPointer
    val pRasterizationState: Array<VkPipelineRasterizationStateCreateInfo> = arrayOf(
        VkPipelineRasterizationStateCreateInfo()
    ),
    @VkPointer
    val pMultisampleState: Array<VkPipelineMultisampleStateCreateInfo> = arrayOf(
        VkPipelineMultisampleStateCreateInfo()
    ),
    @VkPointer
    val pDepthStencilState: Array<VkPipelineDepthStencilStateCreateInfo>? = null,
    @VkPointer
    val pColorBlendState: Array<VkPipelineColorBlendStateCreateInfo> = arrayOf(
        VkPipelineColorBlendStateCreateInfo()
    ),
    @VkPointer
    val pDynamicState: Array<VkPipelineDynamicStateCreateInfo> = arrayOf(
        VkPipelineDynamicStateCreateInfo()
    ),
    @field:VkHandleRef("VkPipelineLayout")
    val layout: VkHandle = 0, // VkPipelineLayout handle
    @field:VkHandleRef("VkRenderPass")
    val renderPass: VkHandle = 0, // VkRenderPass handle
    val subpass: Int = 0,
    @field:VkHandleRef("VkPipeline")
    val basePipelineHandle: VkHandle = 0, // VkPipeline handle
    val basePipelineIndex: Int = -1
)