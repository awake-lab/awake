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

import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineShaderStageCreateFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineShaderStageCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineShaderStageCreateFlags = 0,
    val stage: VkShaderStageFlagBits = VkShaderStageFlagBits.VERTEX,
    @field:VkHandleRef("VkShaderModule")
    val module: VkHandle = 0, // VkShaderModule
    val pName: String? = null,
    val pSpecializationInfo: Array<VkSpecializationInfo>? = null
)

class VkSpecializationInfo(
    val mapEntryCount: Int = 0,
    val pMapEntries: Array<VkSpecializationMapEntry> = emptyArray(),
    val dataSize: Long = 0,
    val pData: Array<Any>? = null // Replace Any with the appropriate data type for pData
)

data class VkSpecializationMapEntry(
    val constantID: Int = 0,
    val offset: Int = 0,
    val size: Long = 0
)