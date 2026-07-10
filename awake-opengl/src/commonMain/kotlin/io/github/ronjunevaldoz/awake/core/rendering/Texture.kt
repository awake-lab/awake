package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.createIntBuffer
import io.github.ronjunevaldoz.awake.core.utils.toUType

fun Int.toFormat(): Int {
    return when (this) {
        CommonGL.GL_RED, CommonGL.GL_R8, CommonGL.GL_R16F, CommonGL.GL_R32F -> CommonGL.GL_RED
        CommonGL.GL_RG, CommonGL.GL_RG8, CommonGL.GL_RG16F, CommonGL.GL_RG32F -> CommonGL.GL_RG
        CommonGL.GL_RGB, CommonGL.GL_RGB8, CommonGL.GL_RGB16F, CommonGL.GL_RGB32F -> CommonGL.GL_RGB
        CommonGL.GL_RGBA, CommonGL.GL_RGBA8, CommonGL.GL_RGBA16F, CommonGL.GL_RGBA32F -> CommonGL.GL_RGBA
        else -> throw IllegalArgumentException("Unsupported internal format: $this")
    }
}

class Texture(
    val width: Int,
    val height: Int,
    internalFormat: Int = CommonGL.GL_RGBA8,
    private val options: Options = Options(),
    private val mipmap: Boolean = false,
    private val buffer: Buffer? = null
) : BufferObject {
    override var id: Int = -1
    val format = internalFormat.toFormat()

    enum class Filter(val value: Int) {
        Linear(CommonGL.GL_LINEAR),
        MipmapLinear(CommonGL.GL_LINEAR_MIPMAP_LINEAR),
        Nearest(CommonGL.GL_NEAREST),
    }

    enum class Wrap(val value: Int) {
        Repeat(CommonGL.GL_REPEAT),
        ClampToEdge(CommonGL.GL_CLAMP_TO_EDGE),
        MirroredRepeat(CommonGL.GL_MIRRORED_REPEAT),
    }

    data class Options(
        val wrapS: Wrap = Wrap.Repeat,
        val wrapT: Wrap = Wrap.Repeat,
        val min: Filter = Filter.Linear,
        val mag: Filter = Filter.Linear
    )

    fun setParameter(name: Int, value: Int) {
        gl.texParameteri(
            CommonGL.GL_TEXTURE_2D,
            name,
            value
        )
    }

    private fun withOptions(image: () -> Unit) {
        create()
        use {
            image()
            with(options) {
                setParameter(
                    CommonGL.GL_TEXTURE_WRAP_S,
                    wrapS.value
                )
                setParameter(
                    CommonGL.GL_TEXTURE_WRAP_T,
                    wrapT.value
                )
                setParameter(
                    CommonGL.GL_TEXTURE_MIN_FILTER,
                    min.value
                )
                setParameter(
                    CommonGL.GL_TEXTURE_MAG_FILTER,
                    mag.value
                )
            }
        }
    }

    override fun create() {
        id = gl.genTextures()
    }

    override fun bind() {
        gl.bindTexture(CommonGL.GL_TEXTURE_2D, id)
    }

    override fun unbind() {
        gl.bindTexture(CommonGL.GL_TEXTURE_2D, 0)
    }

    override fun delete() {
        gl.deleteTextures(id)
    }

    fun setSubData(xOffset: Int = 0, yOffset: Int = 0, bitmap: Bitmap) {
        use {
            val buffer = createIntBuffer(bitmap.pixels)
            gl.texSubImage2D(
                target = CommonGL.GL_TEXTURE_2D,
                level = 0,
                xOffset = xOffset,
                yOffset = yOffset,
                width = width,
                height = height,
                format = format,
                type = buffer.toUType(),
                buffer
            )
            if (mipmap) {
                gl.generateMipmap(CommonGL.GL_TEXTURE_2D)
            }
        }
    }

    companion object {
        fun load(
            width: Int,
            height: Int,
            internalFormat: Int = CommonGL.GL_RGBA8,
            options: Options = Options(),
            mipmap: Boolean = false,
            buffer: Buffer? = null
        ): Texture {
            return Texture(width, height, internalFormat, options, mipmap, buffer).apply {
                withOptions {
                    gl.texImage2D(
                        target = CommonGL.GL_TEXTURE_2D,
                        level = 0,
                        internalFormat = internalFormat,
                        width = width,
                        height = height,
                        border = 0,
                        format = format,
                        type = buffer?.toUType() ?: CommonGL.GL_UNSIGNED_BYTE,
                        buffer
                    )
                    if (mipmap) {
                        gl.generateMipmap(CommonGL.GL_TEXTURE_2D)
                    }
                }
            }
        }

        fun load(
            bitmap: Bitmap,
            options: Options = Options(),
            internalFormat: Int = CommonGL.GL_RGBA8,
            mipmap: Boolean = false
        ): Texture {
            return Texture(
                bitmap.width,
                bitmap.height,
                options = options,
                buffer = null,
                mipmap = mipmap
            ).apply {
                withOptions {
                    gl.texImage2D(
                        CommonGL.GL_TEXTURE_2D,
                        0,
                        internalFormat = internalFormat,
                        bitmap,
                        0
                    )
                    if (mipmap) {
                        gl.generateMipmap(CommonGL.GL_TEXTURE_2D)
                    }
                }
            }
        }
    }

}