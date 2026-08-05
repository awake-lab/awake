// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags


enum class VkDeviceQueueCreateFlagBits(val value: Int) {
    VK_DEVICE_QUEUE_CREATE_PROTECTED_BIT(0x00000001)
}

typealias VkDeviceQueueCreateFlags = VkFlags
