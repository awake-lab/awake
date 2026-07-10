package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.graphics.AppleBitmap
import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.Pixmap
import io.github.ronjunevaldoz.awake.core.math.Vec2
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.Pixmap as SkiaPixmap


class FontBitmap(private val font: Font, antiAlias: Boolean) {
    val bitmap: Bitmap
    private val glyphs = mutableMapOf<Char, Glyph>()

    constructor(fontSize: Float, antiAlias: Boolean) : this(
        Font(Typeface.makeDefault(), fontSize),
        antiAlias
    )

    init {
        val characters = ((32 until 127)).map { it.toChar() }
        val bitmaps = characters.mapNotNull {
            createCharBitmap(font, it, antiAlias)
        }
        val columns = 12
        val rows = (bitmaps.size + columns - 1) / columns

        val maxBitmapWidth = bitmaps.maxOf { it.width }
        val maxBitmapHeight = bitmaps.maxOf { it.height }

        val totalWidth = columns * maxBitmapWidth
        val totalHeight = rows * maxBitmapHeight

        // texture atlas?
        val atlasPixmap = Pixmap(totalWidth, totalHeight, Pixmap.Format.RGBA)

        for (bitmapIndex in bitmaps.indices) {
            val row = bitmapIndex / columns
            val col = bitmapIndex % columns
            val bitmap = bitmaps[bitmapIndex]

            // Calculate glyph properties
            val code = characters[bitmapIndex].code
            val size = Vec2(bitmap.width.toFloat(), bitmap.height.toFloat())
            val bearing = Vec2(0f, bitmap.height.toFloat()) // Assuming baseline is at the bottom
            val advance =
                maxBitmapWidth // Assuming the glyph advance is equal to the max bitmap width

            // Create the Glyph object
            val glyph = Glyph(code, size, bearing, advance)

            // Add the glyph to the list
            glyphs[characters[bitmapIndex]] = glyph

            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    val index =
                        (y + row * maxBitmapHeight) * totalWidth + (x + col * maxBitmapWidth)
                    val newColor = bitmap.pixels[y * bitmap.width + x]
                    atlasPixmap.pixels[index] = newColor
                }
            }
        }
        bitmap = AppleBitmap(totalWidth, totalHeight, 4, atlasPixmap.pixels)
    }

    companion object {
        private val pixmap = SkiaPixmap()
        private val paint = Paint()
        fun createCharPixmap(
            font: Font,
            c: Char,
            antiAlias: Boolean
        ): SkiaPixmap? {
            // Get character width and height
            val charWidth = font.measureTextWidth(c.toString())
            val charHeight = font.size

            // Check if charWidth is 0
            if (charWidth == 0f) return null
            // Calculate padding and surface size
            val surfaceWidth = charWidth.toInt() + 2
            val surfaceHeight = charHeight.toInt() + 2

            // Create surface for holding the character
            val charSurface = Surface.makeRasterN32Premul(surfaceWidth, surfaceHeight)
            return charSurface.use {
                val charCanvas = it.canvas

                charCanvas.clear(Color.TRANSPARENT)

                paint.reset()
                paint.isAntiAlias = antiAlias
                paint.color = Color.WHITE

                val x = 1f
                val y = -font.metrics.ascent + 1

                charCanvas.drawString(c.toString(), x, y, font, paint)

                pixmap.reset()
                it.peekPixels(pixmap)
                pixmap
            }
        }

        fun createCharBitmap(font: Font, c: Char, antiAlias: Boolean): Bitmap? {
            val pixmap = createCharPixmap(font, c, antiAlias) ?: return null
            val width = pixmap.info.width
            val height = pixmap.info.height
            val channel = pixmap.info.bytesPerPixel
            return AppleBitmap(width, height, channel, getPixels(pixmap, true))
        }

        private fun getPixels(pixmap: SkiaPixmap, flipY: Boolean): IntArray {
            val width = pixmap.info.width
            val height = pixmap.info.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val flippedY = if (flipY) height - 1 - y else y
                    val argb = pixmap.getColor(x, flippedY)
                    val alpha = (argb shr 24) and 0xFF
                    val red = (argb shr 16) and 0xFF
                    val green = (argb shr 8) and 0xFF
                    val blue = argb and 0xFF
                    val pixel = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                    pixels[y * width + x] = pixel
                }
            }
            return pixels
        }
    }
}