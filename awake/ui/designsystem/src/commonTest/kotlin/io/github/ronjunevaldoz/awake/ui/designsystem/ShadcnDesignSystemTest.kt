// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Public design-system contract tests. Composition is deliberately exercised through UiScope. */
class ShadcnDesignSystemTest {

    @Test
    fun shadcnThemeTracksOfficialNeutralDarkRoles() {
        assertColorClose(Color(0.039388f, 0.039388f, 0.039388f, 1f), ShadcnTheme.colors.background)
        assertColorClose(Color(0.980256f, 0.980256f, 0.980256f, 1f), ShadcnTheme.colors.foreground)
        assertColorClose(Color(0.898161f, 0.898161f, 0.898161f, 1f), ShadcnTheme.colors.primary)
        assertColorClose(Color(1f, 1f, 1f, 0.1f), ShadcnTheme.colors.border)
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.card)
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.popover)
        assertColorClose(oklch(0.205f, 0f), ShadcnTheme.sidebar)
        assertColorClose(oklch(0.556f, 0f), ShadcnTheme.ring)
    }

    @Test
    fun shadcnThemeDerivesRadiusScaleFromSingleBaseRadius() {
        assertTrue(abs(ShadcnTheme.radii.xs.value - 4f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.sm.value - 6f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.md.value - 8f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.lg.value - 10f) <= 0.0001f)
        assertTrue(abs(ShadcnTheme.radii.xl.value - 14f) <= 0.0001f)
    }

    @Test
    fun shadcnThemeKeepsInteractiveRolesDistinct() {
        val theme = shadcnThemeValues(baseColor = ShadcnBaseColor.Zinc)
        assertTrue(theme.colors.secondary != theme.colors.muted)
        assertTrue(theme.colors.accent != theme.colors.secondary)
        assertTrue(ShadcnTheme.sidebarAccent != ShadcnTheme.sidebar)
    }

    @Test
    fun shadcnThemeFactoryAppliesPresetBaseAndAccentOverrides() {
        val theme = shadcnThemeValues(
            preset = ShadcnStylePreset.Vega,
            baseColor = ShadcnBaseColor.Neutral,
            accent = ShadcnAccent.Base,
            dark = true,
        ).resolved
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
    fun recipesComposeThroughPublicHeadlessScope() {
        val frame = renderShadcnComponent(width = 320f, height = 200f, font = BitmapFont()) {
            column(Modifier.fillMaxSize()) {
                shadcnSurface(id = "surface", modifier = Modifier.width(280f.dp)) {
                    shadcnBadge(id = "status", label = "READY", variant = ShadcnBadgeVariant.Primary)
                    shadcnButton(id = "launch", label = "Launch", variant = ShadcnButtonVariant.Secondary)
                }
                shadcnCard(id = "card") { text("Card body") }
            }
        }
        assertTrue(frame.primitives.isNotEmpty())
        assertTrue(frame.semantics.any { it.id == "surface" })
        assertTrue(frame.semantics.any { it.id == "launch" })
        assertTrue(frame.semantics.any { it.id == "card" })
    }

    private fun assertColorClose(expected: Color, actual: Color, tolerance: Float = 0.08f) {
        assertTrue(abs(expected.r - actual.r) <= tolerance)
        assertTrue(abs(expected.g - actual.g) <= tolerance)
        assertTrue(abs(expected.b - actual.b) <= tolerance)
        assertTrue(abs(expected.a - actual.a) <= tolerance)
    }
}
