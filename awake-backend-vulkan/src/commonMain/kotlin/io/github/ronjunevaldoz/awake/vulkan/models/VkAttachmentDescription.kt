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

import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentLoadOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlagBits

data class VkAttachmentDescription(
    val flags: VkAttachmentDescriptionFlags = 0,
    val format: VkFormat = VkFormat.VK_FORMAT_UNDEFINED,
    val samples: VkSampleCountFlagBits = VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT,
    val loadOp: VkAttachmentLoadOp = VkAttachmentLoadOp.CLEAR,
    val storeOp: VkAttachmentStoreOp = VkAttachmentStoreOp.STORE,
    val stencilLoadOp: VkAttachmentLoadOp = VkAttachmentLoadOp.DONT_CARE,
    val stencilStoreOp: VkAttachmentStoreOp = VkAttachmentStoreOp.DONT_CARE,
    val initialLayout: VkImageLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
    val finalLayout: VkImageLayout = VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
)

typealias VkAttachmentDescriptionFlags = VkFlags


