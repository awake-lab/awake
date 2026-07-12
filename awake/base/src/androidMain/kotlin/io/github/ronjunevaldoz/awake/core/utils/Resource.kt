// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

actual suspend fun readResourceBytes(path: String): ByteArray {
    val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
        ?: object {}.javaClass.classLoader?.getResourceAsStream(path)
        ?: error("Resource not found: $path")
    return stream.use { it.readBytes() }
}
