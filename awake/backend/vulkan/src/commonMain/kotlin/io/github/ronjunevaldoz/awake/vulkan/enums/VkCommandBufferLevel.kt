// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkCommandBufferLevel(val value: Int) {
    VK_COMMAND_BUFFER_LEVEL_PRIMARY(0),
    VK_COMMAND_BUFFER_LEVEL_SECONDARY(1),
    VK_COMMAND_BUFFER_LEVEL_MAX_ENUM(0x7FFFFFFF)
}