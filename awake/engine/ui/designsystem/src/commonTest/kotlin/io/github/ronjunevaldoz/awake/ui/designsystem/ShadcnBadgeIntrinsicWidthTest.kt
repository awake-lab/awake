// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import kotlin.test.Test
import kotlin.test.assertEquals
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

        val frame = ui.finishFrame()
        val badge = frame.semantics.first { it.id == "column-badge" }
        val label = frame.semantics.first { it.role == UiSemanticRole.Text && it.label == "INPUTS" }
        assertTrue(badge.bounds.width < 200f, "column badges should remain compact inside a Column")
        assertEquals(22f, badge.bounds.height, 0.01f, "shadcn text-xs badge uses a 16px line box")
        assertTrue(
            badge.bounds.height >= label.bounds.height,
            "column badge surface must contain its caption slot",
        )
    }

    @Test
    fun rootBadgeKeepsItsCaptionInsideTheSurface() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createUiScope(UiBounds(0f, 0f, 200f, 80f)).shadcnBadge(
            id = "root-badge",
            label = "INPUTS",
            variant = ShadcnBadgeVariant.Outline,
        )

        val frame = ui.finishFrame()
        val badge = frame.semantics.first { it.id == "root-badge" }
        val label = frame.semantics.first { it.role == UiSemanticRole.Text && it.label == "INPUTS" }
        assertEquals(22f, badge.bounds.height, 0.01f, "shadcn text-xs badge uses a 16px line box")
        assertTrue(label.bounds.y >= badge.bounds.y)
        assertTrue(
            label.bounds.y + label.bounds.height <= badge.bounds.y + badge.bounds.height + 0.01f,
            "root badge caption must be painted inside its surface",
        )
    }
}
