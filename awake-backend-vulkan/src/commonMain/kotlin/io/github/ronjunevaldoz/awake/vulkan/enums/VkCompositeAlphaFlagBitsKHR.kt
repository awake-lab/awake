// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkCompositeAlphaFlagBitsKHR(val value: Int) {
    VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR(0x00000001),
    VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR(0x00000002),
    VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR(0x00000004),
    VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR(0x00000008)
}

typealias VkCompositeAlphaFlagsKHR = VkFlags