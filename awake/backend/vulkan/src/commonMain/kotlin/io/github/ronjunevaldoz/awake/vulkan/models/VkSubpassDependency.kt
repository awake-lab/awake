// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkAccessFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDependencyFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlags

data class VkSubpassDependency(
    val srcSubpass: Int = 0,
    val dstSubpass: Int = 0,
    val srcStageMask: VkPipelineStageFlags = 0,
    val dstStageMask: VkPipelineStageFlags = 0,
    val srcAccessMask: VkAccessFlags = 0,
    val dstAccessMask: VkAccessFlags = 0,
    val dependencyFlags: VkDependencyFlags = 0
)