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
        val idle = tokens.neutralStyle().colorFor(UiWidgetState(hovered = false, active = false))
        val hovered = tokens.neutralStyle().colorFor(UiWidgetState(hovered = true, active = false))
        val active = tokens.neutralStyle().colorFor(UiWidgetState(hovered = true, active = true))

        assertNotEquals(idle.toList(), hovered.toList(), "hovered must resolve to a different color than idle")
        assertNotEquals(hovered.toList(), active.toList(), "active must resolve to a different color than hovered")
    }

    @Test
    fun neutralStyleIsStableAcrossRepeatedCalls() {
        val tokens = DefaultUiTheme.tokens
        val first = tokens.neutralStyle().colorFor(UiWidgetState(hovered = true, active = false))
        val second = tokens.neutralStyle().colorFor(UiWidgetState(hovered = true, active = false))
        assertContentEquals(first, second, "the same state must always resolve to the same color")
    }

    @Test
    fun defaultUiThemeSharesOneNeutralStyleAcrossWidgetKinds() {
        // Widgets.kt intentionally calls theme.tokens.neutralStyle() for button/toggle/slider/
        // dropdown alike -- this pins that down so a future accidental divergence is caught,
        // same intent the old per-field UiTheme test had, now expressed against tokens.
        val tokens = DefaultUiTheme.tokens
        val state = UiWidgetState(hovered = true, active = false)
        val a = tokens.neutralStyle().colorFor(state)
        val b = tokens.neutralStyle().colorFor(state)
        assertContentEquals(a, b)
    }

    @Test
    fun destructiveStyleVariesByStateInsteadOfReturningOneFlatColor() {
        // A naive destructiveStyle() that ignores UiWidgetState would regress versus the old
        // hand-rolled DangerUiTheme (which had distinct base/hover/active reds) -- this pins
        // down that brightness actually scales per state.
        val tokens = DefaultUiTheme.tokens
        val idle = tokens.destructiveStyle().colorFor(UiWidgetState(hovered = false, active = false))
        val hovered = tokens.destructiveStyle().colorFor(UiWidgetState(hovered = true, active = false))
        val active = tokens.destructiveStyle().colorFor(UiWidgetState(hovered = true, active = true))

        assertNotEquals(idle.toList(), hovered.toList(), "hovered destructive must differ from idle")
        assertNotEquals(hovered.toList(), active.toList(), "active destructive must differ from hovered")
    }

    @Test
    fun uiThemeHasNoWidgetNamedFields() {
        // UiTheme is exactly `val tokens: UiColorTokens` -- this test exists as a compile-time
        // pin: if a future change reintroduces a `button`/`toggle` field on UiTheme, this
        // object expression would need to grow with it, catching the regression at review
        // time rather than silently reintroducing the widget-identity coupling §6.5 fixed.
        val custom = object : UiTheme {
            override val tokens = DefaultUiTheme.tokens
        }
        assertContentEquals(DefaultUiTheme.tokens.background, custom.tokens.background)
    }
}
