// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression coverage for the uneven-letter-spacing bug: [PackedUiFont.advanceFor] used to
 * compare a cell-relative `offsetXEm` against a pen-relative `advanceEm`, so its "clamp to the
 * glyph's own right edge" logic fired on almost every glyph by a constant, atlas-padding-sized
 * amount instead of only on genuine overhang -- and because each glyph's ink width differs, that
 * constant padding term translated into a *varying* percentage of each glyph's true advance,
 * which is what actually read as uneven spacing (tight after 'A', loose after 'c'/'o'/'r'...).
 *
 * `advancesEm` is the ground truth (straight from the source TTF's `font.getlength`), so a
 * non-overhanging glyph's effective advance must now track it tightly, and the spread across
 * different letters must collapse relative to the pre-fix numbers recorded in the bug report
 * (6%-29% inflation, worst offenders 'r' +29.4%, 'i' +17.8%, 'A' +16.1%, 'c' +13.4%).
 */
class PackedUiFontAdvanceTest {

    private val font = UiFonts.trueSans()
    private val glyphPx = 100f

    @Test
    fun nonOverhangingGlyphsAdvanceByTheirTrueFontAdvance() {
        // Excludes 'f', 'r', 'k', 't', 'v', 'x', 'y', 'A', 'T', 'V' -- those genuinely overhang
        // their own advance in Roboto (see PackedUiFont.advanceFor's doc) and are expected to
        // still clamp by a few percent even after this fix; every other letter should not.
        for (char in "codnuGSBHzLWCDEFIJMNOPQUXYZabeghijlmpqswt") {
            val effective = font.advanceFor(char, glyphPx)
            val trueAdvance = trueAdvanceEm(char) * glyphPx
            val inflationPct = (effective - trueAdvance) / trueAdvance * 100f
            assertTrue(
                inflationPct < 2f,
                "'$char' advance inflated by $inflationPct% " +
                    "(effective=$effective, true=$trueAdvance) -- expected < 2% for a " +
                    "non-overhanging glyph now that offsetXEm is pen-relative",
            )
        }
    }

    /** Ground truth: the font data's own declared per-glyph advance (pen-relative, unclamped). */
    private fun trueAdvanceEm(char: Char): Float {
        val index = RobotoRegularUiFontData.glyphOrder.indexOf(char)
        return RobotoRegularUiFontData.advancesEm[index]
    }
}
