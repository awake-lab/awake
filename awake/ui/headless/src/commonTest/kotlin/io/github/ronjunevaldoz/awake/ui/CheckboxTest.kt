// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.checkbox
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckboxTest {

    @Test
    fun checkboxFlipsOnPressReleaseInsideBounds() {
        var checked = false
        // Whole row is the hit target, not just the small box -- click somewhere in the
        // middle of the row (x=100), not at the box's own tiny x=0..16 range.
        uiTestSession(width = 200f, height = 100f) {
            fun UiScope.draw() {
                checked = primitive.context.createAbsolute(x = 20f, y = 20f).checkbox(
                    "cb", checked, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px),
                )
            }
            click(100f, 20f) { draw() }
        }
        assertTrue(
            checked,
            "clicking anywhere in the row must flip the checkbox, same as a real checkbox's clickable row",
        )
    }

    @Test
    fun checkboxEmitsASeparateBoxAndLabelNotOneBigFill() {
        // checkbox() itself reads no ambient theme (see the `awake-ui-authoring` skill) -- an
        // explicit border here stands in for what a real caller's Style always supplies (e.g.
        // shadcnCheckboxStyle), the same way this test already supplies its own `label`/size.
        val boxStyle = Style { border(1f.dp, Color(0.4f, 0.4f, 0.45f, 0.9f)) }
        val frame = renderUiComponent(width = 200f, height = 100f, font = BitmapFont()) {
            primitive.context.createAbsolute(x = 20f, y = 20f).checkbox(
                "cb", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px),
                style = boxStyle,
            )
        }
        val primitives = frame.primitives

        val quads = primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(
            5,
            quads.size,
            "unchecked box draws the box quad plus its 4 border edge quads, not a full-row fill",
        )
        assertTrue(
            quads[0].w < 160f,
            "the box quad must be far narrower than the row -- proves it's a small box, not the whole row filled",
        )

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "label must still render its own glyphs")
    }

    @Test
    fun checkboxCanUseModifierSizingAsPrimaryApi() {
        val frame = renderUiComponent(width = 220f, height = 100f, font = BitmapFont()) {
            primitive.context.createAbsolute(x = 20f, y = 20f).checkbox(
                "cb", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px),
            )
        }
        val glyphs = frame.primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "modifier-sized checkbox should still render its label")
        assertTrue(
            glyphs.first().x >= 44f,
            "label should still render to the right of the checkbox box when sizing comes from modifier",
        )
    }

    @Test
    fun checkedBoxAddsAStrokedCheckmark() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            primitive.context.createAbsolute(x = 20f, y = 20f)
                .checkbox("cb", checked = true, modifier = Modifier.width(160f.px).height(40f.px))
        }
        val quads = frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(
            1,
            quads.size,
            "checked state paints one primary box quad; the checkmark is emitted separately",
        )
        assertEquals(
            1,
            frame.primitives.filterIsInstance<UiDrawPrimitive.FilledPath>().size,
            "checked state emits one antialiased filled checkmark",
        )
    }

    @Test
    fun styleShapeMakesTheBoxARoundedQuad() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            primitive.context.createAbsolute(x = 20f, y = 20f).checkbox(
                "cb", checked = false, modifier = Modifier.width(160f.px).height(40f.px),
                style = Style { shape(UiShape.sm) },
            )
        }
        val primitive = frame.primitives.first()
        assertIs<UiDrawPrimitive.RoundedQuad>(
            primitive,
            "style.shape() must round the checkbox's own box, not just buttons/panels",
        )
    }

    @Test
    /** Same reason as the other pixel-scanning gates: the row/column walk is the measurement. */
    @Suppress("NestedBlockDepth")
    fun trueFontCheckboxLabelStaysVerticallyCenteredInTheRow() {
        val font = UiFonts.trueSans()
        val frame = renderUiComponent(width = 220f, height = 100f, font = font) {
            primitive.context.createAbsolute(x = 20f, y = 20f).checkbox(
                "cb", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px),
            )
        }

        // Measure the RENDERED ink, not glyph quad bounds: the quad deliberately extends past
        // the ink by the atlas crop bleed plus a texel snap (see PackedUiFontData.quadMetricsEm),
        // so quad centers carry sub-pixel slack that says nothing about where the label sits.
        val width = 220
        val height = 100
        val pixels = frame.primitives.filterIsInstance<UiDrawPrimitive.Glyph>().rasterize(
            width,
            height,
            background = Color(0f, 0f, 0f, 0f),
            font = font,
        )
        var inkTop = -1
        var inkBottom = -1
        for (y in 0 until height) {
            for (x in 0 until width) {
                if ((pixels[(y * width + x) * 4 + 3].toInt() and 0xFF) >= 128) {
                    if (inkTop < 0) inkTop = y
                    inkBottom = y
                }
            }
        }
        assertTrue(inkTop >= 0, "expected the checkbox row to render some ink")
        val rowCenterY = 40f
        val inkCenterY = (inkTop + inkBottom + 1) / 2f

        assertTrue(
            kotlin.math.abs(inkCenterY - rowCenterY) <= 1f,
            "checkbox label should stay vertically centered in its row with the true font: " +
                "rowCenterY=$rowCenterY inkCenterY=$inkCenterY inkRows=$inkTop..$inkBottom",
        )
    }
}
