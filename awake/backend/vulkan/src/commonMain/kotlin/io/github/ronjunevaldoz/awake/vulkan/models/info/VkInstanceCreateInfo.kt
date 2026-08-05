// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.VkPointer
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkInstanceCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkInstanceCreateFlags = 0,
    @VkPointer
    val pApplicationInfo: Array<VkApplicationInfo>? = null,
    @VkArray(sizeAlias = "enabledLayerCount")
    val ppEnabledLayerNames: Array<String>? = null,
    @VkArray(sizeAlias = "enabledExtensionCount")
    val ppEnabledExtensionNames: Array<String>? = null
)


// Representing VkInstanceCreateFlags as a typealias of Int
typealias VkInstanceCreateFlags = VkFlags
