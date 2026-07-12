// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
/** Not implemented on wasmJs yet -- writing a browser-downloadable PNG would need a
 * canvas/Blob/anchor-click dance; logs instead of writing a file. */
actual fun saveDebugPng(pixels: ByteArray, width: Int, height: Int, path: String) {
    println("DEBUG PNG: saveDebugPng not implemented on wasmJs ($width x $height requested for $path)")
}
