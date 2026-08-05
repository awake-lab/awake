// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.graphics

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual fun createBitmap(bytes: ByteArray): Bitmap {
    return decodeBitmap(bytes)
}

fun decodeBitmap(bytes: ByteArray): Bitmap {
    val bufferedImage = ImageIO.read(ByteArrayInputStream(bytes))
    val width = bufferedImage.width
    val height = bufferedImage.height
    val channel = bufferedImage.colorModel.numComponents
    val flipY = true
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val flippedY = if (flipY) height - 1 - y else y
            val color = bufferedImage.getRGB(x, flippedY)
            pixels[y * width + x] = color
        }
    }

    return DefaultBitmap(width, height, channel, pixels)
}
