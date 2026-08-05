// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkPhysicalDeviceType(val value: Int) {
    VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
    VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
    VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
    VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
    VK_PHYSICAL_DEVICE_TYPE_CPU(4)
}
