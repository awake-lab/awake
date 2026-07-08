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

package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagsKHR

@VkMutator
data class VkSurfaceCapabilitiesKHR @JvmOverloads constructor(
    val minImageCount: Int = 0,
    val maxImageCount: Int = 0,
    val currentExtent: VkExtent2D = VkExtent2D(),
    val minImageExtent: VkExtent2D = VkExtent2D(),
    val maxImageExtent: VkExtent2D = VkExtent2D(),
    val maxImageArrayLayers: Int = 0,
    val supportedTransforms: VkSurfaceTransformFlagsKHR = 0,
    val currentTransform: VkSurfaceTransformFlagBitsKHR = VkSurfaceTransformFlagBitsKHR.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR,
    val supportedCompositeAlpha: VkCompositeAlphaFlagsKHR = 0,
    val supportedUsageFlags: VkImageUsageFlags = 0
)