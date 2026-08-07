// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertTrue

class ShadcnContextMenuTest {

    @Test
    fun shadcnContextMenuTriggersOnSecondaryPointer() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())

        // Right click at (50, 20) inside target bounds (0, 0, 100, 40)
        ui.beginFrame(240f, 160f, UiInputState(pointerX = 50f, pointerY = 20f, secondaryPointerDown = true))

        var open = false
        ui.column {
            shadcnContextMenu(
                id = "ctx-1",
                expanded = open,
                onExpandedChange = { open = it },
                items = listOf(UiDropdownMenuItem(label = "Copy")),
            ) {
                text("Right click me", modifier = Modifier.width(100f.dp).height(40f.dp))
            }
        }

        assertTrue(open)
    }
}
