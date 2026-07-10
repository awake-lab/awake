package io.github.ronjunevaldoz.awake.core.memory

expect interface Buffer {
    val size: Int
    fun clear()
}

interface ByteBuf : Buffer {
    operator fun get(index: Int): Byte
    operator fun set(index: Int, value: Byte)
    fun put(data: ByteArray)
}
interface ShortBuf : Buffer {
    operator fun get(index: Int): Short
    operator fun set(index: Int, value: Short)
    fun put(data: ShortArray)
}
interface IntBuf : Buffer {
    operator fun get(index: Int): Int
    operator fun set(index: Int, value: Int)
    fun put(data: IntArray)
}
interface FloatBuf : Buffer {
    operator fun get(index: Int): Float
    operator fun set(index: Int, value: Float)
    fun put(data: FloatArray)
}

expect fun createByteBuffer(data: ByteArray) : ByteBuf
expect fun createShortBuffer(data: ShortArray) : ShortBuf
expect fun createIntBuffer(data: IntArray) : IntBuf
expect fun createFloatBuffer(data: FloatArray) : FloatBuf