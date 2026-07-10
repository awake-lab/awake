/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
