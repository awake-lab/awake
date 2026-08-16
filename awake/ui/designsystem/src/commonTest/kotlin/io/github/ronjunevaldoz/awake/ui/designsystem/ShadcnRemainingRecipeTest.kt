// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnSheetSide
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSheet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToast
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertNull

class ShadcnRemainingRecipeTest {
    @Test
    fun comboboxSheetAndToastUseThePublicHeadlessBoundary() {
        val context = UiContext()
        context.beginFrame(320f, 240f, UiInputState())
        context.createUiScope(UiBounds(0f, 0f, 320f, 240f)).shadcnTheme {
            assertNull(
                combobox(
                id = "settings.theme",
                options = listOf("Light", "Dark"),
                selectedIndex = null,
                ),
            )
            shadcnSheet(
                id = "settings.sheet",
                expanded = true,
                onDismissRequest = {},
                side = ShadcnSheetSide.Right,
            ) { _ -> text("Settings") }
            shadcnToast(id = "settings.saved", message = "Saved")
        }

        context.endFrame()
    }
}
