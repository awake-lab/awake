// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * shadcn's TooltipContent is `bg-foreground text-background` -- an inverted pill. The dark fill in
 * light mode is correct; the text sitting on it was not. surface() pushed its text style but not
 * its `foreground`, so text() found no inherited colour and fell back to the theme's own
 * foreground -- the same colour as the pill it was drawn on, i.e. invisible.
 */
class ShadcnTooltipContrastTest {

    @Test
    fun tooltipTextUsesTheInvertedContentColour() {
        val theme = shadcnThemeValues(dark = false)
        val frame = renderShadcnComponent(
            width = 300f,
            height = 150f,
            theme = theme,
            font = BitmapFont(),
            input = testSnapshot(x = -100f, y = -100f, down = false),
        ) {
            shadcnTooltipText(
                anchorSlot = UiBounds(x = 100f, y = 60f, width = 100f, height = 30f),
                visible = true,
                text = "Tooltip Info",
                id = "tooltip-contrast",
            )
        }
        val glyphs = frame.primitivesOf<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "expected the tooltip label to be drawn")
        glyphs.forEach { glyph ->
            assertEquals(
                theme.colors.background,
                glyph.color,
                "tooltip label must be drawn in the inverted content colour, not the theme foreground",
            )
        }
    }
}
