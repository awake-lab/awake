// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.swapchain

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.GPUTextureView
import io.ygdrasil.webgpu.TextureDescriptor
import io.ygdrasil.webgpu.TextureViewDescriptor

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

    internal var imageFormatWebGpu: GPUTextureFormat = GPUTextureFormat.RGBA8Unorm
        private set

    internal var depthTextureView: GPUTextureView? = null
        private set

    private var depthTextureHandle: io.ygdrasil.webgpu.GPUTexture? = null
    private var configuredWidth = 0u
    private var configuredHeight = 0u

    private val renderingContext get() = graphicsDevice.wgpuContext.renderingContext

    fun create() {
        imageFormatWebGpu = GPUTextureFormat.RGBA8Unorm
        syncSurface()
    }

    fun destroy() {
        depthTextureHandle?.close()
        depthTextureHandle = null
        depthTextureView = null
    }

    fun createSyncObjects() {
    }

    fun destroySyncObjects() {
    }

    fun syncSurface() {
        imageFormatWebGpu = GPUTextureFormat.RGBA8Unorm
        val width = renderingContext.width
        val height = renderingContext.height
        if (width == configuredWidth && height == configuredHeight && depthTextureView != null) return

        depthTextureHandle?.close()
        depthTextureHandle = graphicsDevice.wgpuContext.device.createTexture(
            TextureDescriptor(
                size = io.ygdrasil.webgpu.Extent3D(width = width, height = height),
                format = GPUTextureFormat.Depth32Float,
                usage = GPUTextureUsage.RenderAttachment
            )
        )
        depthTextureView = depthTextureHandle?.createView(TextureViewDescriptor())
        configuredWidth = width
        configuredHeight = height
    }
}
