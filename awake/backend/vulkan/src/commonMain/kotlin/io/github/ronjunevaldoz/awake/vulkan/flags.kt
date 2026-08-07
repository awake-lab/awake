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

fun VkSampleCountFlagBits.toFlags(): VkSampleCountFlags = value

infix fun VkFlags.has(bit: VkEnum): Boolean = this and bit.value != 0

infix fun VkFlags.has(bit: VkSampleCountFlagBits): Boolean = this and bit.value != 0

infix fun VkFlags.has(bit: VkQueueFlagBits): Boolean = this and bit.value != 0

infix fun VkFlags.has(bit: VkCompositeAlphaFlagBitsKHR): Boolean = this and bit.value != 0

infix fun VkFlags.has(bit: VkImageUsageFlagBits): Boolean = this and bit.value != 0

infix fun VkFlags.has(bit: VkSurfaceTransformFlagBitsKHR): Boolean = this and bit.value != 0

fun VkSampleCountFlags.set(bit: VkSampleCountFlagBits): VkSampleCountFlags = this or bit.value

fun VkSampleCountFlags.clear(bit: VkSampleCountFlagBits): VkSampleCountFlags = this and bit.value.inv()
