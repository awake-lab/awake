// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D

class VkRenderPassBeginInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO,
    val pNext: Any? = null,
    @field:VkHandleRef("VkRenderPass")
    val renderPass: VkHandle = 0,
    @field:VkHandleRef("VkFramebuffer")
    val framebuffer: VkHandle = 0,
    val renderArea: VkRect2D = VkRect2D(),
    @VkArray("clearValueCount")
    val pClearValues: Array<VkClearValue>? = null
)