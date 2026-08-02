// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.theme.destructiveStyle
import io.github.ronjunevaldoz.awake.ui.theme.neutralStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class UiThemeTest {

    @Test
    fun neutralStyleResolvesDistinctColorsPerState() {
        val tokens = UiFallbackTheme.tokens
        val idle = tokens.neutralStyle().resolve(MutableStyleState(hovered = false, active = false)).background!!
        val hovered = tokens.neutralStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false
            )
        ).background!!
        val active = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = true)).background!!

        assertNotEquals(idle, hovered, "hovered must resolve to a different color than idle")
        assertNotEquals(hovered, active, "active must resolve to a different color than hovered")
    }

    @Test
    fun neutralStyleIsStableAcrossRepeatedCalls() {
        val tokens = UiFallbackTheme.tokens
        val first = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        val second = tokens.neutralStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false
            )
        ).background!!
        assertEquals(first, second, "the same state must always resolve to the same color")
    }

    @Test
    fun coreUiThemeSharesOneNeutralStyleAcrossWidgetKinds() {
        val state = MutableStyleState(hovered = true, active = false)
        val a = UiFallbackTheme.components.button.resolve(state).background!!
        val b = UiFallbackTheme.components.slider.resolve(state).background!!
        assertEquals(a, b)
    }

    @Test
    fun destructiveStyleVariesByStateInsteadOfReturningOneFlatColor() {
        val tokens = UiFallbackTheme.tokens
        val idle = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = false,
                active = false
            )
        ).background!!
        val hovered = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = false
            )
        ).background!!
        val active = tokens.destructiveStyle().resolve(
            MutableStyleState(
                hovered = true,
                active = true
            )
        ).background!!

        assertNotEquals(idle, hovered, "hovered destructive must differ from idle")
        assertNotEquals(hovered, active, "active destructive must differ from hovered")
    }

    @Test
    fun uiThemeSeparatesTokensFromComponentDefaults() {
        val custom = object : UiTheme {
            override val tokens = UiFallbackTheme.tokens
            override val components = UiFallbackTheme.components
        }
        assertEquals(UiFallbackTheme.tokens.background, custom.tokens.background)
        assertEquals(
            UiFallbackTheme.components.button.resolve().background!!,
            custom.components.button.resolve().background!!
        )
    }
}
