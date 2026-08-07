// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.swapchain

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceCapabilitiesKHR
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SwapchainManagerTest {
    @Test
    fun variableExtentUsesFramebufferSizeProvider() {
        val manager = swapchainManagerWithExtent(width = 1920, height = 1080)

        val extent = manager.chooseSwapExtent(
            VkSurfaceCapabilitiesKHR(
                currentExtent = VkExtent2D(Int.MAX_VALUE, Int.MAX_VALUE),
                minImageExtent = VkExtent2D(64, 64),
                maxImageExtent = VkExtent2D(4096, 4096),
            ),
        )

        assertEquals(VkExtent2D(1920, 1080), extent)
    }

    @Test
    fun variableExtentClampsFramebufferSizeToSurfaceLimits() {
        val manager = swapchainManagerWithExtent(width = 5000, height = 24)

        val extent = manager.chooseSwapExtent(
            VkSurfaceCapabilitiesKHR(
                currentExtent = VkExtent2D(Int.MAX_VALUE, Int.MAX_VALUE),
                minImageExtent = VkExtent2D(64, 64),
                maxImageExtent = VkExtent2D(4096, 2160),
            ),
        )

        assertEquals(VkExtent2D(4096, 64), extent)
    }

    @Test
    fun variableExtentRequiresFramebufferSizeProvider() {
        val manager = SwapchainManager(GraphicsDevice(), maxFramesInFlight = 2)

        assertFailsWith<IllegalArgumentException> {
            manager.chooseSwapExtent(
                VkSurfaceCapabilitiesKHR(
                    currentExtent = VkExtent2D(Int.MAX_VALUE, Int.MAX_VALUE),
                    minImageExtent = VkExtent2D(64, 64),
                    maxImageExtent = VkExtent2D(4096, 2160),
                ),
            )
        }
    }

    @Test
    fun unlimitedSwapchainImageCountDoesNotClampToZero() {
        val manager = SwapchainManager(GraphicsDevice(), maxFramesInFlight = 2)

        val imageCount = manager.chooseSwapchainImageCount(
            VkSurfaceCapabilitiesKHR(
                minImageCount = 2,
                maxImageCount = 0,
            ),
        )

        assertEquals(3, imageCount)
    }

    @Test
    fun headlessSwapchainHasNoTrackedImages() {
        val manager = SwapchainManager(GraphicsDevice(), maxFramesInFlight = 2)
        manager.createHeadless(width = 320, height = 240)

        assertEquals(0, manager.imagesInFlight.size)
        assertEquals(0, manager.renderFinishedSemaphores.size)
    }

    private fun swapchainManagerWithExtent(width: Int, height: Int): SwapchainManager =
        SwapchainManager(
            GraphicsDevice(),
            maxFramesInFlight = 2,
            surfaceExtentProvider = { VkExtent2D(width, height) },
        )
}
