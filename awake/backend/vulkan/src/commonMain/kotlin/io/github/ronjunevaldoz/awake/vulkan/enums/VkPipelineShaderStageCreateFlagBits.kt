// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkPipelineShaderStageCreateFlagBits(val value: Int) {
    ALLOW_VARYING_SUBGROUP_SIZE(0x00000001),
    REQUIRE_FULL_SUBGROUPS(0x00000002),
    ALLOW_VARYING_SUBGROUP_SIZE_EXT(ALLOW_VARYING_SUBGROUP_SIZE.value),
    REQUIRE_FULL_SUBGROUPS_EXT(REQUIRE_FULL_SUBGROUPS.value),
    ;

    companion object {
        fun fromValue(value: Int) =
            values().find { it.value == value }
                ?: throw IllegalArgumentException("Unknown VkPipelineShaderStageCreateFlagBits value: $value")
    }
}

typealias VkPipelineShaderStageCreateFlags = VkFlags // <VkPipelineShaderStageCreateFlagBits>
