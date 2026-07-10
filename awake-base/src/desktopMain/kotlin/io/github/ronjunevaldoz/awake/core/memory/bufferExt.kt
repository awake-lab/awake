package io.github.ronjunevaldoz.awake.core.memory

fun Buffer.`when`(
    byte: java.nio.ByteBuffer.() -> Unit = { TODO("Byte buffer not implemented") },
    short: java.nio.ShortBuffer.() -> Unit = { TODO("Short buffer not implemented") },
    int: java.nio.IntBuffer.() -> Unit = { TODO("Int buffer not implemented") },
    float: java.nio.FloatBuffer.() -> Unit = { TODO("Float buffer not implemented") }
) {
    when (this) {
        is ByteBuffer -> byte(get())
        is ShortBuffer -> short(get())
        is IntBuffer -> int(get())
        is FloatBuffer -> float(get())
    }
}