package io.github.ronjunevaldoz.awake.core.memory

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned


actual interface Buffer {
    fun <C : CPointed> get(): CValuesRef<C>
    actual val size: Int
    actual fun clear()
}

class ByteBuffer(size: Int) : ByteBuf {
    private val buffer = ByteArray(size)

    constructor(data: ByteArray) : this(data.size) {
        data.copyInto(this.buffer)
    }

    override fun get(index: Int): Byte {
        return buffer[index]
    }

    override fun set(index: Int, value: Byte) {
        buffer[index] = value
    }

    override fun put(data: ByteArray) {
        data.copyInto(this.buffer)
    }

    override fun <C : CPointed> get(): CValuesRef<C> {
        return buffer.usePinned { it.addressOf(0).reinterpret() }
    }

    override val size: Int = buffer.size

    override fun clear() {
        // TODO clear buffer on iOS
    }
}

class ShortBuffer(size: Int) : ShortBuf {
    private val buffer = ShortArray(size)

    constructor(data: ShortArray) : this(data.size) {
        data.copyInto(buffer)
    }

    override fun get(index: Int): Short {
        return buffer[index]
    }

    override fun set(index: Int, value: Short) {
        buffer[index] = value
    }

    override fun put(data: ShortArray) {
        data.copyInto(buffer)
    }

    override fun <C : CPointed> get(): CValuesRef<C> {
        return buffer.usePinned { it.addressOf(0).reinterpret() }
    }

    override val size: Int = buffer.size

    override fun clear() {
        // TODO clear buffer on iOS
    }
}

class IntBuffer(size: Int) : IntBuf {
    private val buffer = IntArray(size)

    constructor(data: IntArray) : this(data.size) {
        data.copyInto(buffer)
    }

    override fun get(index: Int): Int {
        return buffer[index]
    }

    override fun set(index: Int, value: Int) {
        buffer[index] = value
    }

    override fun put(data: IntArray) {
        data.copyInto(buffer)
    }

    override fun <C : CPointed> get(): CValuesRef<C> {
        return buffer.usePinned { it.addressOf(0).reinterpret() }
    }

    override val size: Int = buffer.size

    override fun clear() {
        // TODO clear buffer on iOS
    }
}

class FloatBuffer(size: Int) : FloatBuf {
    private val buffer = FloatArray(size)

    constructor(data: FloatArray) : this(data.size) {
        data.copyInto(buffer)
    }

    override fun get(index: Int): Float {
        return buffer[index]
    }

    override fun set(index: Int, value: Float) {
        buffer[index] = value
    }

    override fun put(data: FloatArray) {
        data.copyInto(buffer)
    }

    override fun <C : CPointed> get(): CValuesRef<C> {
        return buffer.usePinned { it.addressOf(0).reinterpret() }
    }

    override val size: Int = buffer.size

    override fun clear() {
        // TODO clear buffer on iOS
    }
}

actual fun createByteBuffer(data: ByteArray): ByteBuf {
    return ByteBuffer(data)
}

actual fun createShortBuffer(data: ShortArray): ShortBuf {
    return ShortBuffer(data)
}

actual fun createIntBuffer(data: IntArray): IntBuf {
    return IntBuffer(data)
}

actual fun createFloatBuffer(data: FloatArray): FloatBuf {
    return FloatBuffer(data)
}