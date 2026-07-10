package io.github.ronjunevaldoz.awake.core.rendering

import io.github.ronjunevaldoz.awake.core.AwakeContext

class VertexArrayObject : VertexArrayData, BufferObject {
    override var id: Int = -1
    override val indexBuffer: ElementBufferObject = ElementBufferObject()
    override val vertexBuffer: LinkedHashSet<VertexBufferObject> = LinkedHashSet()

    override fun create() {
        id = AwakeContext.gl.genVertexArrays()
    }

    override fun bind() {
        // bind vertex array object
        AwakeContext.gl.bindVertexArray(id)
        // enable vertex attrib array
        enableAttributes()
    }

    override fun unbind() {
        // disable vertex attrib array
        disableAttributes()
        // unbind vertex attrib array
        AwakeContext.gl.bindVertexArray(0)
    }

    private fun enableAttributes() {
        vertexBuffer.forEach {
            it.enableAttribute()
        }
    }

    private fun disableAttributes() {
        vertexBuffer.forEach {
            it.disableAttribute()
        }
    }

    override fun delete() {
        indexBuffer.delete()
        vertexBuffer.forEach {
            it.delete()
        }
        AwakeContext.gl.deleteVertexArrays(id)
    }
}