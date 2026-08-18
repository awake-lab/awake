// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropdownMenuIntrinsicWidthTest {

    /**
     * Regression test for the studio camera menu ("Perspective"/"Orthographic" truncating to
     * ellipsis). Reproduces the exact path from the studio's camera menu (CameraMenu.kt /
     * ShadcnContextMenu.kt): a zero-size point anchor (a right-click cursor position, not a real
     * control), `width = Dimension.WrapContent`, no `style` override, under [ShadcnTheme] -- the
     * wrap-content width calculation never accounted for the menu's own content padding, so every
     * item lost that much width to truncation.
     */
    @Test
    fun wrapContentMenuDoesNotTruncateItsWidestItem() {
        val semantics = renderShadcnComponent(width = 640f, height = 400f, font = BitmapFont()) {
            shadcnDropdownMenu(
                id = "camera-menu",
                anchorSlot = UiBounds(120f, 80f, 0f, 0f),
                expanded = true,
                items = listOf(
                    ShadcnDropdownMenuItem(label = "Perspective"),
                    ShadcnDropdownMenuItem(label = "Orthographic"),
                ),
                width = Dimension.WrapContent,
            )
        }.semantics
        val report = inspectTextTruncation(semantics)
        assertTrue(report.isClean, "camera menu items must not truncate: ${report.summary()}")
    }

    @Test
    fun menuItemsUseMenuRowGeometryAndSemantics() {
        val frame = renderShadcnComponent(width = 640f, height = 400f, font = BitmapFont()) {
            shadcnDropdownMenu(
                id = "actions-menu",
                anchorSlot = UiBounds(120f, 80f, 0f, 0f),
                expanded = true,
                items = listOf(
                    ShadcnDropdownMenuItem(label = "Edit"),
                    ShadcnDropdownMenuItem(label = "Delete", destructive = true),
                ),
                width = Dimension.WrapContent,
            )
        }

        val itemSemantics = frame.semantics.filter {
            it.id?.let { id -> id.startsWith("actions-menu.item.") && !id.endsWith(".label") } == true
        }
        assertEquals(2, itemSemantics.size)
        assertTrue(itemSemantics.all { it.role == UiSemanticRole.MenuItem })
        assertTrue(itemSemantics.all { it.bounds.height == 32f })
        assertTrue(itemSemantics.none { it.role == UiSemanticRole.Button })
    }
}
