// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout

data class VkAttachmentReference(
    val attachment: Int = 0,
    val layout: VkImageLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
)