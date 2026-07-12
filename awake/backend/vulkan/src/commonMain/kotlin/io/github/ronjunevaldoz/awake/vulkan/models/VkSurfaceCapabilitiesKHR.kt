// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagsKHR
import kotlin.jvm.JvmOverloads

@VkMutator
data class VkSurfaceCapabilitiesKHR @JvmOverloads constructor(
    val minImageCount: Int = 0,
    val maxImageCount: Int = 0,
    val currentExtent: VkExtent2D = VkExtent2D(),
    val minImageExtent: VkExtent2D = VkExtent2D(),
    val maxImageExtent: VkExtent2D = VkExtent2D(),
    val maxImageArrayLayers: Int = 0,
    val supportedTransforms: VkSurfaceTransformFlagsKHR = 0,
    val currentTransform: VkSurfaceTransformFlagBitsKHR = VkSurfaceTransformFlagBitsKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR,
    val supportedCompositeAlpha: VkCompositeAlphaFlagsKHR = 0,
    val supportedUsageFlags: VkImageUsageFlags = 0
)