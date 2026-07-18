// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.swapchain

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCompositeAlphaFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkComponentSwizzle
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageAspectFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageViewType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSharingMode
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkFenceCreateFlagBits
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.has
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceCapabilitiesKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceFormatKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkComponentMapping
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageSubresourceRange
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSemaphoreCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSwapchainCreateInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.utils.findQueueFamilies
import io.github.ronjunevaldoz.awake.vulkan.utils.querySwapChainSupport

/**
 * Phase 2 (renderer abstraction): owns the swapchain (images, image views, format, extent)
 * and the per-frame-in-flight synchronization primitives (semaphores/fences) -- extracted
 * verbatim from `VulkanApplication`'s `swapChain`/`createImageViews`/`chooseSwap*`/
 * `cleanSwapChain`/`createSyncObjects` functions and their backing fields. Same calls, same
 * order, same TODO (extent falls back to `0x0` instead of reading the real window size when
 * `currentExtent.width == Int.MAX_VALUE` -- pre-existing, not addressed by this move).
 *
 * Framebuffers are deliberately NOT owned here: they also depend on the render pass and
 * depth image view, neither of which is extracted yet -- `VulkanApplication` still owns
 * those and must destroy its framebuffers before calling [destroy].
 */
class SwapchainManager(
    graphicsDevice: GraphicsDevice,
    val maxFramesInFlight: Int
) {
    private val graphicsDevice = graphicsDevice
    private val physicalDevice get() = graphicsDevice.physicalDevice
    private val device get() = graphicsDevice.device
    private val surface get() = graphicsDevice.surface

    var swapChain: Long = 0
    var extent: VkExtent2D = VkExtent2D()
    var imageViews: List<Long> = emptyList()
    var imageFormat = VkFormat.VK_FORMAT_UNDEFINED

    val imageAvailableSemaphores = LongArray(maxFramesInFlight)
    val renderFinishedSemaphores = LongArray(maxFramesInFlight)
    val inFlightFences = LongArray(maxFramesInFlight)
    var currentFrame = 0

    fun create() {
        val (capabilities, formats, presentModes) = querySwapChainSupport(physicalDevice, surface)
        val (format, colorSpace) = chooseSwapSurfaceFormat(formats)
        val presentMode = chooseSwapPresentMode(presentModes)
        val chosenExtent = chooseSwapExtent(capabilities)

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
            imageExtent = chosenExtent,
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
        imageFormat = format
        swapChain = Vulkan.vkCreateSwapchainKHR(device, createInfo)
        extent = chosenExtent
        createImageViews()
    }

    /** Desktop-only headless stand-in for [create] (see `GraphicsDevice.createHeadless`'s doc
     * comment) -- a headless [GraphicsDevice] has no `VkSurfaceKHR`, so there is no real
     * `VkSwapchainKHR` to query/create here. Just fixes [imageFormat]/[extent] to the values
     * [RenderPipeline][io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline]/
     * [OffscreenRenderTarget][io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget]
     * read off this manager (both only ever read a format/extent value, never touch
     * [swapChain]/[imageViews] directly). [imageViews] stays empty -- nothing on the headless
     * path (`Renderer.renderToTexture`/`readPixels`) ever indexes into it; only `Renderer.draw`
     * (never called headless) does. */
    fun createHeadless(width: Int, height: Int, format: VkFormat = VkFormat.VK_FORMAT_R8G8B8A8_UNORM) {
        imageFormat = format
        extent = VkExtent2D(width, height)
    }

    private fun createImageViews() {
        val swapChainImages = Vulkan.vkGetSwapchainImagesKHR(device, swapChain)
        imageViews = swapChainImages.map { swapChainImage ->
            val createInfo = VkImageViewCreateInfo(
                image = swapChainImage,
                viewType = VkImageViewType.VK_IMAGE_VIEW_TYPE_2D,
                format = imageFormat,
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

    /** Deliberately prefers `_UNORM` formats, not `_SRGB` -- every [io.github.ronjunevaldoz
     * .awake.core.colors.Color] this engine produces (`AwakeShadcnTheme`'s OKLCH palette,
     * every widget's authored color) is already gamma-encoded sRGB bytes by the time it
     * reaches a draw call (see `OklchColor.toSrgbChannel()`). An `_SRGB` swapchain format
     * makes the GPU apply ITS OWN linear->sRGB encoding on write, double-encoding colors that
     * are already sRGB-encoded -- this washed every dark/mid-tone color toward gray (a
     * should-be-near-black `(10,10,10)` foreground rendered as `(56,56,56)`, confirmed by
     * sampling a real screenshot), while leaving pure black/white untouched (both endpoints
     * are fixed points of gamma encoding) -- exactly the "pale UI, colors look washed out"
     * symptom this fixes. `colorSpace` stays `SRGB_NONLINEAR_KHR` (unchanged) so the display
     * still presents the swapchain contents as sRGB, which they genuinely are now that
     * there's exactly one gamma-encoding step (software), not two. */
    private fun chooseSwapSurfaceFormat(availableFormats: List<VkSurfaceFormatKHR>): VkSurfaceFormatKHR {
        require(availableFormats.isNotEmpty()) { "AvailableFormats must not be empty." }
        val preferedFormats = listOf(
            VkFormat.VK_FORMAT_R8G8B8A8_UNORM,
            VkFormat.VK_FORMAT_B8G8R8A8_UNORM,
            VkFormat.VK_FORMAT_A8B8G8R8_UNORM_PACK32,
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

    private fun chooseSwapExtent(capabilities: VkSurfaceCapabilitiesKHR): VkExtent2D {
        if (capabilities.currentExtent.width != Int.MAX_VALUE) {
            return capabilities.currentExtent
        }
        // TODO get size from actual window
        val width = 0
        val height = 0
        val actualWidth =
            width.coerceIn(capabilities.minImageExtent.width, capabilities.maxImageExtent.width)
        val actualHeight =
            height.coerceIn(capabilities.minImageExtent.height, capabilities.maxImageExtent.height)
        return VkExtent2D(actualWidth, actualHeight)
    }

    /** Destroys the image views + swapchain itself. Framebuffers are the caller's
     * responsibility (see class doc comment) and must be destroyed BEFORE this. */
    fun destroy() {
        imageViews.forEach { imageView ->
            Vulkan.vkDestroyImageView(device, imageView)
        }
        Vulkan.vkDestroySwapchainKHR(device, swapChain)
    }

    fun createSyncObjects() {
        val semaphoreInfo = VkSemaphoreCreateInfo()
        val fenceInfo = VkFenceCreateInfo(
            flags = VkFenceCreateFlagBits.VK_FENCE_CREATE_SIGNALED_BIT.value
        )

        for (i in 0 until maxFramesInFlight) {
            imageAvailableSemaphores[i] = Vulkan.vkCreateSemaphore(device, semaphoreInfo)
            renderFinishedSemaphores[i] = Vulkan.vkCreateSemaphore(device, semaphoreInfo)
            inFlightFences[i] = Vulkan.vkCreateFence(device, fenceInfo)
        }
    }

    fun destroySyncObjects() {
        repeat(maxFramesInFlight) { index ->
            Vulkan.vkDestroySemaphore(device, imageAvailableSemaphores[index])
            Vulkan.vkDestroySemaphore(device, renderFinishedSemaphores[index])
            Vulkan.vkDestroyFence(device, inFlightFences[index])
        }
    }
}
