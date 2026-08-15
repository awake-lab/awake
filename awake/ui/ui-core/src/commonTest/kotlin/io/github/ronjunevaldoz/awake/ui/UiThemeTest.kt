// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.theme.destructiveStyle
import io.github.ronjunevaldoz.awake.ui.theme.neutralStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UiThemeTest {

    @Test
    fun neutralStyleResolvesDistinctColorsPerState() {
        val tokens = UiDefaultTheme.colors
        val idle = tokens.neutralStyle().resolve(MutableStyleState(hovered = false, active = false)).background!!
        val hovered = tokens.neutralStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false,
            ),
        ).background!!
        val active = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = true)).background!!

        assertNotEquals(idle, hovered, "hovered must resolve to a different color than idle")
        assertNotEquals(hovered, active, "active must resolve to a different color than hovered")
    }

    @Test
    fun neutralStyleIsStableAcrossRepeatedCalls() {
        val tokens = UiDefaultTheme.colors
        val first = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        val second = tokens.neutralStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false,
            ),
        ).background!!
        assertEquals(first, second, "the same state must always resolve to the same color")
    }

    @Test
    fun coreUiThemeSharesOneNeutralStyleAcrossWidgetKinds() {
        val state = MutableStyleState(hovered = true, active = false)
        val a = UiDefaultTheme.components.button.resolve(state).background!!
        val b = UiDefaultTheme.components.slider.resolve(state).background!!
        assertEquals(a, b)
    }

    @Test
    fun destructiveStyleVariesByStateInsteadOfReturningOneFlatColor() {
        val tokens = UiDefaultTheme.colors
        val idle = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = false,
                active = false,
            ),
        ).background!!
        val hovered = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false,
            ),
        ).background!!
        val active = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = true,
            ),
        ).background!!

        assertNotEquals(idle, hovered, "hovered destructive must differ from idle")
        assertNotEquals(hovered, active, "active destructive must differ from hovered")
    }

    // Reproduces the ShadcnTheme composition shape: `style(visual, fallback) = fallback then
    // Style { visual overrides }`, where `visual` sets a plain unconditional background (like
    // slider/toggle/switch's static token color) on top of a fallback that also varies by
    // state. The unconditional override must not silently win over the state rule just because
    // it was declared in a later `then` block.
    @Test
    fun unconditionalOverrideDoesNotClobberStateRuleFromAnEarlierThenBlock() {
        val staticOverrideColor = Color(r = 0.1f, g = 0.2f, b = 0.3f)
        val hoverColor = Color(r = 0.5f, g = 0.5f, b = 0.5f)
        val fallbackWithStateRule = Style {
            background(Color(r = 1f, g = 1f, b = 1f))
            hovered { background(hoverColor) }
        }
        val staticVisualOverride = Style {
            background(staticOverrideColor)
        }
        val composed = fallbackWithStateRule then staticVisualOverride

        val idle = composed.resolve(MutableStyleState(hovered = false)).background!!
        val hovered = composed.resolve(MutableStyleState(hovered = true)).background!!

        assertEquals(staticOverrideColor, idle, "idle must use the themed static override")
        assertEquals(hoverColor, hovered, "hover must still win even though the static override was declared later")
        assertNotEquals(idle, hovered, "hover must resolve to a different color than idle")
    }

    @Test
    fun uiThemeSeparatesTokensFromComponentDefaults() {
        val custom = object : UiTheme {
            override val colors = UiDefaultTheme.colors
            override val components = UiDefaultTheme.components
        }
        assertEquals(UiDefaultTheme.colors.background, custom.colors.background)
        assertEquals(
            UiDefaultTheme.components.button.resolve().background!!,
            custom.components.button.resolve().background!!,
        )
    }
}
