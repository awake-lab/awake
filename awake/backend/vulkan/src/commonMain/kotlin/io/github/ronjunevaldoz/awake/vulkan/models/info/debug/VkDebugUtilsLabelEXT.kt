// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.debug

import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import kotlin.jvm.JvmOverloads

@VkMutator
class VkDebugUtilsLabelEXT @JvmOverloads constructor(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_LABEL_EXT,
    val pNext: Any? = null,
    val pLabelName: String? = null,
    @VkConstArray(arraySize = "4")
    val color: FloatArray = FloatArray(4),
)
