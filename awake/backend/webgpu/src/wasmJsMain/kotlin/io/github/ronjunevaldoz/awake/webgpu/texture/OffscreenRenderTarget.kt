// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.texture

import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.ygdrasil.webgpu.Extent3D
import io.ygdrasil.webgpu.GPUTexture
import io.ygdrasil.webgpu.GPUTextureDimension
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.GPUTextureView
import io.ygdrasil.webgpu.TextureDescriptor
import io.ygdrasil.webgpu.TextureViewDescriptor

/**
 * An offscreen color render destination (`Renderer.createRenderTarget`) -- a
 * [RenderTarget] implementation. Unlike Vulkan's [io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget],
 * WebGPU has no framebuffer object at all -- a render pass just names a [GPUTextureView]
 * directly, so there's no render-pass-compatibility concern to design around here (see that
 * Vulkan class's doc comment for the format-matching constraint it has to work around).
 *
 * Carries its own depth attachment so offscreen readbacks match the same occlusion rules as
 * the on-screen WebGPU path.
 *
 * [colorTexture]'s format follows the same browser-preferred format as the swapchain
 * ([graphicsDevice].wgpuContext.renderingContext.textureFormat, see `SwapchainManager` and
 * `WebGpuCanvasHost.configureSurface`) rather than a hardcoded guess -- `Renderer.renderToTexture`
 * draws into this target with the SAME `RenderPipeline` instance used for the on-screen pass
 * (built with `ColorTargetState(format = swapchainManager.imageFormatWebGpu)`), and WebGPU
 * requires a render pass's color attachment format to exactly match the bound pipeline's
 * target format.
 */
class OffscreenRenderTarget(
    graphicsDevice: GraphicsDevice,
    override val width: Int,
    override val height: Int
) : RenderTarget {
    val colorTexture: GPUTexture = graphicsDevice.wgpuContext.device.createTexture(
        TextureDescriptor(
            size = Extent3D(width = width.toUInt(), height = height.toUInt()),
            format = graphicsDevice.wgpuContext.renderingContext.textureFormat,
            usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.TextureBinding or GPUTextureUsage.CopySrc,
            dimension = GPUTextureDimension.TwoD
        )
    )

    val colorView: GPUTextureView = colorTexture.createView(TextureViewDescriptor())
    val depthTexture: GPUTexture = graphicsDevice.wgpuContext.device.createTexture(
        TextureDescriptor(
            size = Extent3D(width = width.toUInt(), height = height.toUInt()),
            format = GPUTextureFormat.Depth32Float,
            usage = GPUTextureUsage.RenderAttachment,
            dimension = GPUTextureDimension.TwoD
        )
    )
    val depthView: GPUTextureView = depthTexture.createView(TextureViewDescriptor())

    override fun destroy() {
        colorTexture.close()
        depthTexture.close()
    }
}
