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

package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.ImageHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.ImageViewHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.SamplerHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageTiling
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSharingMode2

/**
 * Phase 2 (renderer abstraction): owns a single 2D texture's image/view/sampler upload --
 * extracted verbatim from `VulkanApplication`'s `createTextureImage`/`createTextureImageView`/
 * `createTextureSampler` functions and their backing fields. Same staging-buffer pattern as
 * [io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh]: a HOST_VISIBLE staging buffer is written,
 * then copied into a DEVICE_LOCAL image via a one-time command buffer.
 *
 * [runOneTimeCommands] is injected for the same reason as `Mesh`: it needs a command pool and
 * graphics queue, neither of which is extracted into a dedicated class yet.
 *
 * Descriptor-set binding (which set/binding index this texture occupies) is a Material/Shader
 * concern, not this class's -- callers read [imageView]/[sampler] to build their own
 * `VkDescriptorImageInfo`.
 */
class Texture(
    private val graphicsDevice: GraphicsDevice,
    private val runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    data: ByteArray,
    width: Int,
    height: Int
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    var image: ImageHandle = ImageHandle(0)
        private set
    var imageMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
        private set
    var imageView: ImageViewHandle = ImageViewHandle(0)
        private set
    var sampler: SamplerHandle = SamplerHandle(0)
        private set

    init {
        val imageSize = data.size.toLong()
        val stagingBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = imageSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            )
        )
        val stagingRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, stagingBuffer)
        val stagingMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            stagingRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val stagingMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = stagingRequirements.size,
                memoryTypeIndex = stagingMemoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, stagingBuffer, stagingMemory, 0)
        VulkanBuffers.writeBufferMemoryBytes(device, stagingMemory, 0, data)

        // The staging buffer/memory must be freed even if image creation, allocation, or the
        // copy/transition commands below throw -- otherwise a failed upload leaks GPU memory
        // silently. The image/imageMemory themselves aren't covered: if the copy fails,
        // Texture's constructor throws and the object never becomes visible to a caller.
        val rawImage: Long
        val rawImageMemory: Long
        try {
            rawImage = VulkanImages.vkCreateImage(
                device,
                VkImageCreateInfo(
                    width = width,
                    height = height,
                    format = VkFormat.VK_FORMAT_R8G8B8A8_UNORM.value,
                    usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
                        VkImageUsageFlagBits2.VK_IMAGE_USAGE_SAMPLED_BIT,
                    imageType = VkImageType.VK_IMAGE_TYPE_2D,
                    tiling = VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                    initialLayout = VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED,
                    sharingMode = VkSharingMode2.VK_SHARING_MODE_EXCLUSIVE,
                )
            )
            val imageRequirements = VulkanImages.vkGetImageMemoryRequirements(device, rawImage)
            val imageMemoryTypeIndex = VulkanBuffers.findMemoryType(
                physicalDevice,
                imageRequirements.memoryTypeBits,
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
            )
            rawImageMemory = VulkanBuffers.vkAllocateMemory(
                device,
                VkMemoryAllocateInfo(
                    allocationSize = imageRequirements.size,
                    memoryTypeIndex = imageMemoryTypeIndex
                )
            )
            VulkanImages.vkBindImageMemory(device, rawImage, rawImageMemory, 0)

            runOneTimeCommands { commandBuffer ->
                VulkanImages.vkTransitionImageLayout(
                    commandBuffer,
                    rawImage,
                    VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED,
                    VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
                )
                VulkanImages.vkCmdCopyBufferToImage(
                    commandBuffer,
                    stagingBuffer,
                    rawImage,
                    VkBufferImageCopy(imageWidth = width, imageHeight = height)
                )
                VulkanImages.vkTransitionImageLayout(
                    commandBuffer,
                    rawImage,
                    VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
                )
            }
        } finally {
            VulkanBuffers.vkDestroyBuffer(device, stagingBuffer)
            VulkanBuffers.vkFreeMemory(device, stagingMemory)
        }
        image = ImageHandle(rawImage)
        imageMemory = DeviceMemoryHandle(rawImageMemory)

        imageView = ImageViewHandle(
            Vulkan.vkCreateImageView(
                device,
                VkImageViewCreateInfo(
                    image = rawImage,
                    viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                    format = VkFormat.VK_FORMAT_R8G8B8A8_UNORM,
                    subresourceRange = VkImageSubresourceRange(
                        aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT.value,
                        baseMipLevel = 0,
                        levelCount = 1,
                        baseArrayLayer = 0,
                        layerCount = 1
                    )
                )
            )
        )

        sampler = SamplerHandle(VulkanImages.vkCreateSampler(device, VkSamplerCreateInfo()))
    }

    fun destroy() {
        VulkanImages.vkDestroySampler(device, sampler.handle)
        Vulkan.vkDestroyImageView(device, imageView.handle)
        VulkanImages.vkDestroyImage(device, image.handle)
        VulkanBuffers.vkFreeMemory(device, imageMemory.handle)
    }
}
