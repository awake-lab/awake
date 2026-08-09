// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.memory

// Named after the construct it replaces; detekt's naming pattern cannot match a
// backtick-escaped keyword.
@Suppress("FunctionNaming")
fun Buffer.`when`(
    byte: java.nio.ByteBuffer.() -> Unit = { TODO("Byte buffer not implemented") },
    short: java.nio.ShortBuffer.() -> Unit = { TODO("Short buffer not implemented") },
    int: java.nio.IntBuffer.() -> Unit = { TODO("Int buffer not implemented") },
    float: java.nio.FloatBuffer.() -> Unit = { TODO("Float buffer not implemented") },
) {
    when (this) {
        is ByteBuffer -> byte(get())
        is ShortBuffer -> short(get())
        is IntBuffer -> int(get())
        is FloatBuffer -> float(get())
    }
}
