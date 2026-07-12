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
 * No depth attachment, matching this backend's existing main 3D pass (it also has none
 * today -- see `Renderer.draw`'s own doc comment) -- avoids an asymmetry where offscreen
 * renders would look "more correct" than on-screen ones. Real depth-testing for both is a
 * separate, later follow-up.
 */
class OffscreenRenderTarget(
    graphicsDevice: GraphicsDevice,
    override val width: Int,
    override val height: Int
) : RenderTarget {
    val colorTexture: GPUTexture = graphicsDevice.wgpuContext.device.createTexture(
        TextureDescriptor(
            size = Extent3D(width = width.toUInt(), height = height.toUInt()),
            format = GPUTextureFormat.RGBA8Unorm,
            usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.TextureBinding or GPUTextureUsage.CopySrc,
            dimension = GPUTextureDimension.TwoD
        )
    )

    val colorView: GPUTextureView = colorTexture.createView(TextureViewDescriptor())

    override fun destroy() {
        colorTexture.close()
    }
}
