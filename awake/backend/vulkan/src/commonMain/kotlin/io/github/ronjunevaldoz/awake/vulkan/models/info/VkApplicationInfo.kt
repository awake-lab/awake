// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.Version
import io.github.ronjunevaldoz.awake.vulkan.Version.Companion.vkVersion
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

data class VkApplicationInfo(
    var sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO,
    var pNext: Any? = null,
    var pApplicationName: String,
    var applicationVersion: Int = Version(1, 0, 0).vkVersion,
    var pEngineName: String,
    var engineVersion: Int = Version(1, 0, 0).vkVersion,
    var apiVersion: Int = Version(1, 0, 0).vkVersion,
)
