// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.ronjunevaldoz.awake.core.graphics

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.get
import org.khronos.webgl.toInt8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.ImageBitmap
import org.w3c.files.Blob
import kotlin.js.JsAny
import kotlin.js.toJsArray

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1 left this as a compile-only stub;
// implemented for real here using createImageBitmap() (real browser decode, format-agnostic
// -- no PNG/JPEG sniffing needed) + an offscreen canvas to read pixels back via getImageData().
// The literals below are the RGBA8888 layout getImageData() returns and the ARGB layout
// Bitmap exposes -- channel offsets and byte shifts, not tunable values.
@Suppress("MagicNumber")
actual suspend fun createBitmap(bytes: ByteArray): Bitmap {
    val blobParts = arrayOf<JsAny?>(bytes.toInt8Array()).toJsArray()
    val imageBitmap: ImageBitmap = window.createImageBitmap(Blob(blobParts)).await()
    val width = imageBitmap.width
    val height = imageBitmap.height

    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    ctx.drawImage(imageBitmap, 0.0, 0.0)
    val data: Uint8ClampedArray = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble()).data

    // Mirrors desktop's ImageIO decode path (Bitmap.kt): flip Y so row 0 is the bottom row
    // (this engine's GPU texture-origin convention), and pack ARGB the same way
    // BufferedImage.getRGB does, so every createBitmap actual hands back the same layout.
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        val flippedY = height - 1 - y
        for (x in 0 until width) {
            val offset = (flippedY * width + x) * 4
            val r = data[offset].toInt() and 0xFF
            val g = data[offset + 1].toInt() and 0xFF
            val b = data[offset + 2].toInt() and 0xFF
            val a = data[offset + 3].toInt() and 0xFF
            pixels[y * width + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    return DefaultBitmap(width, height, 4, pixels)
}
