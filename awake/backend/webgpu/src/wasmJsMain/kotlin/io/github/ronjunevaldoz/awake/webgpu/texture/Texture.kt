// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.texture

import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.render.texture.mipChain
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.Extent3D
import io.ygdrasil.webgpu.GPUAddressMode
import io.ygdrasil.webgpu.GPUFilterMode
import io.ygdrasil.webgpu.GPUMipmapFilterMode
import io.ygdrasil.webgpu.GPUSampler
import io.ygdrasil.webgpu.GPUTexture
import io.ygdrasil.webgpu.GPUTextureDimension
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.GPUTextureView
import io.ygdrasil.webgpu.SamplerDescriptor
import io.ygdrasil.webgpu.TexelCopyBufferLayout
import io.ygdrasil.webgpu.TexelCopyTextureInfo
import io.ygdrasil.webgpu.TextureDescriptor
import io.ygdrasil.webgpu.TextureViewDescriptor

/**
 * A sampled 2D texture uploaded from tightly-packed RGBA8 bytes (a glTF base-color image, see
 * `Renderer.createMaterial`) -- the wasmJs counterpart of
 * [io.github.ronjunevaldoz.awake.vulkan.texture.Texture], minus that one's staging
 * buffer/layout-transition machinery: `queue.writeTexture` does the whole upload in one call,
 * so [runOneTimeCommands] (kept for constructor parity with the Vulkan class) is unused.
 *
 * Uploads a full mip chain (see [io.github.ronjunevaldoz.awake.render.texture.mipChain] --
 * backend-neutral CPU box-filter downsampling, the same generator
 * [io.github.ronjunevaldoz.awake.vulkan.texture.Texture] uses) rather than just the base level,
 * so [sampler]'s `mipmapFilter` has more than one level to filter between.
 */
class Texture(
    graphicsDevice: GraphicsDevice,
    @Suppress("UNUSED_PARAMETER") runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    data: ByteArray,
    width: Int,
    height: Int,
) {
    val texture: GPUTexture
    val view: GPUTextureView
    val sampler: GPUSampler

    init {
        val device = graphicsDevice.wgpuContext.device
        val mipLevels = TextureAsset(data, width, height).mipChain()
        val size = Extent3D(width = width.toUInt(), height = height.toUInt())
        texture = device.createTexture(
            TextureDescriptor(
                size = size,
                format = GPUTextureFormat.RGBA8Unorm,
                usage = GPUTextureUsage.TextureBinding or GPUTextureUsage.CopyDst,
                dimension = GPUTextureDimension.TwoD,
                mipLevelCount = mipLevels.size.toUInt(),
            ),
        )
        mipLevels.forEachIndexed { index, level ->
            device.queue.writeTexture(
                destination = TexelCopyTextureInfo(texture = texture, mipLevel = index.toUInt()),
                data = fastArrayBufferOf(level.data),
                dataLayout = TexelCopyBufferLayout(bytesPerRow = (level.width * 4).toUInt()),
                size = Extent3D(width = level.width.toUInt(), height = level.height.toUInt()),
            )
        }
        view = texture.createView(TextureViewDescriptor())
        sampler = device.createSampler(
            SamplerDescriptor(
                addressModeU = GPUAddressMode.Repeat,
                addressModeV = GPUAddressMode.Repeat,
                addressModeW = GPUAddressMode.Repeat,
                magFilter = GPUFilterMode.Linear,
                minFilter = GPUFilterMode.Linear,
                mipmapFilter = GPUMipmapFilterMode.Linear,
            ),
        )
    }

    /** [view]/[sampler] are garbage-collected by the JS runtime (no explicit release exists);
     * only the texture's own GPU allocation has to be closed. */
    fun destroy() {
        texture.close()
    }
}
