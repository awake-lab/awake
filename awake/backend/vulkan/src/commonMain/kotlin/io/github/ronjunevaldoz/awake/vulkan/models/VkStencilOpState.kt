// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompareOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStencilOp

class VkStencilOpState(
    var failOp: VkStencilOp = VkStencilOp.VK_STENCIL_OP_KEEP,
    var passOp: VkStencilOp = VkStencilOp.VK_STENCIL_OP_KEEP,
    var depthFailOp: VkStencilOp = VkStencilOp.VK_STENCIL_OP_KEEP,
    var compareOp: VkCompareOp = VkCompareOp.VK_COMPARE_OP_ALWAYS,
    var compareMask: Int = 0,
    var writeMask: Int = 0,
    var reference: Int = 0
)