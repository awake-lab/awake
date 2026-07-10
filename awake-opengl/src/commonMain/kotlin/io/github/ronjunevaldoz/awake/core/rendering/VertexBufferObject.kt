package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils

class VertexBufferObject(val data: VertexBufferData) : BufferObject {
    override var id: Int = -1

    override fun create() {
        id = AwakeContext.gl.genBuffers()
    }

    override fun bind() {
        AwakeContext.gl.bindBuffer(OpenGL.BufferType.Array, id)
    }

    override fun unbind() {
        AwakeContext.gl.bindBuffer(OpenGL.BufferType.Array, 0)
    }

    fun enableAttribute() {
        AwakeContext.gl.enableVertexAttribArray(data.index)
    }

    fun disableAttribute() {
        AwakeContext.gl.disableVertexAttribArray(data.index)
    }

    fun storeData() {
        with(data) {
            val bufferSize = data.size * Float.SIZE_BYTES
            val buffer = BufferUtils.allocateFloat(bufferSize)
            buffer.put(data.toFloatArray())
            AwakeContext.gl.bufferData(
                OpenGL.BufferType.Array,
                buffer,
                OpenGL.DrawType.Static
            )
            AwakeContext.gl.vertexAttribPointer(
                index,
                size,
                CommonGL.GL_FLOAT,
                false,
                0,
                offset
            )
        }
    }

    override fun delete() {
        AwakeContext.gl.deleteBuffers(id)
    }
}