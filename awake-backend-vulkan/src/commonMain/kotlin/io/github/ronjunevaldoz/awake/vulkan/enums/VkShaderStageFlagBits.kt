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

package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkShaderStageFlagBits(val value: Int) {
    VERTEX(0x00000001),
    TESSELLATION_CONTROL(0x00000002),
    TESSELLATION_EVALUATION(0x00000004),
    GEOMETRY(0x00000008),
    FRAGMENT(0x00000010),
    COMPUTE(0x00000020),
    ALL_GRAPHICS(0x0000001F),
    ALL(0x7FFFFFFF),
    RAYGEN_KHR(0x00000100),
    ANY_HIT_KHR(0x00000200),
    CLOSEST_HIT_KHR(0x00000400),
    MISS_KHR(0x00000800),
    INTERSECTION_KHR(0x00001000),
    CALLABLE_KHR(0x00002000),
    TASK_NV(0x00000040),
    MESH_NV(0x00000080),
    SUBPASS_SHADING_HUAWEI(0x00004000),
    RAYGEN_NV(RAYGEN_KHR.value),
    ANY_HIT_NV(ANY_HIT_KHR.value),
    CLOSEST_HIT_NV(CLOSEST_HIT_KHR.value),
    MISS_NV(MISS_KHR.value),
    INTERSECTION_NV(INTERSECTION_KHR.value),
    CALLABLE_NV(CALLABLE_KHR.value);

    companion object {
        fun fromValue(value: Int) =
            values().find { it.value == value }
                ?: throw IllegalArgumentException("Unknown VkShaderStageFlagBits value: $value")
    }
}
