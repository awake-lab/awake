// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineTessellationStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineTessellationStateCreateFlags = 0,
    val patchControlPoints: Int = 0
)

typealias VkPipelineTessellationStateCreateFlags = VkFlags