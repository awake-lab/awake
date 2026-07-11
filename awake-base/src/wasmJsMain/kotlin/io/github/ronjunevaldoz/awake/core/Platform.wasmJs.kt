// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core

class WasmJsPlatform : Platform {
    override val name: String = "Web"
    override val isMobile: Boolean = false
}

actual fun getPlatform(): Platform = WasmJsPlatform()
