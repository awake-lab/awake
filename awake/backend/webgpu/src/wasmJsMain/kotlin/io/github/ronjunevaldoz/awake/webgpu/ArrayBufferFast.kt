package io.github.ronjunevaldoz.awake.webgpu

import io.ygdrasil.webgpu.ArrayBuffer

/**
 * Faster replacement for wgpu4k's [ArrayBuffer.of] on every hot per-frame `writeBuffer` call
 * site in this backend (vertex/index uploads, MVP and screen-size uniforms).
 *
 * Confirmed via a real Chrome DevTools Performance recording of `samples/ui-showcase` on
 * WebGPU (wasmJs): `ArrayBuffer.of` accounted for ~84% of total per-frame scripting time.
 * wgpu4k's `ArrayBuffer.of(FloatArray)`/`of(IntArray)` (webgpu-ktypes,
 * `wasmJsMain/ArrayBuffer.wasmJs.kt`, `toArrayBuffer()`) boxes every element into a
 * `JsArray<JsNumber>` and then reconstructs a typed array *from that boxed array* -- two
 * per-element wasm/JS boundary crossings per value. `ArrayBuffer.allocate` + `setFloats`/
 * `setInts` writes directly into a typed-array view over the target buffer in a single
 * per-element pass, skipping the intermediate boxed `JsArray` entirely.
 *
 * ponytail: still a per-element loop under the hood (Kotlin/Wasm-GC arrays have no linear
 * memory to bulk-copy/view from), so this halves the work rather than eliminating it.
 * Upgrade path: a wgpu4k fix upstream, or a future Kotlin/Wasm bulk array-to-typed-array
 * interop intrinsic.
 */
internal fun fastArrayBufferOf(data: FloatArray): ArrayBuffer =
    ArrayBuffer.allocate((Float.SIZE_BYTES * data.size).toULong()).apply { setFloats(0uL, data) }

internal fun fastArrayBufferOf(data: IntArray): ArrayBuffer =
    ArrayBuffer.allocate((Int.SIZE_BYTES * data.size).toULong()).apply { setInts(0uL, data) }

internal fun fastArrayBufferOf(data: ByteArray): ArrayBuffer =
    ArrayBuffer.allocate(data.size.toULong()).apply { setBytes(0uL, data) }
