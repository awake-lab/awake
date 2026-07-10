package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.core.graphics.DefaultBitmap
import kotlin.test.Test
import kotlin.test.assertEquals

class TextureLoaderTest {
    @Test
    fun unpacksArgbIntoRedGreenBlueAlphaByteOrder() {
        val alpha = 0xAA
        val red = 0x11
        val green = 0x22
        val blue = 0x33
        val argb = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

        val bitmap = DefaultBitmap(width = 1, height = 1, channel = 4, pixels = intArrayOf(argb))
        val rgba = bitmap.toRgba8Bytes()

        assertEquals(4, rgba.size)
        assertEquals(red.toByte(), rgba[0])
        assertEquals(green.toByte(), rgba[1])
        assertEquals(blue.toByte(), rgba[2])
        assertEquals(alpha.toByte(), rgba[3])
    }

    @Test
    fun convertsEveryPixelInRowMajorOrder() {
        val firstPixel = 0xFF_10_20_30.toInt()
        val secondPixel = 0xFF_40_50_60.toInt()
        val bitmap = DefaultBitmap(width = 2, height = 1, channel = 4, pixels = intArrayOf(firstPixel, secondPixel))

        val rgba = bitmap.toRgba8Bytes()

        assertEquals(8, rgba.size)
        assertEquals(0x10.toByte(), rgba[0])
        assertEquals(0x40.toByte(), rgba[4])
    }
}
