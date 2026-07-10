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
import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType


class VkPushConstantRange(
    val stageFlags: VkShaderStageFlags = 0,
    val offset: Int = 0,
    val size: Int = 0
)
typealias VkShaderStageFlags = VkFlags


class VkPipelineLayoutCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineLayoutCreateFlags = 0,
    @field:VkHandleRef("VkDescriptorSetLayout")
    @VkArray(sizeAlias = "setLayoutCount")
    val pSetLayouts: Array<VkDescriptorSetLayout>? = null, // Optional
    val pushConstantRangeCount: Int = 0, // Optional
    val pPushConstantRanges: Array<VkPushConstantRange>? = null, // Optional
)

typealias VkPipelineLayout = VkHandle
typealias VkDescriptorSetLayout = VkHandle
typealias VkPipelineLayoutCreateFlags = VkFlags