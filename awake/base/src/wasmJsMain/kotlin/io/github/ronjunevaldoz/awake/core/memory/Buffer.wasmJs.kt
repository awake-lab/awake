// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.memory

// No java.nio on wasmJs -- plain Kotlin-array-backed buffers. Simpler than desktopMain's
// direct ByteBuffer, but same contract (get/set/put/clear), which is all any caller uses.
class WasmJsByteBuffer(private val data: ByteArray) : ByteBuf {
    constructor(size: Int) : this(ByteArray(size))

    override fun get(index: Int): Byte = data[index]
    override fun set(index: Int, value: Byte) {
        data[index] = value
    }
    override fun put(data: ByteArray) {
        data.copyInto(this.data)
    }
    override val size: Int = data.size
    override fun clear() = data.fill(0)
    @Suppress("UNCHECKED_CAST")
    override fun <B : Buffer> get(): B = data as B
}

class WasmJsShortBuffer(private val data: ShortArray) : ShortBuf {
    constructor(size: Int) : this(ShortArray(size))

    override fun get(index: Int): Short = data[index]
    override fun set(index: Int, value: Short) {
        data[index] = value
    }
    override fun put(data: ShortArray) {
        data.copyInto(this.data)
    }
    override val size: Int = data.size
    override fun clear() = data.fill(0)
    @Suppress("UNCHECKED_CAST")
    override fun <B : Buffer> get(): B = data as B
}

class WasmJsIntBuffer(private val data: IntArray) : IntBuf {
    constructor(size: Int) : this(IntArray(size))

    override fun get(index: Int): Int = data[index]
    override fun set(index: Int, value: Int) {
        data[index] = value
    }
    override fun put(data: IntArray) {
        data.copyInto(this.data)
    }
    override val size: Int = data.size
    override fun clear() = data.fill(0)
    @Suppress("UNCHECKED_CAST")
    override fun <B : Buffer> get(): B = data as B
}

class WasmJsFloatBuffer(private val data: FloatArray) : FloatBuf {
    constructor(size: Int) : this(FloatArray(size))

    override fun get(index: Int): Float = data[index]
    override fun set(index: Int, value: Float) {
        data[index] = value
    }
    override fun put(data: FloatArray) {
        data.copyInto(this.data)
    }
    override val size: Int = data.size
    override fun clear() = data.fill(0f)
    @Suppress("UNCHECKED_CAST")
    override fun <B : Buffer> get(): B = data as B
}

actual interface Buffer {
    fun <B : Buffer> get(): B
    actual val size: Int
    actual fun clear()
}

actual fun createByteBuffer(data: ByteArray): ByteBuf = WasmJsByteBuffer(data)
actual fun createShortBuffer(data: ShortArray): ShortBuf = WasmJsShortBuffer(data)
actual fun createIntBuffer(data: IntArray): IntBuf = WasmJsIntBuffer(data)
actual fun createFloatBuffer(data: FloatArray): FloatBuf = WasmJsFloatBuffer(data)
