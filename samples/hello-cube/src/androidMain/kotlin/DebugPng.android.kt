// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
/** Not implemented on Android yet -- writing to app-private storage (`Context.filesDir`)
 * would need a `Context` this top-level function doesn't have access to; logs instead of
 * writing a file. */
actual fun saveDebugPng(pixels: ByteArray, width: Int, height: Int, path: String) {
    println("DEBUG PNG: saveDebugPng not implemented on Android ($width x $height requested for $path)")
}
