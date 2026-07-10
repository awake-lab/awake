package io.github.ronjunevaldoz.awake.core

class DesktopPlatform : Platform {
    override val name: String = "Desktop"
    override val isMobile: Boolean = false
}

actual fun getPlatform(): Platform = DesktopPlatform()