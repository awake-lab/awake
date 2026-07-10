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