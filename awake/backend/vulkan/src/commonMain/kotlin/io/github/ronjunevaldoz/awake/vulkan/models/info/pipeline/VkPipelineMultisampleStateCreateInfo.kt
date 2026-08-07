// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
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
    var alphaToOneEnable: VkBool32 = false, // Optional
)

typealias VkPipelineMultisampleStateCreateFlags = VkFlags
