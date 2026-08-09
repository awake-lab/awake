// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import kotlin.jvm.JvmOverloads

@VkMutator
data class VkSurfaceFormatKHR @JvmOverloads constructor(
    val format: VkFormat = VkFormat.VK_FORMAT_UNDEFINED,
    val colorSpace: VkColorSpaceKHR = VkColorSpaceKHR.VK_COLORSPACE_SRGB_NONLINEAR_KHR,
)
