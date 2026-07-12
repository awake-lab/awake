// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

/** Not implemented on iOS yet -- no trivial cross-version filesystem+PNG-encoding API to
 * reach for the way desktop's `javax.imageio` is; logs instead of writing a file. */
actual fun saveDebugPng(pixels: ByteArray, width: Int, height: Int, path: String) {
    println("DEBUG PNG: saveDebugPng not implemented on iOS ($width x $height requested for $path)")
}
