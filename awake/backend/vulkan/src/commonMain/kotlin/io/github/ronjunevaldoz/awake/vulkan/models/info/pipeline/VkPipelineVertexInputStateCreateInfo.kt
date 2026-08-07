// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate

data class VkVertexInputBindingDescription(
    val binding: Int,
    val stride: Int,
    val inputRate: VkVertexInputRate,
)

data class VkVertexInputAttributeDescription(
    val location: Int,
    val binding: Int,
    val format: VkFormat,
    val offset: Int,
)

class VkPipelineVertexInputStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineVertexInputStateCreateFlags = 0,
    @VkArray(sizeAlias = "vertexBindingDescriptionCount")
    val pVertexBindingDescriptions: Array<VkVertexInputBindingDescription>? = null,
    @VkArray(sizeAlias = "vertexAttributeDescriptionCount")
    val pVertexAttributeDescriptions: Array<VkVertexInputAttributeDescription>? = null,
)

typealias VkPipelineVertexInputStateCreateFlags = VkFlags
