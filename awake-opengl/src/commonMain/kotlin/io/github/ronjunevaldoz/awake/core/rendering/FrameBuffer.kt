package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.memory.ByteBuf
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils


class FrameBuffer(private val width: Int, private val height: Int) : BufferObject,
    Disposable {
    override var id: Int = -1
    private var texture: Texture? = null
    private val rbo = RenderBufferObject()

    override fun create() {
        // create buffer
        id = gl.genFrameBuffers()
        // bind framebuffer
        use {
            // Create a texture for rendering
            texture = Texture.load(width, height).apply {
                // store texture to frame buffer
                gl.framebufferTexture2D(
                    CommonGL.GL_FRAMEBUFFER,
                    CommonGL.GL_COLOR_ATTACHMENT0,
                    CommonGL.GL_TEXTURE_2D,
                    id, // texture id
                    0
                )
            }
            // Create a renderBuffer for depth and stencil
            rbo.create()
            rbo.bind()
            rbo.storeData(width, height)
            // Check if the frameBuffer is complete
            checkComplete()
        }
        // Unbind the framebuffer
    }

    override fun bind() {
        gl.bindFramebuffer(CommonGL.GL_FRAMEBUFFER, id)
    }

    override fun unbind() {
        gl.bindFramebuffer(CommonGL.GL_FRAMEBUFFER, 0)
    }

    fun getPixelBuffer(): ByteBuf {
        val pixelBuffer = BufferUtils.allocateByte(width * height * 4)
        use {
            gl.readPixels(
                0,
                0,
                width,
                height,
                CommonGL.GL_RGBA,
                CommonGL.GL_UNSIGNED_BYTE,
                pixelBuffer
            )
        }
        return pixelBuffer
    }

    override fun delete() {
        // Release resources
        gl.deleteFrameBuffers(id)
        rbo.delete()
        texture?.delete()
    }

    override fun dispose() {
        delete()
    }

    private fun checkComplete() {
        val status = gl.checkFramebufferStatus(CommonGL.GL_FRAMEBUFFER)
        check(status == CommonGL.GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is not complete!"
        }
    }
}