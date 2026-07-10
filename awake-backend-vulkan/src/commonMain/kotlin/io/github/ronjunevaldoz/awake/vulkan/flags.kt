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

package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkEnum
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkQueueFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR


fun VkSampleCountFlagBits.toFlags(): VkSampleCountFlags {
    return value
}

infix fun VkFlags.has(bit: VkEnum): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkSampleCountFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkQueueFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkCompositeAlphaFlagBitsKHR): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkImageUsageFlagBits): Boolean {
    return this and bit.value != 0
}

infix fun VkFlags.has(bit: VkSurfaceTransformFlagBitsKHR): Boolean {
    return this and bit.value != 0
}

fun VkSampleCountFlags.set(bit: VkSampleCountFlagBits): VkSampleCountFlags {
    return this or bit.value
}

fun VkSampleCountFlags.clear(bit: VkSampleCountFlagBits): VkSampleCountFlags {
    return this and bit.value.inv()
}