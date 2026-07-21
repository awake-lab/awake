// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiStateHooksTest {

    @Test
    fun rememberStateValuePersistsAcrossFramesForTheSameId() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot())
        val first = ui.rememberIntState("counter")
        first.value += 1

        ui.beginFrame(320f, 200f, testSnapshot())
        val second = ui.rememberIntState("counter")

        assertEquals(1, second.value)
    }

    @Test
    fun rememberStateValueKeepsKeysIndependentWithinOneWidget() {
        val widgetState = WidgetState()
        val expanded = widgetState.rememberStateValue("expanded") { false }
        val clicks = widgetState.rememberStateValue("clicks") { 0 }

        expanded.value = true
        clicks.value = 3

        assertTrue(expanded.value)
        assertEquals(3, clicks.value)
    }

    @Test
    fun resetRemovesStoredValueAndRestoresInitial() {
        val widgetState = WidgetState()
        val remembered = widgetState.rememberStateValue("mode") { "orbit" }

        remembered.value = "fly"
        remembered.reset()

        assertEquals("orbit", remembered.value)
    }

    @Test
    fun rememberBooleanStateCanBeUsedAsAPropertyDelegate() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot())
        val scope = ui.createAbsolute(0f, 0f)
        var expanded by scope.rememberBooleanState("delegate-demo", initial = true)
        expanded = false

        ui.beginFrame(320f, 200f, testSnapshot())
        val nextScope = ui.createAbsolute(0f, 0f)
        val persisted = nextScope.rememberBooleanState("delegate-demo", initial = true)

        assertFalse(persisted.value)
    }

    @Test
    fun rememberPopupStatePersistsAndSupportsToggleHelpers() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot())
        val scope = ui.createAbsolute(0f, 0f)
        val popupState = scope.rememberPopupState("menu")
        popupState.open()
        popupState.toggle()
        popupState.toggle()

        ui.beginFrame(320f, 200f, testSnapshot())
        val nextScope = ui.createAbsolute(0f, 0f)
        val persisted = nextScope.rememberPopupState("menu")
        assertTrue(persisted.expanded)

        persisted.close()

        ui.beginFrame(320f, 200f, testSnapshot())
        val finalScope = ui.createAbsolute(0f, 0f)
        val closed = finalScope.rememberPopupState("menu")
        assertFalse(closed.expanded)
    }
}
