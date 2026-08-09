// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkQueueFlags
import kotlin.jvm.JvmOverloads

@VkMutator
data class VkQueueFamilyProperties @JvmOverloads constructor(
    val queueFlags: VkQueueFlags = 0,
    val queueCount: UInt = 0u,
    val timestampValidBits: UInt = 0u,
    val minImageTransferGranularity: VkExtent3D = VkExtent3D(),
)
