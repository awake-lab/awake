// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference

class VkSubpassDescription(
    val flags: VkSubpassDescriptionFlags = 0,
    val pipelineBindPoint: VkPipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
    @field:VkArray(sizeAlias = "inputAttachmentCount")
    val pInputAttachments: Array<VkAttachmentReference>? = null,
    @field:VkArray(sizeAlias = "colorAttachmentCount")
    val pColorAttachments: Array<VkAttachmentReference>? = null,
    val pResolveAttachments: Array<VkAttachmentReference>? = null,
    val pDepthStencilAttachment: Array<VkAttachmentReference>? = null,
    @field:VkArray(sizeAlias = "preserveAttachmentCount")
    val pPreserveAttachments: IntArray? = null
)

typealias VkSubpassDescriptionFlags = VkFlags