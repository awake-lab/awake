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

enum class VkPipelineBindPoint(val value: Int) {
    VK_PIPELINE_BIND_POINT_GRAPHICS(0),
    VK_PIPELINE_BIND_POINT_COMPUTE(1),
    VK_PIPELINE_BIND_POINT_RAY_TRACING_KHR(1000165000),
    VK_PIPELINE_BIND_POINT_SUBPASS_SHADING_HUAWEI(1000369003),
    VK_PIPELINE_BIND_POINT_RAY_TRACING_NV(VK_PIPELINE_BIND_POINT_RAY_TRACING_KHR.value),
    VK_PIPELINE_BIND_POINT_MAX_ENUM(0x7FFFFFFF)
}