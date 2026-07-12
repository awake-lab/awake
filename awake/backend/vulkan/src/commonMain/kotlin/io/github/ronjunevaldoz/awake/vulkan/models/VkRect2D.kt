// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

data class VkRect2D(
    val offset: VkOffset2D = VkOffset2D(),
    val extent: VkExtent2D = VkExtent2D()
)