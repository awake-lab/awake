// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

enum class VkFenceCreateFlagBits(val value: Int) {
    VK_FENCE_CREATE_SIGNALED_BIT(0x00000001),
    VK_FENCE_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF),
}