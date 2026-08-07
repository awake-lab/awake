// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

// Define the enum class VkQueueFlagBits
enum class VkQueueFlagBits(val value: Int) {
    VK_QUEUE_GRAPHICS_BIT(0x00000001),
    VK_QUEUE_COMPUTE_BIT(0x00000002),
    VK_QUEUE_TRANSFER_BIT(0x00000004),
    VK_QUEUE_SPARSE_BINDING_BIT(0x00000008),
    VK_QUEUE_PROTECTED_BIT(0x00000010),
    VK_QUEUE_VIDEO_DECODE_BIT_KHR(0x00000020),
    VK_QUEUE_VIDEO_ENCODE_BIT_KHR(0x00000040),
    VK_QUEUE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF),
}

typealias VkQueueFlags = VkFlags
