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

enum class VkBlendFactor(val value: Int) {
    VK_BLEND_FACTOR_ZERO(0),
    VK_BLEND_FACTOR_ONE(1),
    VK_BLEND_FACTOR_SRC_COLOR(2),
    VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR(3),
    VK_BLEND_FACTOR_DST_COLOR(4),
    VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR(5),
    VK_BLEND_FACTOR_SRC_ALPHA(6),
    VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA(7),
    VK_BLEND_FACTOR_DST_ALPHA(8),
    VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA(9),
    VK_BLEND_FACTOR_CONSTANT_COLOR(10),
    VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR(11),
    VK_BLEND_FACTOR_CONSTANT_ALPHA(12),
    VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA(13),
    VK_BLEND_FACTOR_SRC_ALPHA_SATURATE(14),
    VK_BLEND_FACTOR_SRC1_COLOR(15),
    VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR(16),
    VK_BLEND_FACTOR_SRC1_ALPHA(17),
    VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA(18)
}

