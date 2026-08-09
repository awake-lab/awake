// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkImageViewCreateFlagBits(val value: Int) {
    VK_IMAGE_VIEW_CREATE_FRAGMENT_DENSITY_MAP_DYNAMIC_BIT_EXT(0x00000001),
    VK_IMAGE_VIEW_CREATE_FRAGMENT_DENSITY_MAP_DEFERRED_BIT_EXT(0x00000002),
    VK_IMAGE_VIEW_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF),
}

typealias VkImageViewCreateFlags = VkFlags
