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
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSharingMode
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkFenceCreateFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanImages
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageUsageFlagBits2
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes


class VulkanApplication : Application {
    /** Phase 2: instance/surface/physical-device/logical-device/queue lifecycle, extracted
     * into a reusable class -- see [GraphicsDevice]'s doc comment. */
    private val graphicsDevice = GraphicsDevice()
    private val surface get() = graphicsDevice.surface
    private val physicalDevice get() = graphicsDevice.physicalDevice
    private val device get() = graphicsDevice.device
    private val graphicsQueue get() = graphicsDevice.graphicsQueue
    private val presentQueue get() = graphicsDevice.presentQueue
    /** Phase 2: swapchain + frames-in-flight sync, extracted into a reusable class -- see
     * [SwapchainManager]'s doc comment. */
    private val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
    private val swapChain get() = swapchainManager.swapChain
    private val swapChainExtent get() = swapchainManager.extent
    private val swapChainImageViews get() = swapchainManager.imageViews
    private val swapChainImageFormat get() = swapchainManager.imageFormat
    private val imageAvailableSemaphores get() = swapchainManager.imageAvailableSemaphores
    private val renderFinishedSemaphores get() = swapchainManager.renderFinishedSemaphores
    private val inFlightFences get() = swapchainManager.inFlightFences
    private var currentFrame
        get() = swapchainManager.currentFrame
        set(value) {
            swapchainManager.currentFrame = value
        }
    /** Phase 2: render pass + graphics pipeline, extracted into a reusable class -- see
     * [RenderPipeline]'s doc comment. Constructed lazily in [setupVulkan] for the same
     * reason as [mesh]/[texture]/[material]: it needs [graphicsDevice] to already be
     * `.create()`-d, plus [material]'s descriptor set layout to exist first. */
    private lateinit var renderPipeline: RenderPipeline
    private var swapChainFrameBuffers: List<Long> = emptyList()
    /** Phase 2: command pool + one-time (upload) command submission, extracted into a
     * reusable class -- see [TransferContext]'s doc comment. Constructed lazily in
     * [setupVulkan] for the same reason as [mesh]/[texture]/[material]: it needs
     * [graphicsDevice] to already be `.create()`-d. */
    private lateinit var transferContext: TransferContext
    private val commandPool get() = transferContext.commandPool.handle
    private var commandBuffers: LongArray = LongArray(MAX_FRAMES_IN_FLIGHT)
    /** Phase 2: vertex/index buffer upload, extracted into a reusable class -- see
     * [Mesh]'s doc comment. Constructed lazily in [setupVulkan] (needs [commandPool] and
     * [graphicsQueue] to already exist for its one-time upload commands), not eagerly like
     * [graphicsDevice]/[swapchainManager]. */
    private lateinit var mesh: Mesh
    /** Phase 2: uniform buffer + descriptor set/pool/layout, extracted into a reusable
     * class -- see [Material]'s doc comment for why it's constructed in two phases. */
    private lateinit var material: Material
    private lateinit var texture: Texture
    private var depthImage: Long = 0
    private var depthImageMemory: Long = 0
    private var depthImageView: Long = 0
    private var frameCount = 0

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
        graphicsDevice.create(window)
        // create swap chain
        swapchainManager.create()
        material = Material(graphicsDevice)
        renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            material.descriptorSetLayout,
            readResourceBytes("assets/shader/vulkan/triangle.vert.spv"),
            readResourceBytes("assets/shader/vulkan/triangle.frag.spv"),
            VERTEX_STRIDE
        )
        createDepthResources()
        createFramebuffers()
        transferContext = TransferContext(graphicsDevice)
        mesh = Mesh(graphicsDevice, transferContext::runOneTimeCommands, cubeVertices, cubeIndices)
        texture = Texture(
            graphicsDevice,
            transferContext::runOneTimeCommands,
            textureData,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        )
        material.createResources(texture)
        createCommandBuffer()
        swapchainManager.createSyncObjects()
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

    /** Fixed camera looking at the origin -- only [updateUniformBuffer]'s model (spin)
     * matrix changes per frame. View/projection math itself lives in [Camera] (awake-core),
     * not here: it's backend-agnostic camera math, not a demo-animation concern. */
    private val camera = Camera(
        eye = Vec3(2f, 2f, 2f),
        center = Vec3(0f, 0f, 0f),
        fovYRadians = (45.0 * kotlin.math.PI / 180.0).toFloat(),
        near = 0.1f,
        far = 10f
    )

    /** Rebuilds model*view*projection every frame (a simple Y-axis spin -- the only part of
     * this that's demo-specific "animation," as opposed to [camera]'s reusable view/
     * projection math) and rewrites the whole uniform buffer. Only safe because [drawFrame]
     * calls `vkDeviceWaitIdle` after every submit -- see that function's comment for why a
     * single shared (not per-frame-in-flight) uniform buffer needs that serialization. */
    private fun updateUniformBuffer() {
        val angle = frameCount * 0.02f
        val model = Mat4().rotateY(angle).rotateX(angle * 0.5f)
        val aspect = swapChainExtent.width.toFloat() / swapChainExtent.height.toFloat()
        // Mat4's `data` array is column-major (matches GLSL's mat4 layout) but its `times`
        // operator's inner loops index it as if row-major, so `A * B` (Kotlin) actually
        // computes the conventional `B * A`. To get the conventional projection*view*model
        // (the standard vertex-transform order: model space -> view space -> clip space),
        // the Kotlin expression has to be written in the opposite order -- see
        // Camera.viewProjectionMatrix's doc for the same rule applied to view*projection.
        val mvp = model * camera.viewProjectionMatrix(aspect)
        material.updateUniformBuffer(mvp.data)
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

    private fun recordCommandBuffer(commandBuffer: Long, aquiredImageIndex: Int) {
        val beginInfo = VkCommandBufferBeginInfo(
            flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value,
//            flags = 0 // VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT.value
        )
        Vulkan.vkBeginCommandBuffer(commandBuffer, beginInfo)

        // start render pass
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
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
        renderPipeline.bind(commandBuffer)
        val viewport = VkViewport(
            width = swapChainExtent.width.toFloat(),
            height = swapChainExtent.height.toFloat(),
        )
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(
            extent = swapChainExtent
        )
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        mesh.bind(commandBuffer)
        material.bind(commandBuffer, renderPipeline.pipelineLayout)
        mesh.draw(commandBuffer)
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

    private fun createFramebuffers() {
        swapChainFrameBuffers = swapChainImageViews.map { imageView ->
            val frameBufferInfo = VkFramebufferCreateInfo(
                renderPass = renderPipeline.renderPass,
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

    private fun cleanSwapChain() {
        swapChainFrameBuffers.forEach { frameBuffer ->
            Vulkan.vkDestroyFramebuffer(device, frameBuffer)
        }
        swapchainManager.destroy()
    }

    private fun destroy() {
        cleanSwapChain()

        swapchainManager.destroySyncObjects()
//      Vulkan.vkFreeCommandBuffers(device, commandPool, 1, &commandBuffer);
        transferContext.destroy()

        mesh.destroy()
        texture.destroy()
        material.destroy()
        Vulkan.vkDestroyImageView(device, depthImageView)
        VulkanImages.vkDestroyImage(device, depthImage)
        VulkanBuffers.vkFreeMemory(device, depthImageMemory)

        renderPipeline.destroy()

        graphicsDevice.destroy()
    }
}