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
    val pDependencies: Array<VkSubpassDependency>? = null
)

typealias VkRenderPassCreateFlags = VkFlags