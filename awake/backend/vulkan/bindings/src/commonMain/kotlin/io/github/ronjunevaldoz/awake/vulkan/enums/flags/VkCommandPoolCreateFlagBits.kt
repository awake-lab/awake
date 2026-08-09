// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

enum class VkCommandPoolCreateFlagBits(val value: Int) {
    VK_COMMAND_POOL_CREATE_TRANSIENT_BIT(0x00000001),
    VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT(0x00000002),
    VK_COMMAND_POOL_CREATE_PROTECTED_BIT(0x00000004),
    VK_COMMAND_POOL_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF),
}

typealias VkCommandPoolCreateFlags = Int
