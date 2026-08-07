// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

data class RgbaSample(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int,
)

data class PixelProbeSummary(
    val width: Int,
    val height: Int,
    val center: RgbaSample,
    val topLeft: RgbaSample,
    val topRight: RgbaSample,
    val bottomLeft: RgbaSample,
    val bottomRight: RgbaSample,
)

fun summarizePixels(pixels: ByteArray, width: Int, height: Int): PixelProbeSummary {
    require(width > 0 && height > 0) { "width and height must be positive" }
    require(pixels.size == width * height * 4) {
        "Pixel buffer size mismatch: actual=${pixels.size} bytes, expected=${width * height * 4} bytes"
    }
    return PixelProbeSummary(
        width = width,
        height = height,
        center = sampleRgba(pixels, width, height, width / 2, height / 2),
        topLeft = sampleRgba(pixels, width, height, 0, 0),
        topRight = sampleRgba(pixels, width, height, width - 1, 0),
        bottomLeft = sampleRgba(pixels, width, height, 0, height - 1),
        bottomRight = sampleRgba(pixels, width, height, width - 1, height - 1),
    )
}

private fun sampleRgba(
    pixels: ByteArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): RgbaSample {
    val clampedX = x.coerceIn(0, width - 1)
    val clampedY = y.coerceIn(0, height - 1)
    val offset = (clampedY * width + clampedX) * 4
    return RgbaSample(
        r = pixels[offset].toInt() and 0xFF,
        g = pixels[offset + 1].toInt() and 0xFF,
        b = pixels[offset + 2].toInt() and 0xFF,
        a = pixels[offset + 3].toInt() and 0xFF,
    )
}
