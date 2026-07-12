// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
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