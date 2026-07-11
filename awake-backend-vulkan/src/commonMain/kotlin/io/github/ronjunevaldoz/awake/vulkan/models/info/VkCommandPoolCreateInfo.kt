// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandPoolCreateFlags

data class VkCommandPoolCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkCommandPoolCreateFlags = 0,
    val queueFamilyIndex: Int = 0
)