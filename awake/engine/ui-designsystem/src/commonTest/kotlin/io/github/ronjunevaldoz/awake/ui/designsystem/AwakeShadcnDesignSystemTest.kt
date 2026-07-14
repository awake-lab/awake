// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.px
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AwakeShadcnDesignSystemTest {

    @Test
    fun awakeShadcnBadgeRendersFromPublicUiApi() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(200f, 80f)

        ui.absolute(20f, 20f, font = BitmapFont(), theme = AwakeShadcnTheme)
            .awakeShadcnBadge(label = "BETA", variant = AwakeShadcnBadgeVariant.Primary)

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "design-system badge should render its own rounded surface")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "design-system badge should render glyphs")
    }

    @Test
    fun awakeShadcnButtonKeepsClickSemantics() {
        val ui = UiContext()
        var clicked = false

        Input.setPointer(down = true, x = 40f, y = 30f)
        ui.beginFrame(200f, 80f)
        clicked = ui.absolute(20f, 20f, font = BitmapFont(), theme = AwakeShadcnTheme)
            .awakeShadcnButton("save", 120f, 32f, "SAVE", variant = AwakeShadcnButtonVariant.Primary)
        ui.endFrame()
        assertTrue(!clicked)

        Input.setPointer(down = false, x = 40f, y = 30f)
        ui.beginFrame(200f, 80f)
        clicked = ui.absolute(20f, 20f, font = BitmapFont(), theme = AwakeShadcnTheme)
            .awakeShadcnButton("save", 120f, 32f, "SAVE", variant = AwakeShadcnButtonVariant.Primary)
        ui.endFrame()

        assertTrue(clicked, "design-system button must preserve the base widget click contract")
    }

    @Test
    fun awakeShadcnSurfaceHostsNestedCustomContent() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(240f, 160f)

        ui.column(20f, 20f, 200f, font = BitmapFont(), theme = AwakeShadcnTheme)
            .awakeShadcnSurface("surface", Dimension.Fixed(200f.px), Dimension.Fixed(100f.px)) {
                awakeShadcnBadge("READY", width = 80f)
            }

        val primitives = ui.endFrame()
        assertEquals(4, primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size, "surface + badge should each emit border + fill rounded quads")
    }
}
