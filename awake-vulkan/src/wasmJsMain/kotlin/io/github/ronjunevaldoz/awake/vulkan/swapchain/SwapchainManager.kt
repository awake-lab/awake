package io.github.ronjunevaldoz.awake.vulkan.swapchain

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.ygdrasil.webgpu.GPUTextureFormat

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): partial real implementation --
 * only what `RenderPipeline`/`Renderer` actually read ([extent], and the new
 * [imageFormatWebGpu]) is real; the frame-in-flight sync fields
 * ([imageAvailableSemaphores]/[renderFinishedSemaphores]/[inFlightFences]) have no WebGPU
 * equivalent (the browser's own frame pacing replaces them) and stay empty. [imageFormat]
 * is `VkFormat`-typed per the `expect` contract, which has no real WebGPU value to hold --
 * [imageFormatWebGpu] (not part of the `expect` contract, `wasmJsMain`-internal) is the
 * real one `RenderPipeline` actually uses.
 */
actual class SwapchainManager actual constructor(
    graphicsDevice: GraphicsDevice,
    actual val maxFramesInFlight: Int
) {
    private val graphicsDevice = graphicsDevice
    actual var swapChain: Long = 0
    actual var extent: VkExtent2D = VkExtent2D()
    actual var imageViews: List<Long> = emptyList()
    actual var imageFormat: VkFormat = VkFormat.VK_FORMAT_UNDEFINED
    actual val imageAvailableSemaphores = LongArray(maxFramesInFlight)
    actual val renderFinishedSemaphores = LongArray(maxFramesInFlight)
    actual val inFlightFences = LongArray(maxFramesInFlight)
    actual var currentFrame = 0

    internal var imageFormatWebGpu: GPUTextureFormat = GPUTextureFormat.BGRA8Unorm
        private set

    private val renderingContext get() = graphicsDevice.wgpuContext.renderingContext

    actual fun create() {
        imageFormatWebGpu = renderingContext.textureFormat
        extent = VkExtent2D(renderingContext.width.toInt(), renderingContext.height.toInt())
    }

    actual fun destroy() {
    }

    actual fun createSyncObjects() {
    }

    actual fun destroySyncObjects() {
    }
}
