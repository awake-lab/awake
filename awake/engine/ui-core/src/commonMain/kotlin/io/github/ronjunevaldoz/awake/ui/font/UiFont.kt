// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

enum class UiFontSamplingMode {
    CoverageAlpha,
    DistanceField
}

data class GlyphRect(val u0: Float, val v0: Float, val u1: Float, val v1: Float)

interface UiFont {
    val samplingMode: UiFontSamplingMode
    val cellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val atlasPixelsRgba: ByteArray
    val distanceFieldRangePx: Float
        get() = 0f

    fun uvFor(char: Char): GlyphRect?
}
