// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkEnum
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkQueueFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR


fun VkSampleCountFlagBits.toFlags(): VkSampleCountFlags {
    return value
}

infix fun VkFlags.has(bit: VkEnum): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkSampleCountFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkQueueFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkCompositeAlphaFlagBitsKHR): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkImageUsageFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkSurfaceTransformFlagBitsKHR): Boolean {
    return this and bit.value != 0
}

fun VkSampleCountFlags.set(bit: VkSampleCountFlagBits): VkSampleCountFlags {
    return this or bit.value
}

fun VkSampleCountFlags.clear(bit: VkSampleCountFlagBits): VkSampleCountFlags {
    return this and bit.value.inv()
}