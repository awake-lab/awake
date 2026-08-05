// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkShaderModuleCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkShaderModuleCreateFlags = 0,
    @VkArray(sizeAlias = "codeSize", stride = UInt::class)
    val pCode: IntArray
)

typealias VkShaderModuleCreateFlags = VkFlags
