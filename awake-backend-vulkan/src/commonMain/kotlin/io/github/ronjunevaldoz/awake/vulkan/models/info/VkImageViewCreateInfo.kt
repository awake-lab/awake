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

package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkHandle
import io.github.ronjunevaldoz.awake.vulkan.VkHandleRef
import io.github.ronjunevaldoz.awake.vulkan.enums.VkComponentSwizzle
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

data class VkImageViewCreateInfo(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO,
    val pNext: Any? = null,
    val flags: Int = 0,
    @field:VkHandleRef("VkImage")
    val image: VkHandle = 0,
    val viewType: VkImageViewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
    val format: VkFormat = VkFormat.VK_FORMAT_UNDEFINED,
    val components: VkComponentMapping = VkComponentMapping(),
    val subresourceRange: VkImageSubresourceRange = VkImageSubresourceRange()
)

data class VkComponentMapping(
    val r: VkComponentSwizzle = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY,
    val g: VkComponentSwizzle = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY,
    val b: VkComponentSwizzle = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY,
    val a: VkComponentSwizzle = VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY
)

data class VkImageSubresourceRange(
    val aspectMask: VkImageAspectFlags = 0,
    val baseMipLevel: Int = 0,
    val levelCount: Int = 1,
    val baseArrayLayer: Int = 0,
    val layerCount: Int = 1
)