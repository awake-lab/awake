// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

object UiFonts {
    fun default(cellSize: Int = 12): UiFont = trueSans(cellSize = cellSize)

    fun bitmap(cellSize: Int = 12): UiFont = BitmapFont(cellSize = cellSize)

    fun trueSans(cellSize: Int = 12): UiFont =
        PackedUiFont(RobotoRegularUiFontData, cellSize = cellSize)

    fun msdf(cellSize: Int = 12): UiFont = MsdfFont(cellSize = cellSize)
}
