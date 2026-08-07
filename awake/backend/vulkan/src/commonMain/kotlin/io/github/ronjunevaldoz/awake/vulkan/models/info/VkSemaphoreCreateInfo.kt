// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkSemaphoreCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkSemaphoreCreateFlags = 0,
)

typealias VkSemaphoreCreateFlags = VkFlags
