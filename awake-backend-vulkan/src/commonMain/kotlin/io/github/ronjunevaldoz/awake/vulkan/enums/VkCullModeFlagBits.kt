// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkCullModeFlagBits(val value: Int) {
    VK_CULL_MODE_NONE(0),
    VK_CULL_MODE_FRONT_BIT(0x00000001),
    VK_CULL_MODE_BACK_BIT(0x00000002),
    VK_CULL_MODE_FRONT_AND_BACK(0x00000003),
    VK_CULL_MODE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkCullModeFlags = VkFlags