// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiTypographyTest {

    @Test
    fun recipesReadTheCurrentCoreThemeWhenNoShadcnThemeIsScoped() {
        val values = shadcnThemeValues(dark = false)
        val ui = UiContext()
        ui.pushFont(UiFonts.bitmap())
        ui.pushTheme(values)
        ui.beginFrame(240f, 80f, testSnapshot())

        ui.headlessRoot().shadcnText("Legacy Core theme")

        val glyph = ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Glyph>().first()
        assertEquals(values.colors.foreground, glyph.color)
    }

    @Test
    fun supportingTextWrapsInsideWrapContentPanels() {
        val ui = UiContext()
        ui.pushFont(UiFonts.bitmap())
        ui.beginFrame(280f, 220f, testSnapshot())

        var panelSlot: UiBounds? = null

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            surface(id = "copy", modifier = Modifier.offset(20f.dp, 20f.dp).width(180f.dp)) { slot ->
                panelSlot = slot
                text("Copy")
                shadcnMuted(
                    "Shared supporting copy should wrap cleanly and grow the panel instead of spilling outside its bounds.",
                    maxLines = 4,
                )
            }
        }

        val primitives = ui.finishFrame().primitives
        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        val resolvedPanel = assertNotNull(panelSlot)
        assertTrue(
            resolvedPanel.height > 32f,
            "wrap-content panels should grow to fit multi-line supporting copy",
        )
        assertTrue(
            glyphs.any { it.y > resolvedPanel.y + 16f },
            "wrapped supporting copy should render on more than one text row",
        )
    }
}
