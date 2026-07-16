// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.ui
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

    @Test
    fun awakeShadcnDslAdaptersComposeInsideUiDsl() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(280f, 180f)

        ui.ui(x = 20f, y = 20f, width = 240f, font = BitmapFont(), theme = AwakeShadcnTheme) {
            awakeShadcnSurface(
                id = "dsl-surface",
                height = Dimension.Fixed(120f.px)
            ) {
                awakeShadcnBadge("READY", variant = AwakeShadcnBadgeVariant.Primary)
                awakeShadcnButton(
                    id = "launch",
                    width = 120f,
                    height = 32f,
                    label = "Launch",
                    variant = AwakeShadcnButtonVariant.Secondary
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "DSL adapters should still render labeled content")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 4, "surface, badge, and button should emit rounded surfaces through the DSL")
    }

    @Test
    fun awakeShadcnFieldWrappersComposeInsideDsl() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(320f, 180f)

        ui.ui(x = 20f, y = 20f, width = 280f, font = BitmapFont(), theme = AwakeShadcnTheme) {
            awakeShadcnSurface(
                id = "dsl-fields",
                height = Dimension.WrapContent
            ) {
                awakeShadcnPropertyToggle("show-grid", "Show Grid", checked = true)
                awakeShadcnPropertyDropdown("mode", "Mode", listOf("Orbit", "Fly"), selectedIndex = 0)
                awakeShadcnPropertySlider("speed", "Speed", min = 1f, max = 10f, value = 5f)
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "field wrappers should keep text rendering intact")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 6, "surface and field wrappers should emit shaped chrome")
    }

    @Test
    fun awakeShadcnBadgeSupportsWrapContentMeasurement() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(240f, 120f)

        ui.absolute(20f, 20f, font = BitmapFont(), theme = AwakeShadcnTheme)
            .awakeShadcnBadge(
                label = "LIVE",
                width = Dimension.WrapContent,
                height = Dimension.WrapContent
            )

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertEquals(4, glyphs.size)
        assertTrue(glyphs.maxOf { it.x + it.w } > 20f, "wrap-content badge should size itself to its label")
    }
}
