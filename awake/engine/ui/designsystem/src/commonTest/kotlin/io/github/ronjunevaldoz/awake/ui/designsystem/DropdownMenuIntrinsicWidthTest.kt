// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.column
import kotlin.test.Test
import kotlin.test.assertTrue

class DropdownMenuIntrinsicWidthTest {

    /**
     * Regression test for the studio camera menu ("Perspective"/"Orthographic" truncating to
     * ellipsis). Reproduces the exact path from the studio's camera menu (CameraMenu.kt /
     * ShadcnContextMenu.kt): a zero-size point anchor (a right-click cursor position, not a real
     * control), `width = Dimension.WrapContent`, no `style` override, under [ShadcnTheme] --
     * whose `theme.components.surface` default carries a Card-sized `contentPadding`
     * ([intrinsicMenuWidthPx][io.github.ronjunevaldoz.awake.ui.designsystem.components.popup]
     * never accounted for it, so every item lost that much width to truncation).
     */
    @Test
    fun wrapContentMenuDoesNotTruncateItsWidestItem() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(640f, 400f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.column {
            shadcnDropdownMenu(
                id = "camera-menu",
                anchorSlot = UiBounds(120f, 80f, 0f, 0f),
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem(label = "Perspective"),
                    UiDropdownMenuItem(label = "Orthographic"),
                ),
                width = Dimension.WrapContent,
            )
        }

        val semantics = ui.finishFrame().semantics
        val report = inspectTextTruncation(semantics)
        assertTrue(report.isClean, "camera menu items must not truncate: ${report.summary()}")
    }
}
