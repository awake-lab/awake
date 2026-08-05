// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.debug

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkObjectType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import kotlin.jvm.JvmOverloads


@VkMutator
data class VkDebugUtilsObjectNameInfoEXT @JvmOverloads constructor(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_OBJECT_NAME_INFO_EXT,
    val pNext: Any? = null,
    val objectType: VkObjectType = VkObjectType.VK_OBJECT_TYPE_UNKNOWN,
    val objectHandle: Long = 0,
    val pObjectName: String? = null
)
