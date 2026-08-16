// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnSheetSide
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSheet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToast
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertNull

class ShadcnRemainingRecipeTest {
    @Test
    fun comboboxSheetAndToastUseThePublicHeadlessBoundary() {
        renderShadcnComponent(width = 320f, height = 240f) {
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

    }
}
