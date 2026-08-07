// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException

/** Waits this frame-in-flight slot's fence, acquires the next swapchain image, and waits
 * that image's own in-flight fence if a prior frame is still using it -- returns `null`
 * (and has already called [Renderer.recreateSwapChain]) when the swapchain went stale
 * mid-acquire, meaning the caller has no valid image to record/submit/present against and
 * must skip the rest of the frame. Split out of [performDraw] ([RendererDraw3D.kt]) into
 * its own file to keep both detekt's method-length limit and that file's function-count
 * limit. */
internal fun Renderer.acquireSwapchainImage(currentFrame: Int): Int? {
    Vulkan.vkWaitForFences(
        device,
        longArrayOf(swapchainManager.inFlightFences[currentFrame]),
        true,
        Long.MAX_VALUE,
    )
    // Reset only AFTER a successful acquire (not before) -- if acquire throws and this
    // frame bails out early (see the catch below), an already-reset-but-never-submitted
    // fence would stay unsignaled forever, hanging the NEXT draw() call's
    // vkWaitForFences on this same frame-in-flight slot indefinitely.
    val imageIndex: Int
    try {
        imageIndex = Vulkan.vkAcquireNextImageKHR(
            device,
            swapchainManager.swapChain,
            Int.MAX_VALUE.toLong(),
            swapchainManager.imageAvailableSemaphores[currentFrame],
            0,
        )
    } catch (e: VkResultException) {
        when (e.result) {
            // A resized/minimized/moved-to-another-display window makes the swapchain
            // stale before this frame's acquire even runs (not just after present, see
            // the catch around vkQueuePresentKHR below) -- recreate and skip this frame
            // entirely: there's no valid acquired image to record/submit/present against.
            VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> {
                recreateSwapChain()
                return null
            }
            else -> throw e
        }
    }
    val imageFence = swapchainManager.imagesInFlight[imageIndex]
    if (imageFence != 0L) {
        Vulkan.vkWaitForFences(device, longArrayOf(imageFence), true, Long.MAX_VALUE)
    }
    swapchainManager.imagesInFlight[imageIndex] = swapchainManager.inFlightFences[currentFrame]
    Vulkan.vkResetFences(device, longArrayOf(swapchainManager.inFlightFences[currentFrame]))
    return imageIndex
}
