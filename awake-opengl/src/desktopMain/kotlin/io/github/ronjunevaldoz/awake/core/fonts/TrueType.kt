package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.rendering.Texture
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap
import org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics
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

            var lineStart = 0

            val factorX: Float = 1.0f / contentScaleX
            val factorY: Float = 1.0f / contentScaleY

            val lineY = 0.0f

            var i = 0
            val to = text.length

//            glBegin(GL_QUADS)
            while (i < to) {
                i += getCP(text, to, i, pCodePoint)

                val cp = pCodePoint[0]
                if (cp == '\n'.code) {
//                    if (isLineBBEnabled()) {
//                        glEnd()
//                        renderLineBB(lineStart, i - 1, y[0], scale)
//                        glBegin(GL_QUADS)
//                    }

                    y.put(0, lineY + (metrics.ascent - metrics.descent + metrics.lineGap) * scale)
                    x.put(0, 0.0f)

                    lineStart = i
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

                val x0 = scale(cpX, q.x0(), factorX)
                val x1 = scale(cpX, q.x1(), factorX)
                val y0 = scale(lineY, q.y0(), factorY)
                val y1 = scale(lineY, q.y1(), factorY)

                val textCoords = floatArrayOf(
                    q.s0(), q.t0(),
                    q.s1(), q.t0(),
                    q.s1(), q.t1(),
                    q.s0(), q.t1()
                )
                val position = floatArrayOf(
                    x0, y0,
                    x1, y0,
                    x1, y1,
                    x0, y1
                )
            }
//            glEnd();
//            if (isLineBBEnabled()) {
//                renderLineBB(lineStart, text.length(), lineY, scale);
//            }
        }
    }

    private fun scale(center: Float, offset: Float, factor: Float): Float {
        return (offset - center) * factor + center
    }

    private fun getStringWidth(
        info: STBTTFontinfo,
        text: String,
        from: Int,
        to: Int,
        fontHeight: Int
    ): Float {
        var width = 0
        stackPush().use { stack ->
            val pCodePoint = stack.mallocInt(1)
            val pAdvancedWidth = stack.mallocInt(1)
            val pLeftSideBearing = stack.mallocInt(1)
            var i = from
            while (i < to) {
                i += getCP(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing)
                width += pAdvancedWidth[0]
                if (isKerningEnabled && i < to) {
                    getCP(text, to, i, pCodePoint)
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint[0])
                }
            }
        }
        return width * stbtt_ScaleForPixelHeight(info, fontHeight.toFloat())
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