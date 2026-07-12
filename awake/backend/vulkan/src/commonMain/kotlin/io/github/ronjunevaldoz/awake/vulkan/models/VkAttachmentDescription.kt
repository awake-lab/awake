// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentLoadOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlagBits

data class VkAttachmentDescription(
    val flags: VkAttachmentDescriptionFlags = 0,
    val format: VkFormat = VkFormat.VK_FORMAT_UNDEFINED,
    val samples: VkSampleCountFlagBits = VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT,
    val loadOp: VkAttachmentLoadOp = VkAttachmentLoadOp.CLEAR,
    val storeOp: VkAttachmentStoreOp = VkAttachmentStoreOp.STORE,
    val stencilLoadOp: VkAttachmentLoadOp = VkAttachmentLoadOp.DONT_CARE,
    val stencilStoreOp: VkAttachmentStoreOp = VkAttachmentStoreOp.DONT_CARE,
    val initialLayout: VkImageLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
    val finalLayout: VkImageLayout = VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
)

typealias VkAttachmentDescriptionFlags = VkFlags


