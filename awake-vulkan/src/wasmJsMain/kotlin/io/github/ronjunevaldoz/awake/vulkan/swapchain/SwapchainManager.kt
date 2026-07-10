package io.github.ronjunevaldoz.awake.vulkan.swapchain

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class SwapchainManager actual constructor(
    graphicsDevice: GraphicsDevice,
    actual val maxFramesInFlight: Int
) {
    actual var swapChain: Long = 0
    actual var extent: VkExtent2D = VkExtent2D()
    actual var imageViews: List<Long> = emptyList()
    actual var imageFormat: VkFormat = VkFormat.VK_FORMAT_UNDEFINED
    actual val imageAvailableSemaphores = LongArray(maxFramesInFlight)
    actual val renderFinishedSemaphores = LongArray(maxFramesInFlight)
    actual val inFlightFences = LongArray(maxFramesInFlight)
    actual var currentFrame = 0

    actual fun create() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun createSyncObjects() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroySyncObjects() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
