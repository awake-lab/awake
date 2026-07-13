// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals

class UiThemeTest {

    @Test
    fun defaultThemeButtonResolvesDistinctColorsPerState() {
        val idle = DefaultUiTheme.button.colorFor(UiWidgetState(hovered = false, active = false))
        val hovered = DefaultUiTheme.button.colorFor(UiWidgetState(hovered = true, active = false))
        val active = DefaultUiTheme.button.colorFor(UiWidgetState(hovered = true, active = true))

        assertNotEquals(idle.toList(), hovered.toList(), "hovered must resolve to a different color than idle")
        assertNotEquals(hovered.toList(), active.toList(), "active must resolve to a different color than hovered")
    }

    @Test
    fun defaultThemeButtonIsStableAcrossRepeatedCalls() {
        val first = DefaultUiTheme.button.colorFor(UiWidgetState(hovered = true, active = false))
        val second = DefaultUiTheme.button.colorFor(UiWidgetState(hovered = true, active = false))
        assertContentEquals(first, second, "the same state must always resolve to the same color")
    }

    @Test
    fun defaultThemeSharesOneNeutralStyleAcrossWidgetKinds() {
        // DefaultUiTheme intentionally reuses one neutral UiStyle for every widget kind today
        // (see UiTheme.kt) -- this pins that down so a future accidental divergence between
        // button/toggle/slider/dropdown colors is caught.
        val state = UiWidgetState(hovered = true, active = false)
        assertContentEquals(DefaultUiTheme.button.colorFor(state), DefaultUiTheme.toggle.colorFor(state))
        assertContentEquals(DefaultUiTheme.button.colorFor(state), DefaultUiTheme.slider.colorFor(state))
        assertContentEquals(DefaultUiTheme.button.colorFor(state), DefaultUiTheme.dropdown.colorFor(state))
    }
}
