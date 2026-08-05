// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkEnum

enum class VkCommandBufferResetFlagBits(override val value: Int) : VkEnum {
    VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT(0x00000001),
    VK_COMMAND_BUFFER_RESET_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkCommandBufferResetFlags = VkFlags
