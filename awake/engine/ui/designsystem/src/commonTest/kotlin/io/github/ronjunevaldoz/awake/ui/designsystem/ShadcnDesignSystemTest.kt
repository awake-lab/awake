// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldDescription
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldError
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLegend
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldSet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnDesignSystemTest {

    @Test
    fun shadcnThemeTracksOfficialNeutralDarkRoles() {
        assertColorClose(Color(0.039388f, 0.039388f, 0.039388f, 1f), ShadcnTheme.colors.background)
        assertColorClose(Color(0.980256f, 0.980256f, 0.980256f, 1f), ShadcnTheme.colors.foreground)
        assertColorClose(Color(0.898161f, 0.898161f, 0.898161f, 1f), ShadcnTheme.colors.primary)
        assertColorClose(Color(1f, 1f, 1f, 0.1f), ShadcnTheme.colors.border)
        // card/popover/sidebar are all 0.205 in real shadcn's dark theme -- see
        // ShadcnTheme.createPalette's comment for why card/sidebar previously drifted toward
        // background instead of matching this (already-correct) popover value.
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.card)
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.popover)
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.sidebar)
        assertColorClose(oklch(0.556f, 0f), ShadcnTheme.ring)
    }

    @Test
    fun shadcnThemeDerivesRadiusScaleFromSingleBaseRadius() {
        // Additive scale (shadcn's own convention, --radius: 0.625rem = 10dp for Vega): sm/md
        // are offset DOWN from base by 4dp/2dp, lg IS base, xl is offset UP by 4dp; xs extends
        // the same 2dp step one further below sm. See ShadcnRadiusScale.fromBase.
        assertTrue(abs(ShadcnTheme.radii.xs.value - 4f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.sm.value - 6f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.md.value - 8f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.lg.value - 10f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.xl.value - 14f) <= 0.0001f)
    }

    @Test
    fun shadcnThemeKeepsInteractiveRolesDistinct() {
        // Real shadcn's own neutral dark theme gives secondary/muted/accent the IDENTICAL
        // oklch(0.269 0 0) (verified against the pinned reference -- see
        // ShadcnReferenceTokenExpandedTest) -- Awake's per-role chroma multipliers
        // (secondary/muted/accent = chroma * 0.55/0.35/0.5) only differentiate them for a base
        // color with real chroma, not Neutral's 0. Distinctness is a per-role-multiplier design
        // choice for chromatic themes, not a shadcn spec guarantee for every base color, so this
        // asserts it against a chromatic base color instead of the Neutral default.
        val theme = shadcnTheme(baseColor = ShadcnBaseColor.Zinc)
        assertTrue(theme.colors.secondary != theme.colors.muted)
        assertTrue(theme.colors.accent != theme.colors.secondary)
        assertTrue(ShadcnTheme.sidebarAccent != ShadcnTheme.sidebar)
    }

    @Test
    fun shadcnThemeFactoryAppliesPresetBaseAndAccentOverrides() {
        val theme = shadcnTheme(
            preset = ShadcnStylePreset.Vega,
            baseColor = ShadcnBaseColor.Neutral,
            accent = ShadcnAccent.Base,
            dark = true,
        ).asShadcnTheme()

        assertEquals(ShadcnStylePreset.Vega, theme.config.preset)
        assertEquals(ShadcnBaseColor.Neutral, theme.config.baseColor)
        assertEquals(ShadcnAccent.Base, theme.config.accent)
        assertTrue(abs(theme.radii.lg.value - 10f) <= 0.0001f)
        assertColorClose(hex(0x09090b), theme.colors.background)
        assertTrue(theme.colors.background != Color.White)
    }

    @Test
    fun oklchProducesExpectedNeutralSrgbValues() {
        assertColorClose(Color(1f, 1f, 1f, 0.1f), oklch(1f, 0f, alpha = 0.1f))
        assertColorClose(Color(0.630163f, 0.630163f, 0.630163f, 1f), oklch(0.708f, 0f))
    }

    @Test
    fun shadcnBadgeRendersFromPublicUiApi() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnBadge(label = "BETA", variant = ShadcnBadgeVariant.Primary)

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.RoundedQuad>(
            primitives.first(),
            "design-system badge should render its own rounded surface",
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "design-system badge should render glyphs",
        )
    }

    @Test
    fun shadcnBadgeReadsConfiguredThemeFromScope() {
        val ui = UiContext()
        val theme = shadcnTheme(
            baseColor = ShadcnBaseColor.Zinc,
            accent = ShadcnAccent.Rose,
        )
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnBadge(label = "LIVE", variant = ShadcnBadgeVariant.Primary)

        val firstQuad = ui.endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        assertColorClose(theme.colors.primary, firstQuad.color)
    }

    @Test
    fun shadcnButtonKeepsClickSemantics() {
        val ui = UiContext()
        var clicked = false
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(200f, 80f, testSnapshot(x = 40f, y = 30f, down = true))
        clicked = ui.createAbsolute(x = 20f, y = 20f)
            .shadcnButton(
                id = "save",
                label = "SAVE",
                modifier = Modifier.width(120f.px).height(32f.px),
                variant = ShadcnButtonVariant.Primary,
            )
        ui.endFrame()
        assertTrue(!clicked)

        ui.beginFrame(200f, 80f, testSnapshot(x = 40f, y = 30f, down = false))
        clicked = ui.createAbsolute(x = 20f, y = 20f)
            .shadcnButton(
                id = "save",
                label = "SAVE",
                modifier = Modifier.width(120f.px).height(32f.px),
                variant = ShadcnButtonVariant.Primary,
            )
        ui.endFrame()

        assertTrue(clicked, "design-system button must preserve the base widget click contract")
    }

    @Test
    fun shadcnSurfaceHostsNestedCustomContent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(240f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createColumn(x = 20f, y = 20f, width = 200f)
            .shadcnSurface(
                "surface",
                modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(100f.px)),
            ) {
                shadcnBadge(label = "READY", modifier = Modifier.width(Dimension.Fixed(80f.px)))
            }

        val primitives = ui.endFrame()
        assertEquals(
            3,
            primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size,
            "surface should emit border + fill, and the filled badge should emit one rounded quad",
        )
    }

    @Test
    fun shadcnDslAdaptersComposeInsideUiDsl() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnSurface(
                id = "dsl-surface",
                modifier = Modifier.height(Dimension.Fixed(120f.px)),
            ) {
                shadcnBadge(label = "READY", variant = ShadcnBadgeVariant.Primary)
                shadcnButton(
                    id = "launch",
                    label = "Launch",
                    modifier = Modifier.width(120f.px).height(32f.px),
                    variant = ShadcnButtonVariant.Secondary,
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "DSL adapters should still render labeled content",
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 4,
            "surface, badge, and button should emit rounded surfaces through the DSL",
        )
    }

    @Test
    fun shadcnFieldWrappersComposeInsideDsl() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnSurface(
                id = "dsl-fields",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                shadcnFieldToggle("show-grid", "Show Grid", checked = true)
                shadcnFieldDropdown("mode", "Mode", listOf("Orbit", "Fly"), selectedIndex = 0)
                shadcnFieldSlider("speed", "Speed", min = 1f, max = 10f, value = 5f)
            }
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "field wrappers should keep text rendering intact",
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 6,
            "surface and field wrappers should emit shaped chrome",
        )
    }

    @Test
    fun shadcnFieldSwitchRendersSwitchPillBesideLabel() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnSurface(
                id = "dsl-switch",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                shadcnFieldSwitch("live-animation", "Live animation", checked = true)
            }
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "label beside the switch should still render text",
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 3,
            "surface, switch pill, and thumb should emit shaped chrome",
        )
    }

    @Test
    fun shadcnSectionHeaderSupportsSlotContent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnSurface(
                id = "slot-header",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                shadcnSectionHeader(
                    title = {
                        row(
                            horizontalArrangement = Arrangement.spacedBy(6f.dp),
                            modifier = Modifier.height(20f.dp.toDimension()),
                        ) {
                            shadcnBadge(
                                label = "NEW",
                                modifier = Modifier.width(Dimension.Fixed(48f.px)),
                                variant = ShadcnBadgeVariant.Outline,
                            )
                            text("Scene Settings")
                        }
                    },
                    description = {
                        shadcnSupportingText("Slot content keeps the header structure reusable.")
                    },
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().isNotEmpty())
    }

    @Test
    fun shadcnPropertyControlsSupportSlotLabels() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnSurface(
                id = "slot-fields",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                shadcnFieldDropdown(
                    id = "mode",
                    options = listOf("Orbit", "Fly"),
                    selectedIndex = 0,
                    labelContent = {
                        text("Camera Mode")
                    },
                )
                shadcnFieldToggle(
                    id = "grid",
                    checked = true,
                    labelContent = {
                        text("Reference Grid")
                    },
                )
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().size >= 5)
    }

    @Test
    fun shadcnFieldDropdownLabelIsVerticallyCenteredInItsRow() {
        // Regression test: the property-row label text() calls never passed
        // verticallyCentered, and text()'s default (verticallyCentered = centered) resolves to
        // false when centered=false is passed for horizontal-only centering -- so the label
        // silently pinned to the top of its 40dp row instead of centering next to the control.
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 120f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnFieldDropdown("mode", "Camera Mode", listOf("Orbit", "Fly"), selectedIndex = 0)
        }

        val label = assertNotNull(
            ui.finishFrame().semantics.firstOrNull { it.label == "Camera Mode" },
        )
        val content = assertNotNull(label.contentBounds, "label should report tight content bounds")
        val slack = label.bounds.height - content.height
        val expectedCenteredY = label.bounds.y + slack / 2f
        assertTrue(
            abs(content.y - expectedCenteredY) < 1f,
            "label should be vertically centered in its row (expected y=$expectedCenteredY, was y=${content.y})",
        )
    }

    @Test
    fun shadcnBadgeSupportsWrapContentMeasurement() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(240f, 120f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnBadge(
                label = "LIVE",
            )

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertEquals(4, glyphs.size)
        assertTrue(
            glyphs.maxOf { it.x + it.w } > 20f,
            "wrap-content badge should size itself to its label",
        )
    }

    @Test
    fun shadcnButtonSlotApiInheritsThemedColorAndCentersContent() {
        val ui = UiContext()
        val theme = ShadcnTheme
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        // Ensure the context's base text style uses the theme's foreground,
        // otherwise it stays at the UiDefaultTheme's default (white).
        ui.pushTextStyle(TextStyle(color = theme.colors.foreground))

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnButton(
                id = "test-btn",
                variant = ShadcnButtonVariant.Primary,
                modifier = Modifier.width(100f.px).height(40f.px),
            ) {
                text("test")
            }

        val primitives = ui.endFrame()
        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "Slot content should render glyphs")

        val primaryForeground = theme.colors.primaryForeground
        glyphs.forEach { glyph ->
            // In Vega dark theme, primary is light and primaryForeground is dark (0.09)
            assertColorClose(primaryForeground, glyph.color)
        }

        val buttonBounds = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        val glyphBoundsX = glyphs.minOf { it.x }
        val glyphBoundsW = glyphs.maxOf { it.x + it.w } - glyphBoundsX
        val glyphCenterX = glyphBoundsX + glyphBoundsW / 2f
        val buttonCenterX = buttonBounds.x + buttonBounds.w / 2f

        assertTrue(
            abs(glyphCenterX - buttonCenterX) < 5f,
            "Text should be roughly horizontally centered in button: glyphCenterX=$glyphCenterX, buttonCenterX=$buttonCenterX",
        )
    }

    @Test
    fun shadcnCardOrdersHeaderBodyFooterWithoutOverlap() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnCard(
                id = "card-full",
                modifier = Modifier.height(Dimension.WrapContent),
                header = { text("Card title") },
                footer = { text("Card footer") },
            ) {
                text("Card body")
            }
        }

        val semantics = ui.finishFrame().semantics
        val header = assertNotNull(semantics.firstOrNull { it.label == "Card title" })
        val body = assertNotNull(semantics.firstOrNull { it.label == "Card body" })
        val footer = assertNotNull(semantics.firstOrNull { it.label == "Card footer" })

        assertTrue(
            header.bounds.y + header.bounds.height <= body.bounds.y + 1f,
            "header must sit above the body, not overlap it",
        )
        assertTrue(
            body.bounds.y + body.bounds.height <= footer.bounds.y + 1f,
            "body must sit above the footer, not overlap it",
        )
    }

    @Test
    fun shadcnCardOmitsDanglingSpaceWhenHeaderOrFooterIsAbsent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnCard(
                id = "card-body-only",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                text("Only body")
            }
        }

        val semantics = ui.finishFrame().semantics
        val card = assertNotNull(semantics.firstOrNull { it.id == "card-body-only" })
        val body = assertNotNull(semantics.firstOrNull { it.label == "Only body" })

        // With no header/footer, the card's wrap-content height should hug the body plus its
        // own padding -- no leftover header/footer divider gap baked in.
        val theme = ShadcnTheme
        val verticalPadding = theme.components.surface.resolve().contentPadding.verticalPx()
        assertTrue(
            abs(card.bounds.height - (body.bounds.height + verticalPadding)) < 2f,
            "header/footer-omitted card should not leave dangling empty space: " +
                "card height=${card.bounds.height}, body height=${body.bounds.height}, padding=$verticalPadding",
        )
    }

    @Test
    fun shadcnFieldGroupStacksFieldsWithSeparatorsWithoutOverlap() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnFieldGroup(id = "settings-group") {
                shadcnFieldToggle("show-grid", "Show Grid", checked = true)
                shadcnFieldSeparator()
                shadcnFieldDropdown("mode", "Mode", listOf("Orbit", "Fly"), selectedIndex = 0)
                shadcnFieldSeparator(label = "more")
                shadcnFieldSlider("speed", "Speed", min = 1f, max = 10f, value = 5f)
            }
        }

        val semantics = ui.finishFrame().semantics
        val gridLabel = assertNotNull(semantics.firstOrNull { it.label == "Show Grid" })
        val modeLabel = assertNotNull(semantics.firstOrNull { it.label == "Mode" })
        val speedLabel = assertNotNull(semantics.firstOrNull { it.label == "Speed" })
        val separatorLabel = assertNotNull(semantics.firstOrNull { it.label == "more" })

        assertTrue(
            gridLabel.bounds.y + gridLabel.bounds.height <= modeLabel.bounds.y + 1f,
            "first field must sit above the second, not overlap it",
        )
        assertTrue(
            modeLabel.bounds.y + modeLabel.bounds.height <= speedLabel.bounds.y + 1f,
            "second field must sit above the third, not overlap it",
        )
        assertTrue(
            modeLabel.bounds.y < separatorLabel.bounds.y && separatorLabel.bounds.y < speedLabel.bounds.y,
            "labeled separator should sit between its two neighboring fields",
        )
        // shadcnFieldGroup's own spacedBy(xl) gap between fields must be reserved even with no
        // divider content -- a group with a separator between three fields must be noticeably
        // taller than the same fields packed with zero spacing.
        val gap = modeLabel.bounds.y - (gridLabel.bounds.y + gridLabel.bounds.height)
        assertTrue(
            gap >= 8f.dp.toPx(),
            "field group spacing should reserve real space between fields, was $gap",
        )
    }

    @Test
    fun shadcnFieldVerticalOrientationStacksLabelAboveControl() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 180f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnField(id = "vertical-field", orientation = ShadcnFieldOrientation.Vertical) {
                shadcnFieldLabel("Display Name", required = true)
                text("Ada Lovelace")
                shadcnFieldDescription("Shown on your public profile.")
            }
        }

        val semantics = ui.finishFrame().semantics
        val nameValue = assertNotNull(semantics.firstOrNull { it.label == "Ada Lovelace" })
        val description = assertNotNull(
            semantics.firstOrNull { it.label == "Shown on your public profile." },
        )
        assertTrue(
            nameValue.bounds.y + nameValue.bounds.height <= description.bounds.y + 1f,
            "vertical field must stack control above its description, not overlap it",
        )
    }

    @Test
    fun shadcnFieldErrorRendersDestructiveColoredText() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnField(id = "error-field", orientation = ShadcnFieldOrientation.Vertical) {
                shadcnFieldLabel("Email")
                shadcnFieldError("Required")
            }
        }

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        val destructive = ShadcnTheme.colors.destructive
        assertTrue(
            glyphs.any { abs(it.color.r - destructive.r) < 0.01f && abs(it.color.g - destructive.g) < 0.01f },
            "shadcnFieldError text should render in the theme's destructive color",
        )
    }

    @Test
    fun shadcnFieldSetRendersLegendAboveContentWithoutOverlap() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 220f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
            shadcnFieldSet(id = "payment-fieldset") {
                shadcnFieldLegend("Payment Method")
                shadcnFieldDescription("All transactions are secure and encrypted")
                shadcnFieldGroup {
                    shadcnField(id = "card-name", orientation = ShadcnFieldOrientation.Vertical) {
                        shadcnFieldLabel("Name on Card")
                        text("Evil Rabbit")
                    }
                }
            }
        }

        val semantics = ui.finishFrame().semantics
        val legend = assertNotNull(semantics.firstOrNull { it.label == "Payment Method" })
        val description = assertNotNull(
            semantics.firstOrNull { it.label == "All transactions are secure and encrypted" },
        )
        val cardName = assertNotNull(semantics.firstOrNull { it.label == "Evil Rabbit" })
        assertTrue(
            legend.bounds.y + legend.bounds.height <= description.bounds.y + 1f,
            "field set legend must render above its description, not overlap it",
        )
        assertTrue(
            description.bounds.y + description.bounds.height <= cardName.bounds.y + 1f,
            "field set legend/description must render above the nested field group content",
        )
    }

    @Test
    fun shadcnPopoverAnchorsContentBelowAndCenteredOnItsTrigger() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 240f, testSnapshot(x = -100f, y = -100f, down = false))

        val anchorSlot = ui.createAbsolute(x = 40f, y = 40f)
            .shadcnSurface(
                id = "trigger",
                modifier = Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(32f.px)),
            ) { }

        val result = ui.createAbsolute(slot = ui.frameBounds())
            .shadcnPopover(
                id = "popover",
                anchorSlot = anchorSlot,
                expanded = true,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(60f.px),
            ) { }

        ui.endFrame()

        val slot = assertNotNull(result.slot, "expanded popover should place its content")
        val expectedX = anchorSlot.x + anchorSlot.width / 2f - slot.width / 2f
        val expectedY = anchorSlot.y + anchorSlot.height + 4f.dp.toPx()
        assertTrue(
            abs(slot.x - expectedX) < 1f,
            "popover should be centered under its anchor: expected x=$expectedX, was x=${slot.x}",
        )
        assertTrue(
            abs(slot.y - expectedY) < 1f,
            "popover should sit just below its anchor: expected y=$expectedY, was y=${slot.y}",
        )
        assertTrue(!result.dismissed, "no click occurred, popover must not report dismissed")
    }

    @Test
    fun shadcnPopoverCollapsesToNoContentWhenNotExpanded() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 240f, testSnapshot(x = -100f, y = -100f, down = false))

        val anchorSlot = ui.createAbsolute(x = 40f, y = 40f)
            .shadcnSurface(
                id = "trigger",
                modifier = Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(32f.px)),
            ) { }

        val result = ui.createAbsolute(slot = ui.frameBounds())
            .shadcnPopover(
                id = "popover",
                anchorSlot = anchorSlot,
                expanded = false,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(60f.px),
            ) { }

        ui.endFrame()

        assertEquals(null, result.slot, "collapsed popover must not claim/place a content slot")
        assertTrue(!result.dismissed, "collapsed popover never had a chance to dismiss")
    }

    @Test
    fun shadcnPopoverDismissesOnOutsideClickButNotOnInsideClick() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        fun frame(pointerX: Float, pointerY: Float, down: Boolean) = run {
            ui.beginFrame(320f, 240f, testSnapshot(x = pointerX, y = pointerY, down = down))
            val anchorSlot = ui.createAbsolute(x = 40f, y = 40f)
                .shadcnSurface(
                    id = "trigger",
                    modifier = Modifier.width(Dimension.Fixed(80f.px))
                        .height(Dimension.Fixed(32f.px)),
                ) { }
            val result = ui.createAbsolute(slot = ui.frameBounds())
                .shadcnPopover(
                    id = "popover",
                    anchorSlot = anchorSlot,
                    expanded = true,
                    width = Dimension.Fixed(120f.px),
                    height = Dimension.Fixed(60f.px),
                ) { }
            ui.endFrame()
            result
        }

        val insideClick = frame(pointerX = 100f, pointerY = 100f, down = true)
        assertTrue(
            !insideClick.dismissed,
            "clicking inside the popover content must not dismiss it",
        )

        val outsideClick = frame(pointerX = 5f, pointerY = 5f, down = true)
        assertTrue(
            outsideClick.dismissed,
            "clicking outside the anchor and content must dismiss the popover",
        )
    }

    @Test
    fun rowWithFixedWeightFixedShadcnSurfaceSiblingsLaysOutSideBySide() {
        // Reproduces samples/scene3d-playground's live shell bug: a row of
        // shadcnSidebar(fixed) | column(weight(1f)) | shadcnSurface(fixed), where the fixed
        // siblings are real shadcn composite widgets with actual content (not raw claimSlot()
        // calls, and not plain headless column()/row() -- see RowColumnWeightCacheTest /
        // LayoutTest's existing weight() coverage for those simpler shapes). Root cause: the
        // shared ui-core surface() widget (which shadcnSidebar/shadcnSurface/shadcnCard all
        // route through) didn't suppress its own children's claimSlot() recording during its
        // content dispatch -- unlike row()/column(), which already do (see
        // UiContext.withMeasuredRecordingSuppressed). When a fixed-width surface() sibling with
        // real content sits next to a weight()-tagged sibling, forcing this row into its
        // trial-measurement (hasWeightedChild/plannedSlots) path, the surface's own children's
        // claims used to leak into this row's measuredSlots/measuredWeights/fillsMainAxis
        // trial, desyncing resolveWeightedMainAxis()'s index pairing and this row's
        // plannedSlots consumption order -- silently handing the weighted and trailing-fixed
        // sibling the wrong slot (see Surface.kt's fix).
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(1200f, 800f, testSnapshot(x = -100f, y = -100f, down = false))

        var sidebarBounds: UiBounds? = null
        var viewportBounds: UiBounds? = null
        var controlsBounds: UiBounds? = null
        ui.createColumn(x = 0f, y = 0f, width = 1200f, height = 800f).row(
            id = "scene3d-playground-shell",
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        ) {
            sidebarBounds = shadcnSidebar(
                id = "scene3d-demo-menu",
                modifier = Modifier.width(200f.dp).height(Dimension.FillMax),
            ) {
                text("Skinned mesh")
            }
            viewportBounds = column(
                id = "scene3d-viewport",
                modifier = Modifier.weight(1f).height(Dimension.FillMax),
            ) {
                text("Skinned mesh -- no skeleton")
            }
            controlsBounds = shadcnSurface(
                id = "scene3d-controls-panel",
                modifier = Modifier.width(220f.dp).height(Dimension.FillMax),
            ) {
                text("Root")
            }
        }
        ui.finishFrame()

        val sidebar = requireNotNull(sidebarBounds)
        val viewport = requireNotNull(viewportBounds)
        val controls = requireNotNull(controlsBounds)

        assertTrue(
            viewport.x >= sidebar.x + sidebar.width - 1f,
            "viewport must start after the sidebar, not overlap it -- sidebar=$sidebar viewport=$viewport",
        )
        assertTrue(
            controls.x >= viewport.x + viewport.width - 1f,
            "controls must start after the viewport, not overlap it -- viewport=$viewport controls=$controls",
        )
        assertTrue(
            viewport.width > 300f,
            "viewport must occupy the large remaining middle area (not get starved by a " +
                "grandchild's leaked slot), got width=${viewport.width}",
        )
        assertEquals(220f.dp.toPx(), controls.width, "controls must keep its own fixed 220dp width")
        assertEquals(
            1200f - 220f.dp.toPx(),
            controls.x,
            "controls must sit flush against the row's right edge",
        )
    }

    @Test
    fun shadcnSidebarOrdersHeaderContentFooterWithoutOverlap() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnSidebar(
                id = "sidebar-full",
                modifier = Modifier.height(Dimension.WrapContent),
                header = { text("Sidebar title") },
                footer = { text("Sidebar footer") },
            ) {
                text("Sidebar content")
            }
        }

        val semantics = ui.finishFrame().semantics
        val header = assertNotNull(semantics.firstOrNull { it.label == "Sidebar title" })
        val content = assertNotNull(semantics.firstOrNull { it.label == "Sidebar content" })
        val footer = assertNotNull(semantics.firstOrNull { it.label == "Sidebar footer" })

        assertTrue(
            header.bounds.y + header.bounds.height <= content.bounds.y + 1f,
            "header must sit above the content, not overlap it",
        )
        assertTrue(
            content.bounds.y + content.bounds.height <= footer.bounds.y + 1f,
            "content must sit above the footer, not overlap it",
        )
    }

    @Test
    fun shadcnSidebarOmitsDanglingSpaceWhenHeaderOrFooterIsAbsent() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(280f, 260f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(240f.dp)) {
            shadcnSidebar(
                id = "sidebar-content-only",
                modifier = Modifier.height(Dimension.WrapContent),
            ) {
                text("Only content")
            }
        }

        val semantics = ui.finishFrame().semantics
        val sidebar = assertNotNull(semantics.firstOrNull { it.id == "sidebar-content-only" })
        val content = assertNotNull(semantics.firstOrNull { it.label == "Only content" })

        // With no header/footer, the sidebar's wrap-content height should hug the content
        // plus its own padding -- no leftover header/footer divider gap baked in.
        val verticalPadding = 16f.dp.toPx()
        assertTrue(
            abs(sidebar.bounds.height - (content.bounds.height + verticalPadding)) < 2f,
            "header/footer-omitted sidebar should not leave dangling empty space: " +
                "sidebar height=${sidebar.bounds.height}, content height=${content.bounds.height}, padding=$verticalPadding",
        )
    }
}

private fun assertColorClose(expected: Color, actual: Color, tolerance: Float = 0.005f) {
    listOf(
        expected.r to actual.r,
        expected.g to actual.g,
        expected.b to actual.b,
        expected.a to actual.a,
    ).forEachIndexed { index, (expectedChannel, actualChannel) ->
        assertTrue(
            abs(expectedChannel - actualChannel) <= tolerance,
            "channel $index expected $expectedChannel but was $actualChannel",
        )
    }
}
