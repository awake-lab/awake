package io.github.ronjunevaldoz.awake.core

interface Platform {
    val name: String
    val isMobile: Boolean
}

expect fun getPlatform(): Platform

val isMobile = getPlatform().isMobile
val isDesktop = getPlatform().name.contains("Desktop")