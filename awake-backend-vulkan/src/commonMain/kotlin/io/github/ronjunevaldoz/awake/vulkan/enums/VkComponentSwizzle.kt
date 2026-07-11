// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkComponentSwizzle(val value: Int) {
    VK_COMPONENT_SWIZZLE_IDENTITY(0),
    VK_COMPONENT_SWIZZLE_ZERO(1),
    VK_COMPONENT_SWIZZLE_ONE(2),
    VK_COMPONENT_SWIZZLE_R(3),
    VK_COMPONENT_SWIZZLE_G(4),
    VK_COMPONENT_SWIZZLE_B(5),
    VK_COMPONENT_SWIZZLE_A(6),
    VK_COMPONENT_SWIZZLE_MAX_ENUM(0x7FFFFFFF);

    companion object {
        // Helper function to convert integer value to VkComponentSwizzle
        fun fromInt(value: Int) = values().find { it.value == value }
    }
}
