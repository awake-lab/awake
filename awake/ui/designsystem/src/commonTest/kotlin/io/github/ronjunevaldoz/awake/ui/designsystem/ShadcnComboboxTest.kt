// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertTrue

/** The behavior contract lives in Headless; this fixture proves the DS test has no bridge need. */
class ShadcnComboboxTest {

    @Test
    fun comboboxComposesThroughHeadlessScope() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 180f, UiInputState())
        ui.headlessRoot().combobox(
            id = "fruit",
            options = listOf("Apple", "Banana", "Cherry"),
            selectedIndex = 1,
            modifier = Modifier.width(200f.dp),
        )
        val frame = ui.finishFrame()
        assertTrue(frame.semantics.any { it.id == "fruit" })
        assertTrue(frame.primitives.isNotEmpty())
    }
}
