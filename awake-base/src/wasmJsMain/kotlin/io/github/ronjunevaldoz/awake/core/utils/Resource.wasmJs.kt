@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.ronjunevaldoz.awake.core.utils

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.fetch.Response

// Web demo (see docs/MVP_PLAN.md's decision log): a real browser fetch() implementation --
// resource files under commonMain/resources are served at the same relative path by
// webpack-dev-server/the production bundle, so a plain relative fetch() works without any
// extra asset-copying step. wasmJs's `await()` infers its return type from call-site
// context (it's not derived from the Promise's own type argument), so both awaits need an
// explicit type -- confirmed the hard way ("Cannot infer type for type parameter 'T'").
actual suspend fun readResourceBytes(path: String): ByteArray {
    val response: Response = window.fetch(path).await()
    if (!response.ok) {
        error("Resource not found: $path (HTTP ${response.status})")
    }
    val buffer: ArrayBuffer = response.arrayBuffer().await()
    val bytes = Int8Array(buffer)
    return ByteArray(bytes.length) { bytes[it] }
}
