// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core

class DesktopPlatform : Platform {
    override val name: String = "Desktop"
    override val isMobile: Boolean = false
}

actual fun getPlatform(): Platform = DesktopPlatform()
