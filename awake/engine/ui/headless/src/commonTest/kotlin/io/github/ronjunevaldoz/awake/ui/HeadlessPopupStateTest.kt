// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeadlessPopupStateTest {

    @Test
    fun facadePreservesPopupStateAcrossFrames() {
        val ui = UiContext()
        ui.beginFrame(320f, 200f, testSnapshot())
        val first = ui.createUiScope(UiBounds(0f, 0f, 0f, 0f)).rememberPopupState("menu")
        first.open()

        ui.beginFrame(320f, 200f, testSnapshot())
        val second = ui.createUiScope(UiBounds(0f, 0f, 0f, 0f)).rememberPopupState("menu")
        assertTrue(second.expanded)
        second.close()

        ui.beginFrame(320f, 200f, testSnapshot())
        val third = ui.createUiScope(UiBounds(0f, 0f, 0f, 0f)).rememberPopupState("menu")
        assertFalse(third.expanded)
    }
}
