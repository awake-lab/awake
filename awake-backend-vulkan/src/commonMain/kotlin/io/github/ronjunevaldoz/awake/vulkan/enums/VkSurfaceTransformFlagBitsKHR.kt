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

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

// Representing VkSurfaceTransformFlagBitsKHR as an enum class
enum class VkSurfaceTransformFlagBitsKHR(val value: Int) {
    VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR(0x00000001),
    VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR(0x00000002),
    VK_SURFACE_TRANSFORM_ROTATE_180_BIT_KHR(0x00000004),
    VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR(0x00000008),
    VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_BIT_KHR(0x00000010),
    VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_90_BIT_KHR(0x00000020),
    VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_180_BIT_KHR(0x00000040),
    VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_270_BIT_KHR(0x00000080),
    VK_SURFACE_TRANSFORM_INHERIT_BIT_KHR(0x00000100),
    VK_SURFACE_TRANSFORM_FLAG_BITS_MAX_ENUM_KHR(0x7FFFFFFF)
}

typealias VkSurfaceTransformFlagsKHR = VkFlags