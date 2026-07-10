package io.github.ronjunevaldoz.awake.core.fonts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import io.github.ronjunevaldoz.awake.core.graphics.AndroidBitmap
import io.github.ronjunevaldoz.awake.core.rendering.Texture

class FontBitmap(fontSize: Float, antialias: Boolean) {
    private val textPaint = TextPaint()
    private val canvas = Canvas()
    val texture: Texture

    init {
        texture = create("Sample bitmap", fontSize)
    }

    private fun create(
        text: String,
        fontSize: Float
    ): Texture {
        textPaint.apply {
            typeface = Typeface.SERIF
            textSize = fontSize
            isAntiAlias = true
            color = Color.WHITE
            hinting = Paint.HINTING_ON
            isSubpixelText = true
        }

        val width = (textPaint.measureText(text) + 2.0f).toInt()
        val height = fontSize.toInt() + 2

        val staticLayout = StaticLayout.Builder.obtain(
            text, 0, text.length,
            textPaint, width
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .build()

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        canvas.setBitmap(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        staticLayout.draw(canvas)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val androidBitmap = AndroidBitmap(width, height, 4, pixels)
        val texture = Texture.load(
            bitmap = androidBitmap,
            options = Texture.Options(
                mag = Texture.Filter.Linear,
                min = Texture.Filter.MipmapLinear,
                wrapS = Texture.Wrap.ClampToEdge,
                wrapT = Texture.Wrap.ClampToEdge
            ),
            mipmap = true
        )
        bitmap.recycle()
        return texture
    }
}