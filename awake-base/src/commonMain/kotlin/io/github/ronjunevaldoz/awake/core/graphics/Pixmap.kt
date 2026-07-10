package io.github.ronjunevaldoz.awake.core.graphics

class Pixmap(val width: Int, val height: Int, val format: Format = Format.RGBA) {
    val pixels = IntArray(width * height)

    init {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val alpha = 0xFF // 0xFF = 255
                val red = 0x00 // 0x00 = 0
                val green = 0x00
                val blue = 0x00
                val pixel = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                pixels[y * width + x] = pixel
            }
        }
    }

    operator fun get(index: Int): Int {
        return pixels[index]
    }

    operator fun get(x: Int, y: Int): Int {
        return pixels[y * width + x]
    }

    operator fun set(index: Int, color: Int) {
        pixels[index] = color
    }

    operator fun set(x: Int, y: Int, color: Int) {
        pixels[y * width + x] = color
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        this[x, y] = color
    }

    fun getPixel(x: Int, y: Int): Int {
        return this[x, y]
    }

    enum class Format {
        RED,
        RGB,
        RGBA
    }
}

fun Pixmap.toBitmap(): Bitmap {
    return DefaultBitmap(width, height, 4, pixels)
}