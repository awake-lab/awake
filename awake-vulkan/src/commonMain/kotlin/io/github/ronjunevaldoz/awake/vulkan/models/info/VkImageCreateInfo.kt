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

/**
 * `imageType`/`format`/`tiling`/`initialLayout`/`usage`/`sharingMode` are all plain `Int`
 * (real Vulkan numeric values, not jni-binding-generator enum fields) -- consistent with
 * the Phase 1d ordinal-vs-value hazard rule (see docs/MVP_PLAN.md). `VkImageType`/
 * `VkFormat`/`VkImageTiling`/`VkImageLayout` are all extension-bearing in the Vulkan spec.
 */
class VkImageCreateInfo(
    val width: Int,
    val height: Int,
    val format: Int,
    val usage: Int,
    val imageType: Int = VkImageType.VK_IMAGE_TYPE_2D,
    val tiling: Int = VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
    val initialLayout: Int = VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED,
    val sharingMode: Int = VkSharingMode2.VK_SHARING_MODE_EXCLUSIVE,
    val mipLevels: Int = 1,
    val arrayLayers: Int = 1,
    val samples: Int = 1, // VK_SAMPLE_COUNT_1_BIT
    val flags: Int = 0,
)

object VkImageType {
    const val VK_IMAGE_TYPE_1D = 0
    const val VK_IMAGE_TYPE_2D = 1
    const val VK_IMAGE_TYPE_3D = 2
}

object VkImageTiling {
    const val VK_IMAGE_TILING_OPTIMAL = 0
    const val VK_IMAGE_TILING_LINEAR = 1
}

/**
 * Plain-Int mirror of the existing enum-typed `VkImageLayout` (kept enum-typed for the
 * legacy generator's swapchain/render-pass usage) -- jni-binding-generator's function-level
 * `Int` params can't share an existing Kotlin `enum class` field-for-field without risking
 * the ordinal hazard, so image-layout values used in this package's plain-`Int` structs are
 * duplicated here as real Vulkan values instead of reusing the enum.
 */
object VkImageLayout2 {
    const val VK_IMAGE_LAYOUT_UNDEFINED = 0
    const val VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL = 5
    const val VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL = 7
}

object VkSharingMode2 {
    const val VK_SHARING_MODE_EXCLUSIVE = 0
    const val VK_SHARING_MODE_CONCURRENT = 1
}

object VkImageUsageFlagBits2 {
    const val VK_IMAGE_USAGE_TRANSFER_DST_BIT = 0x00000002
    const val VK_IMAGE_USAGE_SAMPLED_BIT = 0x00000004
}

class VkSamplerCreateInfo(
    val magFilter: Int = VkFilter.VK_FILTER_LINEAR,
    val minFilter: Int = VkFilter.VK_FILTER_LINEAR,
    val addressModeU: Int = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT,
    val addressModeV: Int = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT,
    val addressModeW: Int = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT,
    val anisotropyEnable: Boolean = false,
    val maxAnisotropy: Float = 1f,
    val borderColor: Int = VkBorderColor.VK_BORDER_COLOR_INT_OPAQUE_BLACK,
    val unnormalizedCoordinates: Boolean = false,
    val mipmapMode: Int = VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR,
)

object VkFilter {
    const val VK_FILTER_NEAREST = 0
    const val VK_FILTER_LINEAR = 1
}

object VkSamplerAddressMode {
    const val VK_SAMPLER_ADDRESS_MODE_REPEAT = 0
    const val VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE = 2
}

object VkSamplerMipmapMode {
    const val VK_SAMPLER_MIPMAP_MODE_NEAREST = 0
    const val VK_SAMPLER_MIPMAP_MODE_LINEAR = 1
}

object VkBorderColor {
    const val VK_BORDER_COLOR_INT_OPAQUE_BLACK = 3
}

class VkDescriptorImageInfo(
    val sampler: Long,
    val imageView: Long,
    val imageLayout: Int = VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
)

/** `bufferRowLength`/`bufferImageHeight` of `0` mean "tightly packed", the common case. */
class VkBufferImageCopy(
    val imageWidth: Int,
    val imageHeight: Int,
    val bufferOffset: Long = 0,
    val bufferRowLength: Int = 0,
    val bufferImageHeight: Int = 0,
    val mipLevel: Int = 0,
    val baseArrayLayer: Int = 0,
    val layerCount: Int = 1,
)
