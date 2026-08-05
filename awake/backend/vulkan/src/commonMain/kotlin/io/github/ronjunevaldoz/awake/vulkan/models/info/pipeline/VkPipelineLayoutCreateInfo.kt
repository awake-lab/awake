// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType


class VkPushConstantRange(
    val stageFlags: VkShaderStageFlags = 0,
    val offset: Int = 0,
    val size: Int = 0
)
typealias VkShaderStageFlags = VkFlags


class VkPipelineLayoutCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineLayoutCreateFlags = 0,
    @field:VkHandleRef("VkDescriptorSetLayout")
    @VkArray(sizeAlias = "setLayoutCount")
    val pSetLayouts: Array<VkDescriptorSetLayout>? = null, // Optional
    val pushConstantRangeCount: Int = 0, // Optional
    val pPushConstantRanges: Array<VkPushConstantRange>? = null, // Optional
)

typealias VkPipelineLayout = VkHandle
typealias VkDescriptorSetLayout = VkHandle
typealias VkPipelineLayoutCreateFlags = VkFlags
