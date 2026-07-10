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

import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFrontFace
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPolygonMode
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineRasterizationStateCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO,
    val pNext: Any? = null,
    val flags: VkPipelineRasterizationStateCreateFlags = 0,
    val depthClampEnable: VkBool32 = false,
    val rasterizerDiscardEnable: VkBool32 = false,
    val polygonMode: VkPolygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL,
    val cullMode: VkCullModeFlags = VkCullModeFlagBits.VK_CULL_MODE_BACK_BIT.value,
    val frontFace: VkFrontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE,
    val depthBiasEnable: VkBool32 = false,
    val depthBiasConstantFactor: Float = 0.0f,
    val depthBiasClamp: Float = 0.0f,
    val depthBiasSlopeFactor: Float = 0.0f,
    val lineWidth: Float = 1.0f
)

typealias VkPipelineRasterizationStateCreateFlags = VkFlags