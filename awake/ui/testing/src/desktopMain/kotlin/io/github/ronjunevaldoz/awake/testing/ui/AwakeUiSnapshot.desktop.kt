// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

actual fun loadAwakeUiSnapshot(id: String): ByteArray? {
    val rootDir = System.getProperty("AWAKE_SNAPSHOT_ROOT") ?: ""
    val file = File(rootDir, "snapshots/ui/$id.png")
    if (!file.exists()) return null
    val image = ImageIO.read(file) ?: return null
    val width = image.width
    val height = image.height
    val pixels = ByteArray(width * height * 4)
    var offset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val argb = image.getRGB(x, y)
            pixels[offset] = ((argb shr 16) and 0xFF).toByte()
            pixels[offset + 1] = ((argb shr 8) and 0xFF).toByte()
            pixels[offset + 2] = (argb and 0xFF).toByte()
            pixels[offset + 3] = ((argb shr 24) and 0xFF).toByte()
            offset += 4
        }
    }
    return pixels
}

actual fun saveAwakeUiSnapshot(id: String, pixels: ByteArray, width: Int, height: Int) {
    val rootDir = System.getProperty("AWAKE_SNAPSHOT_ROOT") ?: ""
    val dir = File(rootDir, "snapshots/ui").apply { mkdirs() }
    val file = File(dir, "$id.png")
    saveImage(file, pixels, width, height)
}

actual fun saveAwakeUiDiff(id: String, pixels: ByteArray, width: Int, height: Int) {
    val dir = File("build/ui-previews").apply { mkdirs() }
    val file = File(dir, "${id}_diff.png")
    saveImage(file, pixels, width, height)
}

private fun saveImage(file: File, pixels: ByteArray, width: Int, height: Int) {
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
    ImageIO.write(image, "png", file)
}
