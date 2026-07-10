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

package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSharingMode
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D

class VkSwapchainCreateInfoKHR(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
    val pNext: Any? = null, // You can use the appropriate type for pNext based on your requirements
    val flags: Int = 0,
    @field:VkHandleRef("VkSurfaceKHR")
    val surface: VkHandle = 0, //VkSurfaceKHR,
    val minImageCount: Int = 0,
    val imageFormat: VkFormat = VkFormat.VK_FORMAT_UNDEFINED,
    val imageColorSpace: VkColorSpaceKHR = VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR,
    val imageExtent: VkExtent2D = VkExtent2D(),
    val imageArrayLayers: Int = 0,
    val imageUsage: VkImageUsageFlags = 0,
    val imageSharingMode: VkSharingMode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE,
    @field:VkArray("queueFamilyIndexCount")
    val pQueueFamilyIndices: IntArray? = null, // Set it to null if it's optional
    val preTransform: VkSurfaceTransformFlagBitsKHR = VkSurfaceTransformFlagBitsKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR,
    val compositeAlpha: VkCompositeAlphaFlagBitsKHR = VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR,
    val presentMode: VkPresentModeKHR = VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR,
    val clipped: VkBool32 = false,
    @field:VkHandleRef("VkSwapchainKHR")
    val oldSwapchain: VkHandle = 0// VkSwapchainKHR
)