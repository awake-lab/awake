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

package io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate

data class VkVertexInputBindingDescription(
    val binding: Int,
    val stride: Int,
    val inputRate: VkVertexInputRate
)

data class VkVertexInputAttributeDescription(
    val location: Int,
    val binding: Int,
    val format: VkFormat,
    val offset: Int
)

class VkPipelineVertexInputStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineVertexInputStateCreateFlags = 0,
    @VkArray(sizeAlias = "vertexBindingDescriptionCount")
    val pVertexBindingDescriptions: Array<VkVertexInputBindingDescription>? = null,
    @VkArray(sizeAlias = "vertexAttributeDescriptionCount")
    val pVertexAttributeDescriptions: Array<VkVertexInputAttributeDescription>? = null,
)


typealias VkPipelineVertexInputStateCreateFlags = VkFlags