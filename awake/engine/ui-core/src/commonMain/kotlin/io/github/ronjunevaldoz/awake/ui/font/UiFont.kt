// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

interface UiFont {
    val cellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val atlasPixelsRgba: ByteArray

    fun uvFor(char: Char): GlyphRect?
}
