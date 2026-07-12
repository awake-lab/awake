// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.utils

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkQueueFlagBits
import io.github.ronjunevaldoz.awake.vulkan.has


data class QueueFamilyIndices(
    var graphicsFamily: Int? = null,
    var presentFamily: Int? = null
) {
    fun isComplete(): Boolean {
        return graphicsFamily != null && presentFamily != null
    }
}


fun findQueueFamilies(physicalDevice: Long, surface: Long): QueueFamilyIndices {
    val queueFamilyProperties =
        Vulkan.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice)
    val indices = QueueFamilyIndices()
    queueFamilyProperties.forEachIndexed { index, queueFamily ->
        if (queueFamily.queueFlags has VkQueueFlagBits.VK_QUEUE_GRAPHICS_BIT) {
            indices.graphicsFamily = index
        }
        if (Vulkan.vkGetPhysicalDeviceSurfaceSupportKHR(
                physicalDevice,
                index,
                surface
            )
        ) {
            indices.presentFamily = index
        }
    }
    return indices
}
