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

package io.github.ronjunevaldoz.awake.vulkan.enums.flags

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

enum class VkDependencyFlagBits(val value: Int) {
    VK_DEPENDENCY_BY_REGION_BIT(0x00000001),
    VK_DEPENDENCY_DEVICE_GROUP_BIT(0x00000004),
    VK_DEPENDENCY_VIEW_LOCAL_BIT(0x00000002),
    VK_DEPENDENCY_VIEW_LOCAL_BIT_KHR(VK_DEPENDENCY_VIEW_LOCAL_BIT.value),
    VK_DEPENDENCY_DEVICE_GROUP_BIT_KHR(VK_DEPENDENCY_DEVICE_GROUP_BIT.value),
    VK_DEPENDENCY_FLAG_BITS_MAX_ENUM(0x7FFFFFFF)
}

typealias VkDependencyFlags = VkFlags