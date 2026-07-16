// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
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
import kotlin.math.abs

class AwakeShadcnDesignSystemTest {

    @Test
    fun awakeShadcnThemeTracksOfficialNeutralDarkRoles() {
        assertColorClose(Color(0.039388f, 0.039388f, 0.039388f, 1f), AwakeShadcnTheme.tokens.background)
        assertColorClose(Color(0.980256f, 0.980256f, 0.980256f, 1f), AwakeShadcnTheme.tokens.foreground)
        assertColorClose(Color(0.898161f, 0.898161f, 0.898161f, 1f), AwakeShadcnTheme.tokens.primary)
        assertColorClose(Color(1f, 1f, 1f, 0.1f), AwakeShadcnTheme.tokens.border)
        assertColorClose(oklch(0.168f, 0f), AwakeShadcnTheme.card)
        assertColorClose(oklch(0.205f, 0f), AwakeShadcnTheme.popover)
        assertColorClose(oklch(0.158f, 0f), AwakeShadcnTheme.sidebar)
        assertColorClose(oklch(0.556f, 0f), AwakeShadcnTheme.ring)
    }

    @Test
    fun awakeShadcnThemeDerivesRadiusScaleFromSingleBaseRadius() {
        assertTrue(abs(AwakeShadcnTheme.radii.xs.value - 2.4f) <= 0.0001f)
        assertTrue(abs(AwakeShadcnTheme.radii.sm.value - 3.6f) <= 0.0001f)
        assertTrue(abs(AwakeShadcnTheme.radii.md.value - 4.8f) <= 0.0001f)
        assertTrue(abs(AwakeShadcnTheme.radii.lg.value - 6f) <= 0.0001f)
        assertTrue(abs(AwakeShadcnTheme.radii.xl.value - 8.4f) <= 0.0001f)
    }

    @Test
    fun awakeShadcnThemeKeepsInteractiveRolesDistinct() {
        assertTrue(AwakeShadcnTheme.tokens.secondary != AwakeShadcnTheme.tokens.muted)
        assertTrue(AwakeShadcnTheme.tokens.accent != AwakeShadcnTheme.tokens.secondary)
        assertTrue(AwakeShadcnTheme.sidebarAccent != AwakeShadcnTheme.sidebar)
    }

    @Test
    fun awakeShadcnThemeFactoryAppliesPresetBaseAndAccentOverrides() {
        val theme = awakeShadcnTheme(
            preset = AwakeShadcnStylePreset.Lyra,
            baseColor = AwakeShadcnBaseColor.Mist,
            accent = AwakeShadcnAccent.Blue
        ).asAwakeShadcnTheme()

        assertEquals(AwakeShadcnStylePreset.Lyra, theme.config.preset)
        assertEquals(AwakeShadcnBaseColor.Mist, theme.config.baseColor)
        assertEquals(AwakeShadcnAccent.Blue, theme.config.accent)
        assertTrue(abs(theme.radii.lg.value - 0f) <= 0.0001f)
        assertColorClose(hex(0x3B82F6), theme.tokens.primary)
        assertTrue(theme.tokens.background != AwakeShadcnTheme.tokens.background)
    }

    @Test
    fun oklchProducesExpectedNeutralSrgbValues() {
        assertColorClose(Color(1f, 1f, 1f, 0.1f), oklch(1f, 0f, alpha = 0.1f))
        assertColorClose(Color(0.630163f, 0.630163f, 0.630163f, 1f), oklch(0.708f, 0f))
    }

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
    fun awakeShadcnBadgeReadsConfiguredThemeFromScope() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        val theme = awakeShadcnTheme(
            baseColor = AwakeShadcnBaseColor.Zinc,
            accent = AwakeShadcnAccent.Rose
        )
        ui.beginFrame(200f, 80f)

        ui.absolute(20f, 20f, font = BitmapFont(), theme = theme)
            .awakeShadcnBadge(label = "LIVE", variant = AwakeShadcnBadgeVariant.Primary)

        val firstQuad = ui.endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        assertColorClose(theme.tokens.primary, firstQuad.color)
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
        assertEquals(3, primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size, "surface should emit border + fill, and the filled badge should emit one rounded quad")
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

private fun assertColorClose(expected: Color, actual: Color, tolerance: Float = 0.005f) {
    listOf(
        expected.r to actual.r,
        expected.g to actual.g,
        expected.b to actual.b,
        expected.a to actual.a
    ).forEachIndexed { index, (expectedChannel, actualChannel) ->
        assertTrue(
            abs(expectedChannel - actualChannel) <= tolerance,
            "channel $index expected $expectedChannel but was $actualChannel"
        )
    }
}
