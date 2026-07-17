// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.ronjunevaldoz.awake.core.utils

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.fetch.Response

// Resources are served at the same relative path by webpack, so plain fetch() works.
// wasmJs's `await()` infers its return type from call-site context, not the Promise's own
// type argument, so both awaits below need an explicit type or inference fails.
actual suspend fun readResourceBytes(path: String): ByteArray {
    val response: Response = window.fetch(path).await()
    if (!response.ok) {
        error("Resource not found: $path (HTTP ${response.status})")
    }
    val buffer: ArrayBuffer = response.arrayBuffer().await()
    val bytes = Int8Array(buffer)
    return ByteArray(bytes.length) { bytes[it] }
}
