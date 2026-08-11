// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.UiTabItem
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import kotlin.test.Test
import kotlin.test.assertTrue

/** Verifies the public Shadcn tabs recipe is composed through Headless scopes. */
class ShadcnTabsFidelityTest {

    @Test
    fun shadcnTabsRendersTrackAndTriggers() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(400f, 200f, UiInputState())
        ui.headlessRoot().column(Modifier.fillMaxSize()) {
            shadcnTabs(
                id = "tabs-fidelity",
                items = listOf(UiTabItem("account", "Account"), UiTabItem("password", "Password")),
                selected = "account",
            )
        }
        val frame = ui.finishFrame()
        assertTrue(frame.primitives.isNotEmpty())
        assertTrue(frame.semantics.any { it.id == "tabs-fidelity.track" })
        assertTrue(frame.semantics.any { it.id == "tabs-fidelity.account" })
        assertTrue(frame.semantics.any { it.id == "tabs-fidelity.password" })
    }
}
