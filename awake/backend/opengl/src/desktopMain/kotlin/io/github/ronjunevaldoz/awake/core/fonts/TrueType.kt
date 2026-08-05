// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.rendering.Texture
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap
import org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance
import org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics
import org.lwjgl.stb.STBTruetype.stbtt_InitFont
import org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.ByteBuffer
import java.nio.IntBuffer


class NativeTrueType(filePath: String, fontSize: Float) : TrueType {
    private val ttf = readResource(filePath)
    private val info: STBTTFontinfo = STBTTFontinfo.create()

    private val metrics: FontMetrics

    private val cdata = STBTTBakedChar.malloc(96)
    override val texture: Texture


    val fontHeight = fontSize
    val contentScaleX = 1f
    val contentScaleY = 1f

    var isKerningEnabled = true

    private val bitmapWidth: Int
        get() = (256 * contentScaleX).toInt()
    private val bitmapHeight: Int
        get() = (256 * contentScaleY).toInt()


    init {
        require(stbtt_InitFont(info, ttf)) { "Failed to init font info" }
        stackPush().use { stack ->
            val pAscent = stack.mallocInt(1)
            val pDescent = stack.mallocInt(1)
            val pLineGap = stack.mallocInt(1)
            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)
            metrics = FontMetrics(
                ascent = pAscent.get(0),
                descent = pDescent.get(0),
                lineGap = pLineGap.get(0)
            )
        }
        texture = createTexture(bitmapWidth, bitmapHeight)
    }

    override fun dispose() {
        cdata.free()
        ttf.clear()
        texture.delete()
        info.free()
    }

    fun createTexture(bitmapWidth: Int, bitmapHeight: Int): Texture {
        val buffer = BufferUtils.allocateByte(bitmapWidth * bitmapHeight)
        stbtt_BakeFontBitmap(
            ttf,
            fontHeight * contentScaleY,
            buffer.get(),
            bitmapWidth,
            bitmapHeight,
            32,
            cdata
        )
        return Texture.load(
            width = bitmapWidth,
            height = bitmapHeight,
            buffer = buffer,
        )
    }

    override fun drawText(text: String) {
        val scale = stbtt_ScaleForPixelHeight(info, fontHeight)
        stackPush().use { stack ->
            val pCodePoint = stack.mallocInt(1)

            val x = stack.floats(0.0f)
            val y = stack.floats(0.0f)

            val q = STBTTAlignedQuad.malloc(stack)

            val factorX: Float = 1.0f / contentScaleX

            val lineY = 0.0f

            var i = 0
            val to = text.length
            while (i < to) {
                i += getCP(text, to, i, pCodePoint)

                val cp = pCodePoint[0]
                if (cp == '\n'.code) {
                    y.put(0, lineY + (metrics.ascent - metrics.descent + metrics.lineGap) * scale)
                    x.put(0, 0.0f)
                } else if (cp < 32 || 128 <= cp) {
                    continue
                }

                val cpX = x[0]
                stbtt_GetBakedQuad(cdata, bitmapWidth, bitmapHeight, cp - 32, x, y, q, true)
                x.put(0, scale(cpX, x[0], factorX))
                if (isKerningEnabled && i < to) {
                    getCP(text, to, i, pCodePoint)
                    x.put(0, x[0] + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint[0]) * scale)
                }

            }
        }
    }

    private fun scale(center: Float, offset: Float, factor: Float): Float {
        return (offset - center) * factor + center
    }

    private fun getCP(text: String, to: Int, i: Int, cpOut: IntBuffer): Int {
        val c1 = text[i]
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            val c2 = text[i + 1]
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2))
                return 2
            }
        }
        cpOut.put(0, c1.code)
        return 1
    }

    private fun readResource(path: String): ByteBuffer {
        return this::class.java.classLoader?.getResourceAsStream(path)?.use {
            it.readBytes().let { bytes ->
                BufferUtils.allocateByte(bytes.size).apply {
                    put(bytes)
                }.get()
            }
        } ?: throw Exception("$path not found")
    }
}

actual fun createTrueType(path: String, size: Float): TrueType {
    return NativeTrueType(path, size)
}
