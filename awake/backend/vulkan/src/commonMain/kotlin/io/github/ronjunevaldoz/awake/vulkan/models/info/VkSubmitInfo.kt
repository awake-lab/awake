// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkSubmitInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_SUBMIT_INFO,
    val pNext: Any? = null,
    @field:VkHandleRef("VkSemaphore")
    @field:VkArray("waitSemaphoreCount")
    val pWaitSemaphores: Array<VkHandle>? = null,
    val pWaitDstStageMask: IntArray? = null,
    @field:VkHandleRef("VkCommandBuffer")
    @field:VkArray("commandBufferCount")
    val pCommandBuffers: Array<VkHandle>? = null,
    @field:VkHandleRef("VkSemaphore")
    @field:VkArray("signalSemaphoreCount")
    val pSignalSemaphores: Array<VkHandle>? = null,
)
