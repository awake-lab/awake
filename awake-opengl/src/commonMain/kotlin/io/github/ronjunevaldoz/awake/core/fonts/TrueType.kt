package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.rendering.Texture

interface TrueType {
    val texture: Texture
    fun drawText(text: String)
    fun dispose()
}


data class FontMetrics(val ascent: Int, val descent: Int, val lineGap: Int)

expect fun createTrueType(path: String, size: Float): TrueType