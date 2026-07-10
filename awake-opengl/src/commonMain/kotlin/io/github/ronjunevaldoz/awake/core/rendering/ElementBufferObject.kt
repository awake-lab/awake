package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils

class ElementBufferObject : BufferObject, IndexBufferData {
    override var id: Int = -1
    override val elements: MutableList<Byte> = mutableListOf()

    override fun create() {
        id = AwakeContext.gl.genBuffers()
    }

    override fun bind() {
        AwakeContext.gl.bindBuffer(OpenGL.BufferType.ElementArray, id)
    }

    override fun unbind() {
        AwakeContext.gl.bindBuffer(OpenGL.BufferType.ElementArray, 0)
    }

    fun storeData() {
        AwakeContext.gl.bufferData(
            OpenGL.BufferType.ElementArray,
            BufferUtils.allocateByte(elements.toByteArray()),
            OpenGL.DrawType.Static
        )
    }

    override fun delete() {
        AwakeContext.gl.deleteBuffers(id)
    }
}