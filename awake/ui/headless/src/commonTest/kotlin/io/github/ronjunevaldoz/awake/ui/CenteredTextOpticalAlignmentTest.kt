// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards vertical placement of single-line centered text -- the badge/button/chip/tab case.
 *
 * `measureVisibleLineBandEm(opticallyCentered = true)` returned `visibleTopEm to 0f`, putting
 * the band's bottom at the line origin and its top BELOW that (`visibleTopEm` is 0.13em for the
 * packed Roboto atlas, 0f for a font using the [io.github.ronjunevaldoz.awake.ui.font.UiFont]
 * defaults). An inverted band trips `measureTextBlock`'s own guard, so every optically-centered
 * line silently fell back to a fabricated `heightPx = glyphPx` that describes no real glyph.
 * Text then sat low, and the compensation was per-component padding fudge -- badge carried
 * `top = 2.5dp, bottom = 1.5dp` to nudge it back.
 *
 * Uses the real packed font, not a synthetic one: the defect lives in the interaction between
 * measured atlas metrics and the centering arithmetic, and a font with tidy 0/1 metrics makes
 * it arithmetically unreachable.
 */
class CenteredTextOpticalAlignmentTest {

    /** 'T' is flat-topped and flat-bottomed with no overshoot, so its quad IS the cap box. */
    private fun capBoxOf(label: String, slot: UiBounds): ClosedFloatingPointRange<Float> {
        val font = UiFonts.default(cellSize = CELL_SIZE)
        val frame = renderUiComponent(width = slot.width, height = slot.height, font = font) {
            primitive.context.createAbsolute(x = slot.x, y = slot.y).text(
                label = label,
                slot = slot,
                font = font,
                textStyle = TextStyle(size = TEXT_SIZE_SP.sp),
                verticallyCentered = true,
            )
        }
        val capital = frame.primitives.filterIsInstance<UiDrawPrimitive.Glyph>().first()
        return capital.y..(capital.y + capital.h)
    }

    @Test
    fun centeredCapBoxSitsInTheOpticalMiddleOfItsSlot() {
        val slot = UiBounds(0f, 0f, 120f, SLOT_HEIGHT)
        val capBox = capBoxOf("Tag", slot)

        val gapAbove = capBox.start - slot.y
        val gapBelow = (slot.y + slot.height) - capBox.endInclusive

        assertTrue(
            abs(gapAbove - gapBelow) <= 1f,
            "centered text must sit in the optical middle: $gapAbove px above the cap, " +
                "$gapBelow px below the baseline (cap box $capBox in slot $slot). " +
                "A skew here is what per-component padding fudge was hiding.",
        )
    }

    @Test
    fun aDescenderDoesNotMoveTheCapitalsAroundIt() {
        val slot = UiBounds(0f, 0f, 120f, SLOT_HEIGHT)

        val withoutDescender = capBoxOf("Tan", slot)
        val withDescender = capBoxOf("Tag", slot)

        assertTrue(
            abs(withoutDescender.start - withDescender.start) < 0.5f,
            "adding a descender must not shift the rest of the line: 'T' sits at " +
                "${withoutDescender.start} in \"Tan\" but ${withDescender.start} in \"Tag\".",
        )
    }

    private companion object {
        const val CELL_SIZE = 12
        const val TEXT_SIZE_SP = 12f

        /** shadcn badge: 12sp text in a 20px line box. */
        const val SLOT_HEIGHT = 20f
    }
}
