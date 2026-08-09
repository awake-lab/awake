// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

data class VkCommandBufferAllocateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
    val pNext: Any? = null,
    @field:VkHandleRef("VkCommandPool")
    val commandPool: VkHandle = 0,
    val level: VkCommandBufferLevel = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
    val commandBufferCount: Int = 1,
)
