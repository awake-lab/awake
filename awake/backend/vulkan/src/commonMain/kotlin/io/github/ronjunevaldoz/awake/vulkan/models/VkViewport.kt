// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

data class VkViewport(
    val x: Float = 0.0f,
    val y: Float = 0.0f,
    val width: Float = 0.0f,
    val height: Float = 0.0f,
    val minDepth: Float = 0.0f,
    val maxDepth: Float = 1.0f
)