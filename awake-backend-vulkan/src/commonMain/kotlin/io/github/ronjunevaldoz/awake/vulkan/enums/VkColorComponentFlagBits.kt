// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkColorComponentFlagBits(override val value: Int) : VkEnum {
    VK_COLOR_COMPONENT_R_BIT(0x00000001),
    VK_COLOR_COMPONENT_G_BIT(0x00000002),
    VK_COLOR_COMPONENT_B_BIT(0x00000004),
    VK_COLOR_COMPONENT_A_BIT(0x00000008),
    VK_COLOR_COMPONENT_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkColorComponentFlags = VkFlags