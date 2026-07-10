/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.createBitmap
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice

/**
 * [Bitmap.pixels] is a packed ARGB `IntArray` on every platform actual (`AndroidBitmap`,
 * `DefaultBitmap`/desktop's `decodeBitmap`, `AppleBitmap` all pack `(alpha shl 24) or
 * (red shl 16) or (green shl 8) or blue`, confirmed by reading each `createBitmap` actual)
 * -- [Texture] wants raw interleaved RGBA8
 * bytes instead (4 bytes per pixel, red first), matching `VK_FORMAT_R8G8B8A8_*`. This is
 * the one conversion step between them.
 */
fun Bitmap.toRgba8Bytes(): ByteArray {
    val bytes = ByteArray(width * height * 4)
    for (i in 0 until width * height) {
        val argb = pixels[i]
        val offset = i * 4
        bytes[offset] = ((argb ushr 16) and 0xFF).toByte()     // R
        bytes[offset + 1] = ((argb ushr 8) and 0xFF).toByte()  // G
        bytes[offset + 2] = (argb and 0xFF).toByte()           // B
        bytes[offset + 3] = ((argb ushr 24) and 0xFF).toByte() // A
    }
    return bytes
}

/**
 * Loads an image resource (PNG/JPEG -- whatever [createBitmap]'s platform actual can
 * decode: `BitmapFactory` on Android, `ImageIO` on desktop, `UIImage` on iOS) as a real
 * Vulkan-backed [Texture], reusing the same [createBitmap] decode path `awake-core`'s
 * legacy OpenGL `TextureLoader` already uses so there's exactly one "bytes -> decoded
 * image" step in this engine, not two independently-maintained ones.
 *
 * There was previously no such path: `awake-vulkan`'s [Texture] only ever took a `Bitmap`,
 * `Texture(graphicsDevice, runOneTimeCommands, data: ByteArray, width, height)` constructor
 * directly -- the one real texture the demo app renders is a hardcoded 2x2 procedural
 * checkerboard `ByteArray`, not something loaded from a resource file.
 */
suspend fun loadVulkanTexture(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    resourcePath: String
): Texture {
    val bitmap = createBitmap(readResourceBytes(resourcePath))
    return Texture(graphicsDevice, runOneTimeCommands, bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
}
