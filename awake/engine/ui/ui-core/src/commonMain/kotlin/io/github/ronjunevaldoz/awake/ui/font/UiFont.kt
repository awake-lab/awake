// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

enum class UiFontSamplingMode {
    CoverageAlpha,
    DistanceField
}

data class GlyphRect(
    val u0: Float,
    val v0: Float,
    val u1: Float,
    val v1: Float,
    val offsetXEm: Float = 0f,
    val offsetYEm: Float = 0f,
    val widthEm: Float = 1f,
    val heightEm: Float = 1f
)

interface UiFont {
    val samplingMode: UiFontSamplingMode
    val cellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val atlasPixelsRgba: ByteArray
    val visibleTopEm: Float
        get() = 0f
    val visibleBottomEm: Float
        get() = 1f
    val distanceFieldRangePx: Float
        get() = 0f

    fun uvFor(char: Char): GlyphRect?

    fun advanceFor(char: Char, glyphPx: Float): Float = glyphPx
}

fun UiFont.measureTextWidth(label: String, glyphPx: Float): Float {
    var width = 0f
    label.forEach { char ->
        if (char != '\n') {
            width += advanceFor(char, glyphPx)
        }
    }
    return width
}
