package io.github.ronjunevaldoz.awake.core.rendering

import io.github.aakira.napier.Napier
import io.github.ronjunevaldoz.awake.core.geometry.Attribute

interface BufferObject {
    val id: Int
    fun create()
    fun bind()
    fun unbind()
    fun delete()
}

/**
 * Vertex Buffer Data
 * @param index attribute index (attribute position)
 * @param size attribute size (n components * data type size; (ex: x,y * Float.SizeBytes))
 * @param data attribute data (vertices)
 */
data class VertexBufferData(
    val index: Int,
    val size: Int,
    val data: List<Float>,
    val stride: Int,
    val offset: Int
)

/**
 * Index Buffer Object
 */
interface IndexBufferData {
    val elements: MutableList<Byte>
}

interface VertexArrayData {
    val id: Int
    val indexBuffer: ElementBufferObject
    val vertexBuffer: LinkedHashSet<VertexBufferObject>
}

fun VertexArrayData.addVertexBuffer(attribute: Attribute) {
    with(attribute) {
        addVertexBuffer(location, size, data)
    }
}

fun VertexArrayData.addVertexBuffer(
    index: Int,
    size: Int,
    data: FloatArray,
    stride: Int = 0,
    offset: Int = 0
) {
    vertexBuffer.add(
        VertexBufferObject(
            VertexBufferData(
                index,
                size,
                data.toList(),
                stride,
                offset
            )
        )
    )
}

fun VertexArrayData.addIndexBuffer(elements: ByteArray) {
    indexBuffer.elements.clear()
    indexBuffer.elements.addAll(elements.toList())
}

fun VertexArrayObject.build(): VertexArrayObject {
    Napier.d("building VAO")
    create()
    bind()
    indexBuffer.build {
    }
    for (vertexBuffer in vertexBuffer) {
        vertexBuffer.build()
    }
    unbind()
    return this
}

fun VertexBufferObject.build() {
    Napier.d("building VBO")
    create()
    bind()
    storeData()
    unbind()
}

fun ElementBufferObject.build(block: () -> Unit) {
    if (elements.isEmpty()) return
    Napier.d("building EBO")
    create()
    bind()
    storeData()
    block()
//    unbind()
}

fun createVAO(block: VertexArrayData.() -> Unit): VertexArrayObject {
    return VertexArrayObject().apply(block).build()
}

fun BufferObject.use(block: () -> Unit) {
    bind()
    block()
    unbind()
}