// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkBlendFactor
import io.github.ronjunevaldoz.awake.vulkan.enums.VkBlendOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkLogicOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineColorBlendAttachmentState(
    val blendEnable: VkBool32 = false,
    val srcColorBlendFactor: VkBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE,
    val dstColorBlendFactor: VkBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ZERO,
    val colorBlendOp: VkBlendOp = VkBlendOp.VK_BLEND_OP_ADD,
    val srcAlphaBlendFactor: VkBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE,
    val dstAlphaBlendFactor: VkBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ZERO,
    val alphaBlendOp: VkBlendOp = VkBlendOp.VK_BLEND_OP_ADD,
    val colorWriteMask: VkColorComponentFlags = VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT.value,
)

class VkPipelineColorBlendStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineColorBlendStateCreateFlags = 0,
    val logicOpEnable: VkBool32 = false,
    val logicOp: VkLogicOp = VkLogicOp.VK_LOGIC_OP_COPY,
    @field:VkArray("attachmentCount")
    val pAttachments: Array<VkPipelineColorBlendAttachmentState>? = null,
    @VkConstArray
    val blendConstants: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f),
)

typealias VkPipelineColorBlendStateCreateFlags = VkFlags
