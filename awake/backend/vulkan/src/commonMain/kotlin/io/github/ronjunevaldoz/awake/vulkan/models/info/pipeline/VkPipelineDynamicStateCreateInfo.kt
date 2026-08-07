// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineDynamicStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineDynamicStateCreateFlags = 0,
    @field:VkArray(sizeAlias = "dynamicStateCount")
    val pDynamicStates: Array<VkDynamicState> = emptyArray(),
)

typealias VkPipelineDynamicStateCreateFlags = VkFlags
