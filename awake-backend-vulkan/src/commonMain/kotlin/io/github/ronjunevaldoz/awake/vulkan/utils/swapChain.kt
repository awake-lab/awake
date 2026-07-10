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
import io.github.ronjunevaldoz.awake.vulkan.VulkanExtension
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceCapabilitiesKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceFormatKHR


data class SwapChainSupportDetails(
    val capabilities: VkSurfaceCapabilitiesKHR,
    val formats: List<VkSurfaceFormatKHR>,
    val presentModes: List<VkPresentModeKHR>,
)

fun isSwapChainSupported(physicalDevice: Long, surface: Long): Boolean {
    var swapChainAdequate = false
    // verify swap chain extension supported
    if (isDeviceExtSupported(physicalDevice, VulkanExtension.VK_KHR_SWAPCHAIN)) {
        val swapChainSupport = querySwapChainSupport(physicalDevice, surface)
        swapChainAdequate =
            swapChainSupport.formats.isNotEmpty() && swapChainSupport.presentModes.isNotEmpty()
    }
    return swapChainAdequate
}


fun querySwapChainSupport(physicalDevice: Long, surface: Long): SwapChainSupportDetails {
    val capabilities =
        Vulkan.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface)
    val formats = Vulkan.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface)
    val presentModes =
        Vulkan.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface)

    return SwapChainSupportDetails(
        capabilities,
        formats.toList(),
        presentModes.toList()
    )
}