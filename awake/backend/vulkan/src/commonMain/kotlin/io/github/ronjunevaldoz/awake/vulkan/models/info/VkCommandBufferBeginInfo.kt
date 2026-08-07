// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlags

class VkCommandBufferBeginInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO,
    val pNext: Any? = null,
    val flags: VkCommandBufferUsageFlags = 0,
    val pInheritanceInfo: Array<VkCommandBufferInheritanceInfo>? = null,
)
