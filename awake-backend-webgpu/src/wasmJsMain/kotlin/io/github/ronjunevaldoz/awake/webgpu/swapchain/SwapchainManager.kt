package io.github.ronjunevaldoz.awake.webgpu.swapchain

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.ygdrasil.webgpu.GPUTextureFormat

/**
 * Module restructuring slice 2 (see docs/MVP_PLAN.md): partial real implementation --
 * the frame-in-flight sync fields ([imageAvailableSemaphores]/[renderFinishedSemaphores]/
 * [inFlightFences]) have no WebGPU equivalent (the browser's own frame pacing replaces
 * them) and stay empty. `extent`/`imageFormat` (Vulkan-typed, `VkExtent2D`/`VkFormat`) were
 * dropped entirely once this backend moved to its own module: they existed only to satisfy
 * the old shared `expect` contract with the Vulkan backend, and grep confirmed neither is
 * read anywhere in this module either -- canvas size/format come from [imageFormatWebGpu]
 * and `renderingContext.width`/`.height` directly (see [io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.draw]).
 */
class SwapchainManager(
    graphicsDevice: GraphicsDevice,
    val maxFramesInFlight: Int
) {
    private val graphicsDevice = graphicsDevice
    var swapChain: Long = 0
    var imageViews: List<Long> = emptyList()
    val imageAvailableSemaphores = LongArray(maxFramesInFlight)
    val renderFinishedSemaphores = LongArray(maxFramesInFlight)
    val inFlightFences = LongArray(maxFramesInFlight)
    var currentFrame = 0

    internal var imageFormatWebGpu: GPUTextureFormat = GPUTextureFormat.BGRA8Unorm
        private set

    private val renderingContext get() = graphicsDevice.wgpuContext.renderingContext

    fun create() {
        imageFormatWebGpu = renderingContext.textureFormat
    }

    fun destroy() {
    }

    fun createSyncObjects() {
    }

    fun destroySyncObjects() {
    }
}
