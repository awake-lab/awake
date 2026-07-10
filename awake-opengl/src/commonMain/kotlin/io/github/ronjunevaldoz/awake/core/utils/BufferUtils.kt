package io.github.ronjunevaldoz.awake.core.utils

import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.ByteBuf
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuf
import io.github.ronjunevaldoz.awake.core.memory.ShortBuf
import io.github.ronjunevaldoz.awake.core.memory.createByteBuffer
import io.github.ronjunevaldoz.awake.core.memory.createFloatBuffer
import io.github.ronjunevaldoz.awake.core.memory.createIntBuffer
import io.github.ronjunevaldoz.awake.core.memory.createShortBuffer

object BufferUtils {
    fun allocateByte(size: Int): ByteBuf = createByteBuffer(ByteArray(size))
    fun allocateShort(size: Int): ShortBuf = createShortBuffer(ShortArray(size))
    fun allocateInt(size: Int): IntBuf = createIntBuffer(IntArray(size))
    fun allocateFloat(size: Int): FloatBuf = createFloatBuffer(FloatArray(size))
    fun allocateByte(data: ByteArray): ByteBuf = createByteBuffer(data)
    fun allocateShort(data: ShortArray): ShortBuf = createShortBuffer(data)
    fun allocateInt(data: IntArray): IntBuf = createIntBuffer(data)
    fun allocateFloat(data: FloatArray): FloatBuf = createFloatBuffer(data)
}

fun Buffer.toUType(): Int {
    return when (this) {
        is ByteBuf -> CommonGL.GL_UNSIGNED_BYTE
        is IntBuf -> CommonGL.GL_UNSIGNED_INT
        is ShortBuf -> CommonGL.GL_UNSIGNED_SHORT
        else -> throw Exception("Unsupported buffer type")
    }
}

fun Buffer.sizeBytes(): Int {
    return when (this) {
        is ByteBuf -> Byte.SIZE_BYTES
        is IntBuf -> Int.SIZE_BYTES
        is ShortBuf -> Short.SIZE_BYTES
        is FloatBuf -> Float.SIZE_BYTES
        else -> throw Exception("Unsupported buffer type")
    }
}