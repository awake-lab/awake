// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

/** Draws [label] as a row of glyph quads. */
fun UiScope.text(
    label: String,
    slot: UiSlot = claimSlot(Dimension.FillMax, Dimension.Fixed(((this.font?.let { it.cellSize * resolvedTextScale() } ?: 0f)).px)),
    font: BitmapFont? = this.font,
    color: FloatArray = theme.tokens.foreground,
    centered: Boolean = false
) {
    checkNotNull(font) { "text() requires a font, either from the UiScope or passed explicitly" }
    val glyphPx = font.cellSize * resolvedTextScale()
    val textWidth = label.length * glyphPx
    var penX = if (centered) slot.x + (slot.width - textWidth) / 2f else slot.x
    val penY = if (centered) slot.y + (slot.height - glyphPx) / 2f else slot.y
    for (char in label) {
        val uv = font.uvFor(char)
        if (uv != null) {
            emit(UiDrawPrimitive.Glyph(penX, penY, glyphPx, glyphPx, uv.u0, uv.v0, uv.u1, uv.v1, color))
        }
        penX += glyphPx
    }
}
