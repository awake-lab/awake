// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums.flags

// Plain Int constants (bitmask), not an enum class -- consistent with how other
// Vulkan *FlagBits are modeled when their real values aren't sequential ordinals.
object VkMemoryPropertyFlagBits {
    const val VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT = 0x00000001
    const val VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT = 0x00000002
    const val VK_MEMORY_PROPERTY_HOST_COHERENT_BIT = 0x00000004
    const val VK_MEMORY_PROPERTY_HOST_CACHED_BIT = 0x00000008
    const val VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT = 0x00000010
}
