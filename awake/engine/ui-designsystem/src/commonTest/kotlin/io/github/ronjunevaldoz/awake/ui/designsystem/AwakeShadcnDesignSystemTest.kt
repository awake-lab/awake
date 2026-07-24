// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.createColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
            preset = AwakeShadcnStylePreset.Vega,
            baseColor = AwakeShadcnBaseColor.Neutral,
            accent = AwakeShadcnAccent.Base,
            dark = true
        ).asAwakeShadcnTheme()

        assertEquals(AwakeShadcnStylePreset.Vega, theme.config.preset)
        assertEquals(AwakeShadcnBaseColor.Neutral, theme.config.baseColor)
        assertEquals(AwakeShadcnAccent.Base, theme.config.accent)
        assertTrue(abs(theme.radii.lg.value - 6f) <= 0.0001f)
        assertColorClose(hex(0x09090b), theme.tokens.background)
        assertTrue(theme.tokens.background != Color.White)
    }

    @Test
    fun oklchProducesExpectedNeutralSrgbValues() {
        assertColorClose(Color(1f, 1f, 1f, 0.1f), oklch(1f, 0f, alpha = 0.1f))
        assertColorClose(Color(0.630163f, 0.630163f, 0.630163f, 1f), oklch(0.708f, 0f))
    }

    @Test
    fun awakeShadcnBadgeRendersFromPublicUiApi() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnBadge(label = "BETA", variant = AwakeShadcnBadgeVariant.Primary)

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "design-system badge should render its own rounded surface")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "design-system badge should render glyphs")
    }

    @Test
    fun awakeShadcnBadgeReadsConfiguredThemeFromScope() {
        val ui = UiContext()
        val theme = awakeShadcnTheme(
            baseColor = AwakeShadcnBaseColor.Zinc,
            accent = AwakeShadcnAccent.Rose
        )
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnBadge(label = "LIVE", variant = AwakeShadcnBadgeVariant.Primary)

        val firstQuad = ui.endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        assertColorClose(theme.tokens.primary, firstQuad.color)
    }

    @Test
    fun awakeShadcnButtonKeepsClickSemantics() {
        val ui = UiContext()
        var clicked = false
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)

        ui.beginFrame(200f, 80f, testSnapshot(x = 40f, y = 30f, down = true))
        clicked = ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnButton(
                id = "save",
                label = "SAVE",
                modifier = Modifier.width(120f.px).height(32f.px),
                variant = AwakeShadcnButtonVariant.Primary
            )
        ui.endFrame()
        assertTrue(!clicked)

        ui.beginFrame(200f, 80f, testSnapshot(x = 40f, y = 30f, down = false))
        clicked = ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnButton(
                id = "save",
                label = "SAVE",
                modifier = Modifier.width(120f.px).height(32f.px),
                variant = AwakeShadcnButtonVariant.Primary
            )
        ui.endFrame()

        assertTrue(clicked, "design-system button must preserve the base widget click contract")
    }

    @Test
    fun awakeShadcnSurfaceHostsNestedCustomContent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(240f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createColumn(modifier = Modifier.offset(20f.dp, 20f.dp).width(200f.dp))
            .awakeShadcnSurface("surface", modifier = Modifier.copy(width = Dimension.Fixed(200f.px), height = Dimension.Fixed(100f.px))) {
                awakeShadcnBadge(label = "READY", width = Dimension.Fixed(80f.px), height = Dimension.WrapContent)
            }

        val primitives = ui.endFrame()
        assertEquals(3, primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size, "surface should emit border + fill, and the filled badge should emit one rounded quad")
    }

    @Test
    fun awakeShadcnDslAdaptersComposeInsideUiDsl() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(280f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            awakeShadcnSurface(
                id = "dsl-surface", modifier = Modifier.copy(height = Dimension.Fixed(120f.px))) {
                awakeShadcnBadge(label = "READY", variant = AwakeShadcnBadgeVariant.Primary)
                awakeShadcnButton(
                    id = "launch",
                    label = "Launch",
                    modifier = Modifier.width(120f.px).height(32f.px),
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
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(320f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            awakeShadcnSurface(
                id = "dsl-fields", modifier = Modifier.copy(height = Dimension.WrapContent)) {
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
    fun awakeShadcnSectionHeaderSupportsSlotContent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(280f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            awakeShadcnSurface(
                id = "slot-header", modifier = Modifier.copy(height = Dimension.WrapContent)) {
                awakeShadcnSectionHeader(
                    title = {
                        row( horizontalArrangement = Arrangement.spacedBy(6f.dp), modifier = Modifier.copy(height = 20f.dp.toDimension())) {
                            awakeShadcnBadge(
                                label = "NEW",
                                modifier = UiModifier(width = Dimension.Fixed(48f.px)),
                                variant = AwakeShadcnBadgeVariant.Outline
                            )
                            text("Scene Settings")
                        }
                    },
                    description = {
                        awakeShadcnSupportingText("Slot content keeps the header structure reusable.")
                    }
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().isNotEmpty())
    }

    @Test
    fun awakeShadcnPropertyControlsSupportSlotLabels() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            awakeShadcnSurface(
                id = "slot-fields", modifier = Modifier.copy(height = Dimension.WrapContent)) {
                awakeShadcnPropertyDropdown(
                    id = "mode",
                    options = listOf("Orbit", "Fly"),
                    selectedIndex = 0,
                    labelContent = {
                        text("Camera Mode")
                    }
                )
                awakeShadcnPropertyToggle(
                    id = "grid",
                    checked = true,
                    labelContent = {
                        text("Reference Grid")
                    }
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 5)
    }

    @Test
    fun awakeShadcnBadgeSupportsWrapContentMeasurement() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(AwakeShadcnTheme)
        ui.beginFrame(240f, 120f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnBadge(
                label = "LIVE",
                width = Dimension.WrapContent,
                height = Dimension.WrapContent
            )

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertEquals(4, glyphs.size)
        assertTrue(glyphs.maxOf { it.x + it.w } > 20f, "wrap-content badge should size itself to its label")
    }

    @Test
    fun awakeShadcnButtonSlotApiInheritsThemedColorAndCentersContent() {
        val ui = UiContext()
        val theme = AwakeShadcnTheme
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        // Ensure the context's base text style uses the theme's foreground,
        // otherwise it stays at the CoreUiTheme's default (white).
        ui.pushTextStyle(TextStyle(color = theme.tokens.foreground))

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
            .awakeShadcnButton(
                id = "test-btn",
                variant = AwakeShadcnButtonVariant.Primary,
                modifier = Modifier.width(100f.px).height(40f.px)
            ) {
                text("test")
            }

        val primitives = ui.endFrame()
        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "Slot content should render glyphs")

        val primaryForeground = theme.tokens.primaryForeground
        glyphs.forEach { glyph ->
            // In Vega dark theme, primary is light and primaryForeground is dark (0.09)
            assertColorClose(primaryForeground, glyph.color)
        }

        val buttonBounds = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        val glyphBoundsX = glyphs.minOf { it.x }
        val glyphBoundsW = glyphs.maxOf { it.x + it.w } - glyphBoundsX
        val glyphCenterX = glyphBoundsX + glyphBoundsW / 2f
        val buttonCenterX = buttonBounds.x + buttonBounds.w / 2f

        assertTrue(abs(glyphCenterX - buttonCenterX) < 5f, "Text should be roughly horizontally centered in button: glyphCenterX=$glyphCenterX, buttonCenterX=$buttonCenterX")
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
