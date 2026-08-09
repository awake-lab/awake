// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport

class VkPipelineViewportStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineViewportStateCreateFlags = 0,
    @VkArray(sizeAlias = "viewportCount")
    val pViewports: Array<VkViewport>? = null,
    @VkArray(sizeAlias = "scissorCount")
    val pScissors: Array<VkRect2D>? = null,
)

typealias VkPipelineViewportStateCreateFlags = VkFlags
