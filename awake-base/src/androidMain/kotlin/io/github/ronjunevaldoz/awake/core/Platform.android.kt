package io.github.ronjunevaldoz.awake.core

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val isMobile: Boolean = true
}

actual fun getPlatform(): Platform = AndroidPlatform()