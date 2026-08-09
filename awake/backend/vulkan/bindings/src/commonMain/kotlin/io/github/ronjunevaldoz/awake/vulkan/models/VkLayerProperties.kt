// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import kotlin.jvm.JvmOverloads

@VkMutator
data class VkLayerProperties @JvmOverloads constructor(
    @VkConstArray("VK_MAX_EXTENSION_NAME_SIZE")
    val layerName: String = "",
    val specVersion: Int = 0,
    val implementationVersion: Int = 0,
    @VkConstArray("VK_MAX_DESCRIPTION_SIZE")
    val description: String = "",
)
