// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import kotlin.test.Test
import kotlin.test.assertTrue

class ShadcnBadgeIntrinsicWidthTest {

    @Test
    fun columnBadgeUsesNaturalWidthInsteadOfSurfaceWidth() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnBadge(
                id = "column-badge",
                label = "INPUTS",
                variant = ShadcnBadgeVariant.Outline,
            )
        }

        val badge = ui.finishFrame().semantics.first { it.id == "column-badge" }
        assertTrue(badge.bounds.width < 200f, "column badges should remain compact inside a Column")
    }
}
