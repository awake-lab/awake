/*
 * Awake
 * Awake.awake-demo.shared.commonMain
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

package demo

import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.vulkan.VK_SUBPASS_EXTERNAL
import io.github.ronjunevaldoz.awake.vulkan.Version
import io.github.ronjunevaldoz.awake.vulkan.Version.Companion.vkVersion
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkComponentSwizzle
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFrontFace
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPhysicalDeviceType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSharingMode
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkAccessFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandPoolCreateFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkFenceCreateFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import io.github.ronjunevaldoz.awake.vulkan.has
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentDescription
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSubpassDependency
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceCapabilitiesKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceFormatKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkApplicationInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkComponentMapping
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceQueueCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSemaphoreCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubpassDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSwapchainCreateInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorImageInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolSize
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutBinding
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageLayout2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageTiling
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSharingMode2
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkIndexType
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.DebugUtilsFormattedCallback
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCreateInfoEXT
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendAttachmentState
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDepthStencilStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDynamicStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineInputAssemblyStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineMultisampleStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineRasterizationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineShaderStageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineVertexInputStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputAttributeDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputBindingDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineViewportStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDevice
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException
import io.github.ronjunevaldoz.awake.vulkan.utils.findQueueFamilies
import io.github.ronjunevaldoz.awake.vulkan.utils.getAppExtProps
import io.github.ronjunevaldoz.awake.vulkan.utils.getAppLayerProps
import io.github.ronjunevaldoz.awake.vulkan.utils.isSwapChainSupported
import io.github.ronjunevaldoz.awake.vulkan.utils.querySwapChainSupport
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes


class VulkanApplication : Application {
    var debugUtilsMessenger: Long = 0
    var instance: Long = 0
    var surface: Long = 0
    var physicalDevice: Long = 0
    var device: Long = 0
    var graphicsQueue: Long = 0
    var presentQueue: Long = 0
    var swapChain: Long = 0
    var swapChainExtent: VkExtent2D = VkExtent2D()
    var swapChainImageViews: List<Long> = emptyList()
    var swapChainImageFormat = VkFormat.VK_FORMAT_UNDEFINED
    var renderPass: Long = 0
    var pipelineCache: Long = 0
    var pipelineLayout: Long = 0
    var graphicsPipeline: LongArray = longArrayOf()
    var swapChainFrameBuffers: List<Long> = emptyList()
    var commandPool: Long = 0
    var commandBuffers: LongArray = LongArray(MAX_FRAMES_IN_FLIGHT)
    var imageAvailableSemaphores: LongArray = LongArray(MAX_FRAMES_IN_FLIGHT)
    var renderFinishedSemaphores: LongArray = LongArray(MAX_FRAMES_IN_FLIGHT)
    var inFlightFences: LongArray = LongArray(MAX_FRAMES_IN_FLIGHT)
    var currentFrame = 0
    var vertexBuffer: Long = 0
    var vertexBufferMemory: Long = 0
    var indexBuffer: Long = 0
    var indexBufferMemory: Long = 0
    var descriptorSetLayout: Long = 0
    var descriptorPool: Long = 0
    var descriptorSet: Long = 0
    var uniformBuffer: Long = 0
    var uniformBufferMemory: Long = 0
    var textureImage: Long = 0
    var textureImageMemory: Long = 0
    var textureImageView: Long = 0
    var textureSampler: Long = 0
    var depthImage: Long = 0
    var depthImageMemory: Long = 0
    var depthImageView: Long = 0
    var frameCount = 0
    /** Set by [createSurface]; used by [destroy] to tear down platform window resources via
     * [destroySurfaceWindow] (a no-op on Android, which owns its own window lifecycle). */
    private var nativeWindow: Any? = null

    companion object {
        const val MAX_FRAMES_IN_FLIGHT = 2
        val clearColorValue = VkClearColorValue.rgba(0f, 0f, 0f, 1f)
        val clearDepthValue = VkClearDepthStencilValue(depth = 1f, stencil = 0)
        const val DEPTH_FORMAT = 126 // VkFormat.VK_FORMAT_D32_SFLOAT.value

        // interleaved position(vec3) + color(vec3) + uv(vec2), matching triangle.vert's
        // location 0 / location 1 / location 2 inputs. 8 unique corners of a unit cube,
        // colored with the classic RGB-cube palette (black/red/yellow/green/blue/magenta/
        // white/cyan) so every face is visually distinguishable; UVs are approximate
        // (shared corners can't have per-face-correct UVs without duplicating vertices,
        // out of scope for this MVP proof of indexed drawing + a real MVP matrix).
        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        // 12 triangles, 2 per face. cullMode is set to NONE in the pipeline (see
        // createGraphicsPipeline) specifically so this winding order doesn't need to be
        // outward-consistent per face -- depth testing alone resolves correct occlusion.
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )

        // A tiny 2x2 RGBA8 checkerboard (white/black) -- proves real texture sampling
        // without needing an image file loader (out of scope for this MVP phase).
        const val TEXTURE_WIDTH = 2
        const val TEXTURE_HEIGHT = 2
        val textureData = byteArrayOf(
            // white, black
            -1, -1, -1, -1, 0, 0, 0, -1,
            // black, white
            0, 0, 0, -1, -1, -1, -1, -1,
        )
    }

    private fun IntArray.toByteArrayLE(): ByteArray {
        val out = ByteArray(size * 4)
        for (i in indices) {
            val v = this[i]
            out[i * 4] = (v and 0xFF).toByte()
            out[i * 4 + 1] = ((v shr 8) and 0xFF).toByte()
            out[i * 4 + 2] = ((v shr 16) and 0xFF).toByte()
            out[i * 4 + 3] = ((v shr 24) and 0xFF).toByte()
        }
        return out
    }

    override fun create(surface: Any?) {
        surface?.let { setupVulkan(it) }
    }

    override fun update(delta: Float) {
        drawFrame()
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun resize(x: Int, y: Int, width: Int, height: Int) {

    }

    override fun dispose() {
        destroy()
    }

    private fun setupVulkan(window: Any) {
        createInstance()
        setupDebugMessenger()
        createSurface(window)
        // Physical Devices
        pickPhysicalDevice()
        // Logical Device
        createLogicalDevice()
        // create swap chain
        swapChain()
        createRenderPass()
        createDescriptorSetLayout()
        createGraphicsPipeline()
        createDepthResources()
        createFramebuffers()
        createCommandPool()
        createVertexBuffer()
        createIndexBuffer()
        createUniformBuffer()
        createTextureImage()
        createTextureImageView()
        createTextureSampler()
        createDescriptorPool()
        createDescriptorSet()
        createCommandBuffer()
        createSyncObjects()
    }

    private fun createDescriptorSetLayout() {
        descriptorSetLayout = VulkanDescriptors.vkCreateDescriptorSetLayout(
            device,
            VkDescriptorSetLayoutCreateInfo(
                pBindings = arrayOf(
                    VkDescriptorSetLayoutBinding(
                        binding = 0,
                        descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                        // MVP matrix is only ever read in the vertex shader now.
                        stageFlags = VkShaderStageFlagBits.VERTEX.value
                    ),
                    VkDescriptorSetLayoutBinding(
                        binding = 1,
                        descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                        stageFlags = VkShaderStageFlagBits.FRAGMENT.value
                    )
                )
            )
        )
    }

    private fun createDepthResources() {
        depthImage = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = swapChainExtent.width,
                height = swapChainExtent.height,
                format = DEPTH_FORMAT,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
            )
        )
        val requirements = VulkanImages.vkGetImageMemoryRequirements(device, depthImage)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        depthImageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = requirements.size,
                memoryTypeIndex = memoryTypeIndex
            )
        )
        VulkanImages.vkBindImageMemory(device, depthImage, depthImageMemory, 0)
        depthImageView = Vulkan.vkCreateImageView(
            device,
            VkImageViewCreateInfo(
                image = depthImage,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = VkFormat.VK_FORMAT_D32_SFLOAT,
                subresourceRange = VkImageSubresourceRange(
                    aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_DEPTH_BIT.value,
                    baseMipLevel = 0,
                    levelCount = 1,
                    baseArrayLayer = 0,
                    layerCount = 1
                )
            )
        )
    }

    /** MVP-matrix uniform buffer (64 bytes, mat4) -- initial contents don't matter since
     * [updateUniformBuffer] overwrites it every frame before the first draw. */
    private fun createUniformBuffer() {
        val bufferSize = (16 * Float.SIZE_BYTES).toLong()
        uniformBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = bufferSize,
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            )
        )
        val memRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, uniformBuffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            memRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        uniformBufferMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = memRequirements.size,
                memoryTypeIndex = memoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, uniformBuffer, uniformBufferMemory, 0)
    }

    /** Rebuilds model*view*projection every frame (a simple Y-axis spin) and rewrites the
     * whole uniform buffer. Only safe because [drawFrame] calls `vkDeviceWaitIdle` after
     * every submit -- see that function's comment for why a single shared (not
     * per-frame-in-flight) uniform buffer needs that serialization. */
    private fun updateUniformBuffer() {
        val angle = frameCount * 0.02f
        val model = Mat4().rotateY(angle).rotateX(angle * 0.5f)
        val view = Mat4.setLookAt(
            eye = Vec3(2f, 2f, 2f),
            center = Vec3(0f, 0f, 0f),
            up = Vec3(0f, 1f, 0f)
        )
        val aspect = swapChainExtent.width.toFloat() / swapChainExtent.height.toFloat()
        val projection = Mat4.perspective(
            fovY = (45.0 * kotlin.math.PI / 180.0).toFloat(),
            aspect = aspect,
            near = 0.1f,
            far = 10f
        )
        // Mat4.perspective follows the OpenGL convention (NDC +Y up); Vulkan's NDC has +Y
        // down, so the projection's Y scale must be flipped or the cube renders upside down.
        projection.m11 *= -1f
        // Mat4's `data` array is column-major (matches GLSL's mat4 layout) but its `times`
        // operator's inner loops index it as if row-major, so `A * B` (Kotlin) actually
        // computes the conventional `B * A`. To get the conventional projection*view*model
        // (the standard vertex-transform order: model space -> view space -> clip space),
        // the Kotlin expression has to be written in the opposite order.
        val mvp = model * view * projection
        VulkanBuffers.writeBufferMemoryFloats(device, uniformBufferMemory, 0, mvp.data)
    }

    private fun createDescriptorPool() {
        descriptorPool = VulkanDescriptors.vkCreateDescriptorPool(
            device,
            VkDescriptorPoolCreateInfo(
                maxSets = 1,
                pPoolSizes = arrayOf(
                    VkDescriptorPoolSize(
                        type = VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                        descriptorCount = 1
                    ),
                    VkDescriptorPoolSize(
                        type = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                        descriptorCount = 1
                    )
                )
            )
        )
    }

    private fun createDescriptorSet() {
        descriptorSet = VulkanDescriptors.vkAllocateDescriptorSet(
            device,
            descriptorPool,
            descriptorSetLayout
        )
        VulkanDescriptors.vkUpdateDescriptorSetBuffer(
            device,
            descriptorSet,
            0,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            VkDescriptorBufferInfo(
                buffer = uniformBuffer,
                range = (16 * Float.SIZE_BYTES).toLong()
            )
        )
        VulkanDescriptors.vkUpdateDescriptorSetImage(
            device,
            descriptorSet,
            1,
            VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            VkDescriptorImageInfo(
                sampler = textureSampler,
                imageView = textureImageView
            )
        )
    }

    /** Runs [block] on a fresh one-time command buffer, submitted and waited-on via a
     * throwaway fence rather than vkQueueWaitIdle/vkFreeCommandBuffers (neither of which
     * exist in the legacy Vulkan object yet). */
    private fun runOneTimeCommands(block: (Long) -> Unit) {
        val allocInfo = VkCommandBufferAllocateInfo(
            commandPool = commandPool,
            level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            commandBufferCount = 1
        )
        val commandBuffer = Vulkan.vkAllocateCommandBuffers(device, allocInfo)
        Vulkan.vkBeginCommandBuffer(
            commandBuffer,
            VkCommandBufferBeginInfo(
                flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value
            )
        )
        block(commandBuffer)
        Vulkan.vkEndCommandBuffer(commandBuffer)

        val fence = Vulkan.vkCreateFence(device, VkFenceCreateInfo())
        Vulkan.vkQueueSubmit(
            graphicsQueue,
            arrayOf(VkSubmitInfo(pCommandBuffers = arrayOf(commandBuffer))),
            fence
        )
        Vulkan.vkWaitForFences(device, longArrayOf(fence), true, Long.MAX_VALUE)
        Vulkan.vkDestroyFence(device, fence)
    }

    private fun createTextureImage() {
        val imageSize = textureData.size.toLong()
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
        VulkanBuffers.writeBufferMemoryBytes(device, stagingMemory, 0, textureData)

        textureImage = VulkanImages.vkCreateImage(
            device,
            VkImageCreateInfo(
                width = TEXTURE_WIDTH,
                height = TEXTURE_HEIGHT,
                format = VkFormat.VK_FORMAT_R8G8B8A8_UNORM.value,
                usage = VkImageUsageFlagBits2.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
                    VkImageUsageFlagBits2.VK_IMAGE_USAGE_SAMPLED_BIT,
                imageType = VkImageType.VK_IMAGE_TYPE_2D,
                tiling = VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                initialLayout = VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED,
                sharingMode = VkSharingMode2.VK_SHARING_MODE_EXCLUSIVE,
            )
        )
        val imageRequirements = VulkanImages.vkGetImageMemoryRequirements(device, textureImage)
        val imageMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            imageRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        textureImageMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = imageRequirements.size,
                memoryTypeIndex = imageMemoryTypeIndex
            )
        )
        VulkanImages.vkBindImageMemory(device, textureImage, textureImageMemory, 0)

        runOneTimeCommands { commandBuffer ->
            VulkanImages.vkTransitionImageLayout(
                commandBuffer,
                textureImage,
                VkImageLayout2.VK_IMAGE_LAYOUT_UNDEFINED,
                VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
            )
            VulkanImages.vkCmdCopyBufferToImage(
                commandBuffer,
                stagingBuffer,
                textureImage,
                VkBufferImageCopy(imageWidth = TEXTURE_WIDTH, imageHeight = TEXTURE_HEIGHT)
            )
            VulkanImages.vkTransitionImageLayout(
                commandBuffer,
                textureImage,
                VkImageLayout2.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VkImageLayout2.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
            )
        }

        VulkanBuffers.vkDestroyBuffer(device, stagingBuffer)
        VulkanBuffers.vkFreeMemory(device, stagingMemory)
    }

    private fun createTextureImageView() {
        textureImageView = Vulkan.vkCreateImageView(
            device,
            VkImageViewCreateInfo(
                image = textureImage,
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
    }

    private fun createTextureSampler() {
        textureSampler = VulkanImages.vkCreateSampler(device, VkSamplerCreateInfo())
    }

    /** Allocates a HOST_VISIBLE staging buffer, writes into it via [write], copies it into a
     * new DEVICE_LOCAL buffer (usage = [usage] or TRANSFER_DST) using a one-time command
     * buffer + [VulkanBuffers.vkCmdCopyBuffer], then frees the staging buffer. DEVICE_LOCAL
     * memory is the whole reason this is worth the extra buffer/copy: it's not necessarily
     * CPU-mappable, but it's the memory type the GPU can read fastest. */
    private fun createDeviceLocalBuffer(
        byteSize: Long,
        usage: Int,
        write: (memory: Long) -> Unit
    ): Pair<Long, Long> {
        val stagingBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
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
        write(stagingMemory)

        val destBuffer = VulkanBuffers.vkCreateBuffer(
            device,
            VkBufferCreateInfo(
                size = byteSize,
                usage = usage or VkBufferUsageFlagBits.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
            )
        )
        val destRequirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, destBuffer)
        val destMemoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            destRequirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
        )
        val destMemory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(
                allocationSize = destRequirements.size,
                memoryTypeIndex = destMemoryTypeIndex
            )
        )
        VulkanBuffers.vkBindBufferMemory(device, destBuffer, destMemory, 0)

        runOneTimeCommands { commandBuffer ->
            VulkanBuffers.vkCmdCopyBuffer(commandBuffer, stagingBuffer, destBuffer, byteSize)
        }

        VulkanBuffers.vkDestroyBuffer(device, stagingBuffer)
        VulkanBuffers.vkFreeMemory(device, stagingMemory)

        return destBuffer to destMemory
    }

    private fun createVertexBuffer() {
        val bufferSize = (cubeVertices.size * Float.SIZE_BYTES).toLong()
        val (buffer, memory) = createDeviceLocalBuffer(
            byteSize = bufferSize,
            usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            write = { stagingMemory ->
                VulkanBuffers.writeBufferMemoryFloats(device, stagingMemory, 0, cubeVertices)
            }
        )
        vertexBuffer = buffer
        vertexBufferMemory = memory
    }

    private fun createIndexBuffer() {
        val indexBytes = cubeIndices.toByteArrayLE()
        val bufferSize = indexBytes.size.toLong()
        val (buffer, memory) = createDeviceLocalBuffer(
            byteSize = bufferSize,
            usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
            write = { stagingMemory ->
                VulkanBuffers.writeBufferMemoryBytes(device, stagingMemory, 0, indexBytes)
            }
        )
        indexBuffer = buffer
        indexBufferMemory = memory
    }

    private fun drawFrame() {
        Vulkan.vkWaitForFences(
            device,
            longArrayOf(inFlightFences[currentFrame]),
            true,
            Long.MAX_VALUE
        )
        Vulkan.vkResetFences(device, longArrayOf(inFlightFences[currentFrame]))

        val imageIndex = Vulkan.vkAcquireNextImageKHR(
            device,
            swapChain,
            Int.MAX_VALUE.toLong(),
            imageAvailableSemaphores[currentFrame],
            0
        )

        updateUniformBuffer()
        frameCount++

        Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
        recordCommandBuffer(commandBuffers[currentFrame], imageIndex)

        val waitSemaphores = arrayOf(imageAvailableSemaphores[currentFrame])
        val waitStages =
            intArrayOf(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value)
        val signalSemaphores = arrayOf(renderFinishedSemaphores[currentFrame])

        val submitInfo = VkSubmitInfo(
            pWaitSemaphores = waitSemaphores,
            pWaitDstStageMask = waitStages,
            pCommandBuffers = arrayOf(commandBuffers[currentFrame]),
            pSignalSemaphores = signalSemaphores
        )

        Vulkan.vkQueueSubmit(graphicsQueue, arrayOf(submitInfo), inFlightFences[currentFrame])

        val presentInfo = VkPresentInfoKHR(
            pWaitSemaphores = signalSemaphores,
            pSwapchains = arrayOf(swapChain),
            pImageIndices = intArrayOf(imageIndex),
            pResults = VkResult.values()
        )

        try {
            Vulkan.vkQueuePresentKHR(presentQueue, presentInfo)
        } catch (e: VkResultException) {
            when (e.result) {
                VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> recreateSwapChain()
                else -> throw e
            }
        }

        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT

        // Fully serializes frames so the single (not per-frame-in-flight) uniform buffer
        // above can be safely rewritten every frame -- see updateUniformBuffer's comment.
        // A real engine would double-buffer the UBO per frame-in-flight instead of paying
        // this full-pipeline stall; deferred as a Phase 2 (renderer abstraction) concern.
        VulkanBuffers.vkDeviceWaitIdle(device)
    }

    private fun recreateSwapChain() {
        // TODO process recreation of swapchain here
//        Vulkan.vkDeviceWaitIdle(device)
    }

    private fun createSyncObjects() {
        val semaphoreInfo = VkSemaphoreCreateInfo()
        val fenceInfo = VkFenceCreateInfo(
            flags = VkFenceCreateFlagBits.VK_FENCE_CREATE_SIGNALED_BIT.value
        )

        for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
            imageAvailableSemaphores[i] = Vulkan.vkCreateSemaphore(device, semaphoreInfo)
            renderFinishedSemaphores[i] = Vulkan.vkCreateSemaphore(device, semaphoreInfo)
            inFlightFences[i] = Vulkan.vkCreateFence(device, fenceInfo)
        }
    }

    private fun recordCommandBuffer(commandBuffer: Long, aquiredImageIndex: Int) {
        val beginInfo = VkCommandBufferBeginInfo(
            flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value,
//            flags = 0 // VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT.value
        )
        Vulkan.vkBeginCommandBuffer(commandBuffer, beginInfo)

        // start render pass
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPass,
            framebuffer = swapChainFrameBuffers[aquiredImageIndex],
            renderArea = VkRect2D(
                extent = swapChainExtent
            ),
            pClearValues = arrayOf(clearColorValue, clearDepthValue)
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            renderPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE
        )

        // basic drawing
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline[0]
        )
        val viewport = VkViewport(
            width = swapChainExtent.width.toFloat(),
            height = swapChainExtent.height.toFloat(),
        )
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(
            extent = swapChainExtent
        )
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            0,
            longArrayOf(vertexBuffer),
            longArrayOf(0L)
        )
        VulkanBuffers.vkCmdBindIndexBuffer(
            commandBuffer,
            indexBuffer,
            0,
            VkIndexType.VK_INDEX_TYPE_UINT32
        )
        VulkanDescriptors.vkCmdBindDescriptorSet(commandBuffer, pipelineLayout, 0, descriptorSet)
        VulkanBuffers.vkCmdDrawIndexed(commandBuffer, cubeIndices.size, 1, 0, 0, 0)
        Vulkan.vkCmdEndRenderPass(commandBuffer)
        Vulkan.vkEndCommandBuffer(commandBuffer)
    }

    private fun createCommandBuffer() {
        val allocInfo = VkCommandBufferAllocateInfo(
            commandPool = commandPool,
            level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            commandBufferCount = 1
        )
        for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
            commandBuffers[i] = Vulkan.vkAllocateCommandBuffers(device, allocInfo)
        }
    }

    private fun createCommandPool() {
        val (graphicsFamily, presentFamily) = findQueueFamilies(physicalDevice, surface)

        val poolInfo = VkCommandPoolCreateInfo(
            flags = VkCommandPoolCreateFlagBits.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT.value,
            queueFamilyIndex = graphicsFamily!!
        )

        commandPool = Vulkan.vkCreateCommandPool(device, poolInfo)
    }

    private fun createFramebuffers() {
        swapChainFrameBuffers = swapChainImageViews.map { imageView ->
            val frameBufferInfo = VkFramebufferCreateInfo(
                renderPass = renderPass,
                // depthImageView is shared across every framebuffer -- only one frame is
                // ever actually in the depth-write phase at a time given drawFrame's
                // vkDeviceWaitIdle serialization, so this is safe (a "real" per-frame-in-
                // flight setup would need one depth image per frame-in-flight instead).
                pAttachments = arrayOf(imageView, depthImageView),
                width = swapChainExtent.width,
                height = swapChainExtent.height,
                layers = 1
            )
            Vulkan.vkCreateFramebuffer(device, frameBufferInfo)
        }.toList()
    }

    private fun createRenderPass() {
        renderPass = Vulkan.vkCreateRenderPass(
            device, VkRenderPassCreateInfo(
                pAttachments = arrayOf(
                    VkAttachmentDescription(
                        format = swapChainImageFormat,
                        initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                        finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
                    ),
                    VkAttachmentDescription(
                        format = VkFormat.VK_FORMAT_D32_SFLOAT,
                        storeOp = VkAttachmentStoreOp.DONT_CARE,
                        initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                        finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                    )
                ),
                pSubpasses = arrayOf(
                    VkSubpassDescription(
                        pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                        pColorAttachments = arrayOf(
                            VkAttachmentReference(
                                attachment = 0,
                                layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                            )
                        ),
                        pDepthStencilAttachment = arrayOf(
                            VkAttachmentReference(
                                attachment = 1,
                                layout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                            )
                        )
                    )
                ),
                pDependencies = arrayOf(
                    VkSubpassDependency(
                        srcSubpass = VK_SUBPASS_EXTERNAL,
                        dstSubpass = 0,
                        srcStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                            VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                        srcAccessMask = 0,
                        dstStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                            VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                        dstAccessMask = VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT.value or
                            VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.value or
                            VkAccessFlagBits.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT.value,
                    )
                )
            )
        )
    }

    private fun createInstance() {
        val appInfo = VkApplicationInfo(
            pApplicationName = "Awake Vulkan - Application",
            pEngineName = "Awake Vulkan - Engine",
            apiVersion = Version(1, 3, 0).vkVersion
        )
        val layerProperties = getAppLayerProps()
        val layerExtProps = layerProperties.map { layer ->
            getAppExtProps(layer)
        }.flatten()

        // glfwGetRequiredInstanceExtensions() is a safe no-op returning emptyArray() on
        // every non-GLFW platform (Android/iOS) -- see VulkanWindow.kt's actuals.
        val glfwExtensions = VulkanWindow.glfwGetRequiredInstanceExtensions().toList()
        // MoltenVK (desktop macOS) conforms to the Vulkan Portability spec: vkCreateInstance
        // requires both VK_KHR_portability_enumeration enabled AND
        // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR set, or it fails with
        // VK_ERROR_INCOMPATIBLE_DRIVER. GLFW reporting VK_EXT_metal_surface as required is
        // the reliable signal we're really running on MoltenVK (as opposed to Android or a
        // real-native-Vulkan desktop driver, neither of which ever reports that extension).
        val onMoltenVk = "VK_EXT_metal_surface" in glfwExtensions
        val portabilityExtension = if (onMoltenVk) listOf("VK_KHR_portability_enumeration") else emptyList()
        val instanceFlags = if (onMoltenVk) 0x00000001 else 0 // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR

        val extProperties = (getAppExtProps() + layerExtProps + glfwExtensions + portabilityExtension).distinct()

        val createInfo = VkInstanceCreateInfo(
            flags = instanceFlags,
            pApplicationInfo = arrayOf(appInfo),
            ppEnabledLayerNames = layerProperties.toTypedArray(),
            ppEnabledExtensionNames = extProperties.toTypedArray()
        )
        instance = Vulkan.vkCreateInstance(createInfo)
    }

    private fun pickPhysicalDevice() {
        val physicalDevices =
            Vulkan.vkEnumeratePhysicalDevices(instance).map { VkPhysicalDevice(it, instance) }
        if (physicalDevices.isNotEmpty()) {
            // find a gpu
            val gpu = physicalDevices.find { vkDevice ->
                val properties = Vulkan.vkGetPhysicalDeviceProperties(vkDevice.physicalDevice)
                val features = Vulkan.vkGetPhysicalDeviceFeatures(vkDevice.physicalDevice)
                val hasGeometry = features.geometryShader
                val isIntegratedGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU
                val isDiscreteGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
                val isVirtualGPU =
                    properties.deviceType == VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU
                isIntegratedGPU || isDiscreteGPU || isVirtualGPU
            } ?: throw Exception("Cannot find suitable gpu!")
            physicalDevice = gpu.physicalDevice
        }
    }

    private fun createLogicalDevice() {
        // Queue families
        val queueFamilyProperties =
            Vulkan.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice)
        val indices = findQueueFamilies(physicalDevice, surface)

        if (!isSwapChainSupported(physicalDevice, surface)) {
//            Timber.w("Vulkan", "SwapChain not supported")
        }

        if (!indices.isComplete()) {
            // graphics not supported?
            throw Exception("GPU graphics / Presentation not supported")
        }

        // to avoid duplicate queue family index use set
        val uniqueQueueFamilies = setOf(
            indices.graphicsFamily!!,
            indices.presentFamily!!
        )
        val queueInfos = uniqueQueueFamilies.map { uniqueQueueFamilyIndex ->
            VkDeviceQueueCreateInfo(
                queueFamilyIndex = uniqueQueueFamilyIndex,
                queueCount = 1,
                pQueuePriorities = floatArrayOf(1.0f)
            )
        }

        val features = Vulkan.vkGetPhysicalDeviceFeatures(physicalDevice)
        val deviceExtensions =
            Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice)
                .map { it.extensionName }.toList()
        val layerProperties = getAppLayerProps()
        val layerDeviceExtProps = layerProperties.map { layer ->
            Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice, layer)
                .map { it.extensionName }.toList()
        }.flatten()

        val deviceInfo = VkDeviceCreateInfo(
            pQueueCreateInfos = queueInfos.toTypedArray(),
            pEnabledFeatures = arrayOf(features),
            ppEnabledExtensionNames = (deviceExtensions + layerDeviceExtProps).distinct()
                .toTypedArray()
        )
        device = Vulkan.vkCreateDevice(physicalDevice, deviceInfo) // VkDevice


        graphicsQueue = Vulkan.vkGetDeviceQueue(
            device,
            indices.graphicsFamily!!,
            0
        ) // TODO where to get queueIndex??
        presentQueue = Vulkan.vkGetDeviceQueue(
            device,
            indices.presentFamily!!,
            0
        ) // TODO where to get queueIndex??
    }


    private fun setupDebugMessenger() {
        val androidLogCallback: (String, String) -> Unit = { severity, message ->
            println("AWAKE_VERIFY_VALIDATION [$severity] $message")
        }
        val createInfo = VkDebugUtilsMessengerCreateInfoEXT(
            pfnUserCallback = { severity, messageType, callbackData, userData ->
                DebugUtilsFormattedCallback(androidLogCallback).invoke(
                    severity,
                    messageType,
                    callbackData,
                    userData
                )
            },
            pUserData = null
        )
        debugUtilsMessenger = Vulkan.vkCreateDebugUtilsMessengerEXT(instance, createInfo)
    }

    private fun swapChain() {
        val (capabilities, formats, presentModes) = querySwapChainSupport(physicalDevice, surface)
        val (format, colorSpace) = chooseSwapSurfaceFormat(formats)
        val presentMode = chooseSwapPresentMode(presentModes)
        val extent = chooseSwapExtent(capabilities)

        val imageCount = (capabilities.minImageCount + 1).coerceIn(1, capabilities.maxImageCount)

        val indices = findQueueFamilies(physicalDevice, surface)
        var queueFamilyIndices: Array<Int>? =
            arrayOf(indices.graphicsFamily!!, indices.presentFamily!!)
        val imageSharingMode: VkSharingMode
        if (indices.graphicsFamily !== indices.presentFamily) {
            imageSharingMode = VkSharingMode.VK_SHARING_MODE_CONCURRENT
        } else {
            imageSharingMode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE
            queueFamilyIndices = null
        }
        val compositeAlpha =
            if (capabilities.supportedCompositeAlpha has VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR) {
                VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR
            } else if (capabilities.supportedCompositeAlpha has VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR) {
                VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR
            } else {
                throw Exception("No valid compositeAlpha found")
            }


        val imageUsage =
            if (capabilities.supportedUsageFlags has VkImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) {
                VkImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT.value
            } else {
                throw Exception("No valid usage flags found")
            }

        val preTransform = capabilities.currentTransform

        val createInfo = VkSwapchainCreateInfoKHR(
            surface = surface,
            minImageCount = imageCount,
            imageFormat = format,
            imageColorSpace = colorSpace,
            imageExtent = extent,
            imageArrayLayers = 1,
            imageUsage = imageUsage,
            imageSharingMode = imageSharingMode,
            pQueueFamilyIndices = queueFamilyIndices?.toIntArray(),
            preTransform = preTransform,
            compositeAlpha = compositeAlpha,
            presentMode = presentMode,
            clipped = true,
            oldSwapchain = swapChain
        )
        swapChainImageFormat = format
        swapChain = Vulkan.vkCreateSwapchainKHR(device, createInfo)
        swapChainExtent = extent
        createImageViews()
    }

    private fun createImageViews() {
        val swapChainImages = Vulkan.vkGetSwapchainImagesKHR(device, swapChain)

        swapChainImageViews = swapChainImages.map { swapChainImage ->
            val createInfo = VkImageViewCreateInfo(
                image = swapChainImage,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = swapChainImageFormat,
                components = VkComponentMapping(
                    VkComponentSwizzle.VK_COMPONENT_SWIZZLE_R,
                    VkComponentSwizzle.VK_COMPONENT_SWIZZLE_G,
                    VkComponentSwizzle.VK_COMPONENT_SWIZZLE_B,
                    VkComponentSwizzle.VK_COMPONENT_SWIZZLE_A,
                ),
                subresourceRange = VkImageSubresourceRange(
                    aspectMask = VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT.value,
                    baseMipLevel = 0,
                    levelCount = 1,
                    baseArrayLayer = 0,
                    layerCount = 1
                )
            )
            Vulkan.vkCreateImageView(device, createInfo)
        }
    }

    fun createShaderModule(code: IntArray): Long {
        val createInfo = VkShaderModuleCreateInfo(
            pCode = code
        )
        return Vulkan.vkCreateShaderModule(device, createInfo)
    }

    fun ByteArray.toIntArray(): IntArray {
//        val byteBuffer = ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asIntBuffer()
//        val unsignedByteArray = IntArray(byteBuffer.remaining())
//        byteBuffer.get(unsignedByteArray)
        return IntArray(this.size / 4) { i ->
            (this[i * 4].toInt() and 0xFF) or
                    ((this[i * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((this[i * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((this[i * 4 + 3].toInt() and 0xFF) shl 24)
        }
    }

    private fun createGraphicsPipeline() {
        run {
            // WARNING: make sure the .spv vulkan version match, this might cause out of memory
            val fragShaderCode = readResourceBytes("assets/shader/vulkan/triangle.frag.spv")
            val vertShaderCode = readResourceBytes("assets/shader/vulkan/triangle.vert.spv")

            val fragShaderModule = createShaderModule(fragShaderCode.toIntArray())
            val vertShaderModule = createShaderModule(vertShaderCode.toIntArray())

            // process shader
            val fragShaderStageInfo = VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.FRAGMENT,
                module = fragShaderModule,
                pName = "main"
            )
            val vertShaderStageInfo = VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.VERTEX,
                module = vertShaderModule,
                pName = "main"
            )
            val shaderStages = arrayOf(fragShaderStageInfo, vertShaderStageInfo)

            val vertexInputInfo = arrayOf(
                VkPipelineVertexInputStateCreateInfo(
                    pVertexBindingDescriptions = arrayOf(
                        VkVertexInputBindingDescription(
                            binding = 0,
                            stride = VERTEX_STRIDE,
                            inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX
                        )
                    ),
                    pVertexAttributeDescriptions = arrayOf(
                        VkVertexInputAttributeDescription(
                            location = 0,
                            binding = 0,
                            format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT,
                            offset = 0
                        ),
                        VkVertexInputAttributeDescription(
                            location = 1,
                            binding = 0,
                            format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT,
                            offset = 3 * Float.SIZE_BYTES
                        ),
                        VkVertexInputAttributeDescription(
                            location = 2,
                            binding = 0,
                            format = VkFormat.VK_FORMAT_R32G32_SFLOAT,
                            offset = 6 * Float.SIZE_BYTES
                        )
                    )
                )
            )

            val dynamicInfo = arrayOf(
                VkPipelineDynamicStateCreateInfo(
                    pDynamicStates = arrayOf(
                        VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT,
                        VkDynamicState.VK_DYNAMIC_STATE_SCISSOR,
                    )
                )
            )

            val viewportInfo = arrayOf(
                VkPipelineViewportStateCreateInfo(
                    pViewports = arrayOf(
                        VkViewport(
                            width = swapChainExtent.width.toFloat(),
                            height = swapChainExtent.height.toFloat(),
                        )
                    ),
                    pScissors = arrayOf(
                        VkRect2D(
                            offset = VkOffset2D(),
                            extent = swapChainExtent
                        )
                    )
                )
            )

            val depthStencil = arrayOf(
                VkPipelineDepthStencilStateCreateInfo()
            )

            val multisamplingInfo = arrayOf(
                VkPipelineMultisampleStateCreateInfo()
            )
            // Specify we will use triangle lists to draw geometry.
            val inputAssemblyInfo = arrayOf(
                VkPipelineInputAssemblyStateCreateInfo(
                    topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                    primitiveRestartEnable = false
                )
            )
            // Specify rasterization state.
            val rasterizationInfo = arrayOf(
                VkPipelineRasterizationStateCreateInfo(
                    // NONE, deliberately: cubeIndices' winding isn't guaranteed
                    // outward-consistent per face (see its comment) -- depth testing alone
                    // resolves correct occlusion regardless of triangle winding. Revisit
                    // once per-face vertex duplication makes winding order meaningful.
                    cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                    frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE,
                    lineWidth = 1f
                )
            )

            val blendAttachment = VkPipelineColorBlendAttachmentState(
                blendEnable = false,
                colorWriteMask = VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT.value or VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT.value
            )

            val colorBlendInfo = arrayOf(
                VkPipelineColorBlendStateCreateInfo(
                    pAttachments = arrayOf(blendAttachment)
                )
            )

            pipelineLayout = Vulkan.vkCreatePipelineLayout(
                device,
                VkPipelineLayoutCreateInfo(pSetLayouts = arrayOf(descriptorSetLayout))
            )

            val createInfos = arrayOf(
                VkGraphicsPipelineCreateInfo(
                    pStages = shaderStages,
                    pVertexInputState = vertexInputInfo,
                    pInputAssemblyState = inputAssemblyInfo,
                    pViewportState = viewportInfo,
                    pRasterizationState = rasterizationInfo,
                    pMultisampleState = multisamplingInfo,
                    pColorBlendState = colorBlendInfo,
                    pDepthStencilState = depthStencil,
                    pDynamicState = dynamicInfo,
                    layout = pipelineLayout,
                    renderPass = renderPass,
                    subpass = 0,
                    basePipelineHandle = 0, // Optional
                    basePipelineIndex = -1 // Optional
                )
            )
            pipelineCache = Vulkan.vkCreatePipelineCache(device, VkPipelineCacheCreateInfo())
            graphicsPipeline = Vulkan.vkCreateGraphicsPipelines(
                device, pipelineCache, createInfos
            )

            Vulkan.vkDestroyShaderModule(device, fragShaderModule)
            Vulkan.vkDestroyShaderModule(device, vertShaderModule)
        }
    }

    private fun chooseSwapSurfaceFormat(availableFormats: List<VkSurfaceFormatKHR>): VkSurfaceFormatKHR {
        require(availableFormats.isNotEmpty()) { "AvailableFormats must not be empty." }
        val preferedFormats = listOf(
            VkFormat.VK_FORMAT_R8G8B8A8_SRGB,
            VkFormat.VK_FORMAT_B8G8R8A8_SRGB,
            VkFormat.VK_FORMAT_A8B8G8R8_SRGB_PACK32,
        )
        return availableFormats.find { surfaceFormat ->
            preferedFormats.contains(surfaceFormat.format) && surfaceFormat.colorSpace == VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR
        } ?: availableFormats.first()
    }

    private fun chooseSwapPresentMode(availablePresetModes: List<VkPresentModeKHR>): VkPresentModeKHR {
        require(availablePresetModes.isNotEmpty()) { "AvailablePresetModes must not be empty." }
        return availablePresetModes.find { presentMode ->
            presentMode == VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR
        } ?: return VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR
    }

    private fun chooseSwapExtent(
        capabilities: VkSurfaceCapabilitiesKHR,
//        context: Context
    ): VkExtent2D {
        if (capabilities.currentExtent.width != Int.MAX_VALUE) {
            return capabilities.currentExtent
        }
//        val displayMetrics = context.resources.displayMetrics
        // TODO get size from actual window
        val width = 0//displayMetrics.widthPixels
        val height = 0//displayMetrics.heightPixels
        val actualWidth =
            width.coerceIn(capabilities.minImageExtent.width, capabilities.maxImageExtent.width)
        val actualHeight =
            height.coerceIn(capabilities.minImageExtent.height, capabilities.maxImageExtent.height)
        return VkExtent2D(actualWidth, actualHeight)
    }

    /** [window] is an `android.view.Surface` on Android, or a GLFW window handle (`Long`,
     * from [VulkanWindow.glfwCreateWindow]) on desktop -- see [io.github.ronjunevaldoz.awake.
     * vulkan.createSurface] for the real per-platform expect/actual. */
    private fun createSurface(window: Any): VkSurfaceKHR {
        nativeWindow = window
        surface = io.github.ronjunevaldoz.awake.vulkan.createSurface(instance, window)
        return VkSurfaceKHR(
            instance = instance,
            surface = surface
        )
    }

    private fun cleanSwapChain() {
        swapChainImageViews.forEach { imageView ->
            Vulkan.vkDestroyImageView(device, imageView)
        }
        swapChainFrameBuffers.forEach { frameBuffer ->
            Vulkan.vkDestroyFramebuffer(device, frameBuffer)
        }
        Vulkan.vkDestroySwapchainKHR(device, swapChain)
    }

    private fun destroy() {
        cleanSwapChain()

        repeat(MAX_FRAMES_IN_FLIGHT) { index ->
            Vulkan.vkDestroySemaphore(device, imageAvailableSemaphores[index])
            Vulkan.vkDestroySemaphore(device, renderFinishedSemaphores[index])
            Vulkan.vkDestroyFence(device, inFlightFences[index])
        }
//      Vulkan.vkFreeCommandBuffers(device, commandPool, 1, &commandBuffer);
        Vulkan.vkDestroyCommandPool(device, commandPool)

        VulkanBuffers.vkDestroyBuffer(device, vertexBuffer)
        VulkanBuffers.vkFreeMemory(device, vertexBufferMemory)
        VulkanBuffers.vkDestroyBuffer(device, indexBuffer)
        VulkanBuffers.vkFreeMemory(device, indexBufferMemory)
        VulkanBuffers.vkDestroyBuffer(device, uniformBuffer)
        VulkanBuffers.vkFreeMemory(device, uniformBufferMemory)
        VulkanImages.vkDestroySampler(device, textureSampler)
        Vulkan.vkDestroyImageView(device, textureImageView)
        VulkanImages.vkDestroyImage(device, textureImage)
        VulkanBuffers.vkFreeMemory(device, textureImageMemory)
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)
        VulkanDescriptors.vkDestroyDescriptorPool(device, descriptorPool)
        VulkanDescriptors.vkDestroyDescriptorSetLayout(device, descriptorSetLayout)

        graphicsPipeline.forEach { pipeline ->
            Vulkan.vkDestroyPipeline(device, pipeline)
        }

        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyRenderPass(device, renderPass)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)

        Vulkan.vkDestroySurfaceKHR(instance, surface)
        Vulkan.vkDestroyDevice(device)
        Vulkan.vkDestroyDebugUtilsMessengerEXT(instance, debugUtilsMessenger)
        Vulkan.vkDestroyInstance(instance)

        nativeWindow?.let { io.github.ronjunevaldoz.awake.vulkan.destroySurfaceWindow(it) }
    }
}