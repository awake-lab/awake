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
import io.github.ronjunevaldoz.awake.vulkan.VkPointer
import io.github.ronjunevaldoz.awake.vulkan.VkSampleMask
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

class VkPipelineMultisampleStateCreateInfo(
    var sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO,
    var pNext: Any? = null,
    var flags: VkPipelineMultisampleStateCreateFlags = 0,
    var rasterizationSamples: VkSampleCountFlagBits = VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT,
    var sampleShadingEnable: VkBool32 = false,
    var minSampleShading: Float = 1.0f, // Optional
    @VkPointer
    var pSampleMask: VkSampleMask = 0, // Optional
    var alphaToCoverageEnable: VkBool32 = false, // Optional
    var alphaToOneEnable: VkBool32 = false // Optional
)

typealias VkPipelineMultisampleStateCreateFlags = VkFlags