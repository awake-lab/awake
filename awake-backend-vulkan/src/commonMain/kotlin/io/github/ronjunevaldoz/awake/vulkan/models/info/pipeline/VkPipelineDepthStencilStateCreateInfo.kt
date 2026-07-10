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
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompareOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.models.VkStencilOpState

class VkPipelineDepthStencilStateCreateInfo(
    var sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO,
    var pNext: Any? = null,
    var flags: VkPipelineDepthStencilStateCreateFlags = 0,
    var depthTestEnable: VkBool32 = true,
    var depthWriteEnable: VkBool32 = true,
    var depthCompareOp: VkCompareOp = VkCompareOp.VK_COMPARE_OP_LESS_OR_EQUAL,
    var depthBoundsTestEnable: VkBool32 = false,
    var stencilTestEnable: VkBool32 = false,
    var front: VkStencilOpState = VkStencilOpState(),
    var back: VkStencilOpState = VkStencilOpState(),
    var minDepthBounds: Float = 0.0f,
    var maxDepthBounds: Float = 1.0f
)

typealias VkPipelineDepthStencilStateCreateFlags = VkFlags