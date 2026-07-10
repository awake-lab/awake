package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL

class RenderBufferObject : BufferObject {
    override var id: Int = -1

    override fun create() {
        id = AwakeContext.gl.genRenderBuffers()
    }

    override fun bind() {
        AwakeContext.gl.bindRenderBuffers(CommonGL.GL_RENDERBUFFER, id)
    }

    fun storeData(width: Int, height: Int) {
        AwakeContext.gl.renderBufferStorage(
            CommonGL.GL_RENDERBUFFER,
            CommonGL.GL_DEPTH24_STENCIL8,
            width,
            height
        )
        AwakeContext.gl.framebufferRenderBuffer(
            CommonGL.GL_FRAMEBUFFER,
            CommonGL.GL_DEPTH_STENCIL_ATTACHMENT,
            CommonGL.GL_RENDERBUFFER,
            id
        )
    }

    override fun unbind() {
        AwakeContext.gl.bindRenderBuffers(CommonGL.GL_RENDERBUFFER, 0)
    }

    override fun delete() {
        AwakeContext.gl.deleteRenderBuffers(id)
    }

}