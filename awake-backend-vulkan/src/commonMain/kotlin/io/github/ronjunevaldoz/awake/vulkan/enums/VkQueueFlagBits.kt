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

// Define the enum class VkQueueFlagBits
enum class VkQueueFlagBits(val value: Int) {
    VK_QUEUE_GRAPHICS_BIT(0x00000001),
    VK_QUEUE_COMPUTE_BIT(0x00000002),
    VK_QUEUE_TRANSFER_BIT(0x00000004),
    VK_QUEUE_SPARSE_BINDING_BIT(0x00000008),
    VK_QUEUE_PROTECTED_BIT(0x00000010),
    VK_QUEUE_VIDEO_DECODE_BIT_KHR(0x00000020),
    VK_QUEUE_VIDEO_ENCODE_BIT_KHR(0x00000040),
    VK_QUEUE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkQueueFlags = VkFlags