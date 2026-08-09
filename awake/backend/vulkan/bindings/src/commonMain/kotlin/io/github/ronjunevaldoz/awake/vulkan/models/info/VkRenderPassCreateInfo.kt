// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentDescription
import io.github.ronjunevaldoz.awake.vulkan.models.VkSubpassDependency

class VkRenderPassCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkRenderPassCreateFlags = 0,
    @VkArray(sizeAlias = "attachmentCount")
    val pAttachments: Array<VkAttachmentDescription>? = null,
    @VkArray(sizeAlias = "subpassCount")
    val pSubpasses: Array<VkSubpassDescription>? = null,
    @VkArray(sizeAlias = "dependencyCount")
    val pDependencies: Array<VkSubpassDependency>? = null,
)

typealias VkRenderPassCreateFlags = VkFlags
