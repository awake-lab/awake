// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual suspend fun readResourceBytes(path: String): ByteArray {
    val fullPath = NSBundle.mainBundle.resourcePath?.plus("/$path")
        ?: error("Bundle resource path unavailable")
    val data: NSData = NSData.dataWithContentsOfFile(fullPath)
        ?: error("Resource not found: $fullPath")
    val size = data.length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
}
