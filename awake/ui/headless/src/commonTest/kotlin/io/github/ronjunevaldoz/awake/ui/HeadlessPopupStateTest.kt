// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.UiPopupState
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeadlessPopupStateTest {

    @Test
    fun facadePreservesPopupStateAcrossFrames() {
        uiTestSession(width = 320f, height = 200f) {
            lateinit var first: UiPopupState
            frame { first = rememberPopupState("menu") }
            first.open()

            lateinit var second: UiPopupState
            frame { second = rememberPopupState("menu") }
            assertTrue(second.expanded)
            second.close()

            lateinit var third: UiPopupState
            frame { third = rememberPopupState("menu") }
            assertFalse(third.expanded)
        }
    }
}
