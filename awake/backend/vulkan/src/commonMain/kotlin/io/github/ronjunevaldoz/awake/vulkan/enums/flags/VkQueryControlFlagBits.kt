// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkEnum

enum class VkQueryControlFlagBits(override val value: Int) : VkEnum {
    VK_QUERY_CONTROL_PRECISE_BIT(0x00000001),
    VK_QUERY_CONTROL_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkQueryControlFlags = VkFlags
