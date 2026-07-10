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

enum class VkComponentSwizzle(val value: Int) {
    VK_COMPONENT_SWIZZLE_IDENTITY(0),
    VK_COMPONENT_SWIZZLE_ZERO(1),
    VK_COMPONENT_SWIZZLE_ONE(2),
    VK_COMPONENT_SWIZZLE_R(3),
    VK_COMPONENT_SWIZZLE_G(4),
    VK_COMPONENT_SWIZZLE_B(5),
    VK_COMPONENT_SWIZZLE_A(6),
    VK_COMPONENT_SWIZZLE_MAX_ENUM(0x7FFFFFFF);

    companion object {
        // Helper function to convert integer value to VkComponentSwizzle
        fun fromInt(value: Int) = values().find { it.value == value }
    }
}
