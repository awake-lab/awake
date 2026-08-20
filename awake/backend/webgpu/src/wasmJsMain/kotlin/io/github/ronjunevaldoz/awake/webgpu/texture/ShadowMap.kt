// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.texture

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.ygdrasil.webgpu.Extent3D
import io.ygdrasil.webgpu.GPUAddressMode
import io.ygdrasil.webgpu.GPUFilterMode
import io.ygdrasil.webgpu.GPUSampler
import io.ygdrasil.webgpu.GPUTexture
import io.ygdrasil.webgpu.GPUTextureDimension
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.GPUTextureView
import io.ygdrasil.webgpu.SamplerDescriptor
import io.ygdrasil.webgpu.TextureDescriptor
import io.ygdrasil.webgpu.TextureViewDescriptor

/**
 * A single depth-only render target for the directional shadow pre-pass in the WebGPU backend.
 * Mirrors Vulkan's `ShadowMap` (2048x2048 Depth32Float).
 */
class ShadowMap(
    graphicsDevice: GraphicsDevice,
    val size: Int = DEFAULT_SIZE,
) {
    val depthTexture: GPUTexture = graphicsDevice.wgpuContext.device.createTexture(
        TextureDescriptor(
            size = Extent3D(width = size.toUInt(), height = size.toUInt()),
            format = GPUTextureFormat.Depth32Float,
            usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.TextureBinding,
            dimension = GPUTextureDimension.TwoD,
        ),
    )

    val depthView: GPUTextureView = depthTexture.createView(TextureViewDescriptor())

    val sampler: GPUSampler = graphicsDevice.wgpuContext.device.createSampler(
        SamplerDescriptor(
            addressModeU = GPUAddressMode.ClampToEdge,
            addressModeV = GPUAddressMode.ClampToEdge,
            addressModeW = GPUAddressMode.ClampToEdge,
            magFilter = GPUFilterMode.Nearest,
            minFilter = GPUFilterMode.Nearest,
        ),
    )

    fun destroy() {
        depthTexture.close()
    }

    companion object {
        const val DEFAULT_SIZE = 2048
    }
}
