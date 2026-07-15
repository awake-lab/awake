// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

actual fun saveDebugPng(pixels: ByteArray, width: Int, height: Int, path: String) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    var offset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val r = pixels[offset].toInt() and 0xFF
            val g = pixels[offset + 1].toInt() and 0xFF
            val b = pixels[offset + 2].toInt() and 0xFF
            val a = pixels[offset + 3].toInt() and 0xFF
            image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            offset += 4
        }
    }
    ImageIO.write(image, "png", File(path))
    println("DEBUG PNG: wrote $width x $height image to $path")
}
