// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.texture

/**
 * Full mip chain for this [TextureAsset], base level first, down to a final 1x1 level.
 * Backend-neutral box-filter downsampling -- neither backend has GPU-side mip generation
 * (Vulkan has no `vkCmdBlitImage` binding, WebGPU has no native blit-based mip generation at
 * all), so this generates the whole chain once on the CPU at texture-load time and each
 * backend uploads every level directly (Vulkan: `vkCmdCopyBufferToImage` per mip level,
 * already supports a `mipLevel` target; WebGPU: `queue.writeTexture` per mip level).
 */
fun TextureAsset.mipChain(): List<TextureAsset> {
    val levels = mutableListOf(this)
    var current = this
    while (current.width > 1 || current.height > 1) {
        val nextWidth = (current.width / 2).coerceAtLeast(1)
        val nextHeight = (current.height / 2).coerceAtLeast(1)
        current = current.boxDownsample(nextWidth, nextHeight)
        levels += current
    }
    return levels
}

private fun TextureAsset.boxDownsample(newWidth: Int, newHeight: Int): TextureAsset {
    val out = ByteArray(newWidth * newHeight * 4)
    for (y in 0 until newHeight) {
        val srcY0 = (y * height) / newHeight
        val srcY1 = (((y + 1) * height) / newHeight).coerceAtLeast(srcY0 + 1).coerceAtMost(height)
        for (x in 0 until newWidth) {
            val srcX0 = (x * width) / newWidth
            val srcX1 = (((x + 1) * width) / newWidth).coerceAtLeast(srcX0 + 1).coerceAtMost(width)
            var r = 0
            var g = 0
            var b = 0
            var a = 0
            var count = 0
            for (sy in srcY0 until srcY1) {
                for (sx in srcX0 until srcX1) {
                    val offset = (sy * width + sx) * 4
                    r += data[offset].toInt() and 0xFF
                    g += data[offset + 1].toInt() and 0xFF
                    b += data[offset + 2].toInt() and 0xFF
                    a += data[offset + 3].toInt() and 0xFF
                    count += 1
                }
            }
            val outOffset = (y * newWidth + x) * 4
            out[outOffset] = (r / count).toByte()
            out[outOffset + 1] = (g / count).toByte()
            out[outOffset + 2] = (b / count).toByte()
            out[outOffset + 3] = (a / count).toByte()
        }
    }
    return TextureAsset(out, newWidth, newHeight)
}
