// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiStateHooksTest {

    @Test
    fun rememberStateValuePersistsAcrossFramesForTheSameId() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot())
        var first by ui.rememberIntState("counter")
        first += 1

        ui.beginFrame(320f, 200f, testSnapshot())
        val second by ui.rememberIntState("counter")

        assertEquals(1, second)
    }

    @Test
    fun rememberStateValueKeepsKeysIndependentWithinOneWidget() {
        val widgetState = WidgetState()
        var expanded by widgetState.rememberStateValue("expanded") { false }
        var clicks by widgetState.rememberStateValue("clicks") { 0 }

        expanded = true
        clicks = 3

        assertTrue(expanded)
        assertEquals(3, clicks)
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
        val scope = ui.createAbsolute(slot = UiBounds(0f, 0f, 0f, 0f))
        var expanded by scope.rememberBooleanState("delegate-demo", initial = true)
        expanded = false

        ui.beginFrame(320f, 200f, testSnapshot())
        val nextScope = ui.createAbsolute(slot = UiBounds(0f, 0f, 0f, 0f))
        val persisted by nextScope.rememberBooleanState("delegate-demo", initial = true)

        assertFalse(persisted)
    }

    @Test
    fun rememberPopupStatePersistsAndSupportsToggleHelpers() {
        val ui = UiContext()

        ui.beginFrame(320f, 200f, testSnapshot())
        val scope = ui.createAbsolute(slot = UiBounds(0f, 0f, 0f, 0f))
        val popupState = scope.rememberPopupState("menu")
        popupState.open()
        popupState.toggle()
        popupState.toggle()

        ui.beginFrame(320f, 200f, testSnapshot())
        val nextScope = ui.createAbsolute(slot = UiBounds(0f, 0f, 0f, 0f))
        val persisted = nextScope.rememberPopupState("menu")
        assertTrue(persisted.expanded)

        persisted.close()

        ui.beginFrame(320f, 200f, testSnapshot())
        val finalScope = ui.createAbsolute(slot = UiBounds(0f, 0f, 0f, 0f))
        val closed = finalScope.rememberPopupState("menu")
        assertFalse(closed.expanded)
    }
}
