// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkDependencyFlagBits(val value: Int) {
    VK_DEPENDENCY_BY_REGION_BIT(0x00000001),
    VK_DEPENDENCY_DEVICE_GROUP_BIT(0x00000004),
    VK_DEPENDENCY_VIEW_LOCAL_BIT(0x00000002),
    VK_DEPENDENCY_VIEW_LOCAL_BIT_KHR(VK_DEPENDENCY_VIEW_LOCAL_BIT.value),
    VK_DEPENDENCY_DEVICE_GROUP_BIT_KHR(VK_DEPENDENCY_DEVICE_GROUP_BIT.value),
    VK_DEPENDENCY_FLAG_BITS_MAX_ENUM(0x7FFFFFFF),
}

typealias VkDependencyFlags = VkFlags
