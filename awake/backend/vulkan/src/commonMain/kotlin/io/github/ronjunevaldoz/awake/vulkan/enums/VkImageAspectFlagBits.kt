// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkImageAspectFlagBits(val value: Int) {
    VK_IMAGE_ASPECT_COLOR_BIT(0x00000001),
    VK_IMAGE_ASPECT_DEPTH_BIT(0x00000002),
    VK_IMAGE_ASPECT_STENCIL_BIT(0x00000004),
    VK_IMAGE_ASPECT_METADATA_BIT(0x00000008),
    VK_IMAGE_ASPECT_PLANE_0_BIT(0x00000010),
    VK_IMAGE_ASPECT_PLANE_1_BIT(0x00000020),
    VK_IMAGE_ASPECT_PLANE_2_BIT(0x00000040),
    VK_IMAGE_ASPECT_MEMORY_PLANE_0_BIT_EXT(0x00000080),
    VK_IMAGE_ASPECT_MEMORY_PLANE_1_BIT_EXT(0x00000100),
    VK_IMAGE_ASPECT_MEMORY_PLANE_2_BIT_EXT(0x00000200),
    VK_IMAGE_ASPECT_MEMORY_PLANE_3_BIT_EXT(0x00000400),
    VK_IMAGE_ASPECT_NONE_KHR(0),
    VK_IMAGE_ASPECT_PLANE_0_BIT_KHR(VK_IMAGE_ASPECT_PLANE_0_BIT.value),
    VK_IMAGE_ASPECT_PLANE_1_BIT_KHR(VK_IMAGE_ASPECT_PLANE_1_BIT.value),
    VK_IMAGE_ASPECT_PLANE_2_BIT_KHR(VK_IMAGE_ASPECT_PLANE_2_BIT.value),
    VK_IMAGE_ASPECT_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

    companion object {
        // Helper function to convert integer value to VkImageAspectFlagBits
        fun fromInt(value: Int) = values().find { it.value == value }
    }
}

typealias VkImageAspectFlags = VkFlags
