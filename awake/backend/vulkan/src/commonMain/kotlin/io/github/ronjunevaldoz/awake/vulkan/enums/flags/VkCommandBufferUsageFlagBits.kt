// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

import io.github.ronjunevaldoz.awake.vulkan.enums.VkEnum

enum class VkCommandBufferUsageFlagBits(override val value: Int) : VkEnum {
    VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT(0x00000001),
    VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT(0x00000002),
    VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT(0x00000004),
    VK_COMMAND_BUFFER_USAGE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkCommandBufferUsageFlags = Int