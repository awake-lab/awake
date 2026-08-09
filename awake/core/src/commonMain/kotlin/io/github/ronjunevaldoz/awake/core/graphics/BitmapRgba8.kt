// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.graphics

/** [Bitmap.pixels]' packed ARGB ints (`0xAARRGGBB` -- the convention every [createBitmap]
 * actual already produces) widened into tightly-packed RGBA8 bytes: the layout
 * `io.github.ronjunevaldoz.awake.render.texture.TextureAsset` (render-api) and Vulkan's
 * `VK_FORMAT_R8G8B8A8_UNORM` texture upload both expect. Every [createBitmap] actual already
 * flips Y (OpenGL's bottom-up UV convention, this bitmap utility's original consumer) -- a
 * Vulkan/WebGPU caller doesn't undo that here; it flips V once in its own texture-sampling
 * shader instead (see `textured.wgsl`), rather than touching three platform actuals written
 * for a different backend's convention. */
fun Bitmap.toRgba8Bytes(): ByteArray {
    val bytes = ByteArray(pixels.size * RGBA_BYTES_PER_PIXEL)
    var offset = 0
    for (pixel in pixels) {
        bytes[offset + RED_BYTE_OFFSET] = ((pixel shr RED_SHIFT) and BYTE_MASK).toByte()
        bytes[offset + GREEN_BYTE_OFFSET] = ((pixel shr GREEN_SHIFT) and BYTE_MASK).toByte()
        bytes[offset + BLUE_BYTE_OFFSET] = (pixel and BYTE_MASK).toByte()
        bytes[offset + ALPHA_BYTE_OFFSET] = ((pixel shr ALPHA_SHIFT) and BYTE_MASK).toByte()
        offset += RGBA_BYTES_PER_PIXEL
    }
    return bytes
}

private const val RGBA_BYTES_PER_PIXEL = 4
private const val RED_BYTE_OFFSET = 0
private const val GREEN_BYTE_OFFSET = 1
private const val BLUE_BYTE_OFFSET = 2
private const val ALPHA_BYTE_OFFSET = 3
private const val ALPHA_SHIFT = 24
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val BYTE_MASK = 0xFF
