// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.font.GlyphRect
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.testing.ui.saveUiSnapshot
import kotlin.test.Test

/** Visual aid for the bitmap baseline repair; outputs `build/ui-snapshots/bitmap-baseline-*.png`. */
class BitmapFontBaselineVisualTest {
    @Test
    fun writesBeforeAndAfterBaselineFixtures() {
        val aligned = BitmapFont(cellSize = 24)
        val legacy = object : UiFont by aligned {
            override fun glyphFor(char: Char, weight: FontWeight): GlyphRect? = aligned.uvFor(char)
        }

        save("bitmap-baseline-before", legacy)
        save("bitmap-baseline-after", aligned)
        saveGlyphSheet("bitmap-glyph-sheet-after", aligned)
    }

    private fun save(name: String, font: UiFont) {
        val glyphPx = 32f
        var penX = 16f
        val baselineY = 32f
        val primitives = "Aagp Hy".mapNotNull { char ->
            if (char == ' ') {
                penX += glyphPx
                null
            } else {
                font.glyphFor(char, FontWeight.Normal)?.let { glyph ->
                    UiDrawPrimitive.Glyph(
                        x = penX + glyph.offsetXEm * glyphPx,
                        y = baselineY + glyph.offsetYEm * glyphPx,
                        w = glyph.widthEm * glyphPx,
                        h = glyph.heightEm * glyphPx,
                        u0 = glyph.u0,
                        v0 = glyph.v0,
                        u1 = glyph.u1,
                        v1 = glyph.v1,
                        color = Color.White,
                    ).also { penX += font.advanceFor(char, glyphPx, FontWeight.Normal) }
                }
            }
        }
        saveUiSnapshot(name, primitives, width = 320, height = 96, background = Color.Black, font = font)
    }

    private fun saveGlyphSheet(name: String, font: UiFont) {
        val cell = 24f
        val columns = 16
        val glyphs = ('A'..'Z').toList() + ('a'..'z').toList() + ('0'..'9').toList() + "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toList()
        val rows = (glyphs.size + columns - 1) / columns
        val width = (columns * cell).toInt()
        val height = (rows * cell).toInt()
        val primitives = buildList {
            for (x in 0..columns) add(UiDrawPrimitive.Quad(x * cell, 0f, 1f, height.toFloat(), Color(0.22f, 0.22f, 0.24f, 1f)))
            for (y in 0..rows) add(UiDrawPrimitive.Quad(0f, y * cell, width.toFloat(), 1f, Color(0.22f, 0.22f, 0.24f, 1f)))
            glyphs.forEachIndexed { index, char ->
                val glyph = font.glyphFor(char, FontWeight.Normal) ?: return@forEachIndexed
                val x = (index % columns) * cell + 4f
                val y = (index / columns) * cell + 2f
                add(UiDrawPrimitive.Glyph(x + glyph.offsetXEm * 16f, y + glyph.offsetYEm * 16f, glyph.widthEm * 16f, glyph.heightEm * 16f, glyph.u0, glyph.v0, glyph.u1, glyph.v1, Color.White))
            }
        }
        saveUiSnapshot(name, primitives, width, height, background = Color.Black, font = font)
    }
}
