/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    val pSignalSemaphores: Array<VkHandle>? = null
)