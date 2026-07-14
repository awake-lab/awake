// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals

class UiThemeTest {

    @Test
    fun neutralStyleResolvesDistinctColorsPerState() {
        val tokens = DefaultUiTheme.tokens
        val idle = tokens.neutralStyle().resolve(MutableStyleState(hovered = false, active = false)).background!!
        val hovered = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        val active = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = true)).background!!

        assertNotEquals(idle.toList(), hovered.toList(), "hovered must resolve to a different color than idle")
        assertNotEquals(hovered.toList(), active.toList(), "active must resolve to a different color than hovered")
    }

    @Test
    fun neutralStyleIsStableAcrossRepeatedCalls() {
        val tokens = DefaultUiTheme.tokens
        val first = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        val second = tokens.neutralStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        assertContentEquals(first, second, "the same state must always resolve to the same color")
    }

    @Test
    fun defaultUiThemeSharesOneNeutralStyleAcrossWidgetKinds() {
        // Built-ins intentionally share the same neutral-first base style. This pins that down
        // so component defaults don't silently drift apart.
        val tokens = DefaultUiTheme.tokens
        val state = MutableStyleState(hovered = true, active = false)
        val a = DefaultUiTheme.components.button.resolve(state).background!!
        val b = DefaultUiTheme.components.slider.resolve(state).background!!
        assertContentEquals(a, b)
    }

    @Test
    fun destructiveStyleVariesByStateInsteadOfReturningOneFlatColor() {
        // A naive destructiveStyle() that ignores StyleState would regress versus the old
        // hand-rolled DangerUiTheme.
        val tokens = DefaultUiTheme.tokens
        val idle = tokens.destructiveStyle().resolve(MutableStyleState(hovered = false, active = false)).background!!
        val hovered = tokens.destructiveStyle().resolve(MutableStyleState(hovered = true, active = false)).background!!
        val active = tokens.destructiveStyle().resolve(MutableStyleState(hovered = true, active = true)).background!!

        assertNotEquals(idle.toList(), hovered.toList(), "hovered destructive must differ from idle")
        assertNotEquals(hovered.toList(), active.toList(), "active destructive must differ from hovered")
    }

    @Test
    fun uiThemeSeparatesTokensFromComponentDefaults() {
        val custom = object : UiTheme {
            override val tokens = DefaultUiTheme.tokens
            override val components = DefaultUiTheme.components
        }
        assertContentEquals(DefaultUiTheme.tokens.background, custom.tokens.background)
        assertContentEquals(
            DefaultUiTheme.components.button.resolve().background!!,
            custom.components.button.resolve().background!!
        )
    }
}
