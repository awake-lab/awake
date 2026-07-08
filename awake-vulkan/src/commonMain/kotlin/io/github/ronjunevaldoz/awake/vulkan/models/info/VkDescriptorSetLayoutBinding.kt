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

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags

/**
 * `descriptorType`/`stageFlags` are modeled as plain `Int` rather than jni-binding-generator
 * enum fields -- see the Phase 1d enum-marshalling hazard note in docs/MVP_PLAN.md. Both
 * VkDescriptorType and VkShaderStageFlagBits are extension-bearing in the Vulkan spec, so
 * ordinal-based marshalling would be unsafe long-term even though today's values happen to
 * line up. Use [VkDescriptorType] and Awake's existing `VkShaderStageFlagBits.value` for
 * these fields.
 */
class VkDescriptorSetLayoutBinding(
    val binding: Int,
    val descriptorType: Int,
    val descriptorCount: Int = 1,
    val stageFlags: VkShaderStageFlags = 0,
)

typealias VkShaderStageFlags = VkFlags

object VkDescriptorType {
    const val VK_DESCRIPTOR_TYPE_SAMPLER = 0
    const val VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER = 1
    const val VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE = 2
    const val VK_DESCRIPTOR_TYPE_STORAGE_IMAGE = 3
    const val VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER = 4
    const val VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER = 5
    const val VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER = 6
    const val VK_DESCRIPTOR_TYPE_STORAGE_BUFFER = 7
    const val VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC = 8
    const val VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC = 9
    const val VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT = 10
}

class VkDescriptorSetLayoutCreateInfo(
    val flags: Int = 0,
    @VkArray(sizeAlias = "bindingCount")
    val pBindings: Array<VkDescriptorSetLayoutBinding>? = null,
)

class VkDescriptorPoolSize(
    val type: Int,
    val descriptorCount: Int,
)

class VkDescriptorPoolCreateInfo(
    val maxSets: Int,
    val flags: Int = 0,
    @VkArray(sizeAlias = "poolSizeCount")
    val pPoolSizes: Array<VkDescriptorPoolSize>? = null,
)

class VkDescriptorBufferInfo(
    val buffer: Long,
    val range: Long,
    val offset: Long = 0,
)
