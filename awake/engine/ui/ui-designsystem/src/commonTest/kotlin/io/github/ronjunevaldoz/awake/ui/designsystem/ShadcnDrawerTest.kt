// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnDrawerTest {

    @Test
    fun shadcnDrawerRendersWhenExpanded() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(400f, 600f, UiInputState())

        var dismissed = false
        ui.column {
            shadcnDrawer(
                id = "drawer-1",
                expanded = true,
                onDismissRequest = { dismissed = true },
                header = { text("Settings Drawer") },
            ) {
                text("Drawer Content")
            }
        }

        assertFalse(dismissed)
    }

    // Nothing else in a drawer's own chrome (rounded panel, drag handle, text) emits a
    // FilledPath -- only an icon glyph does. A text-glyph close button (missing from the font
    // atlas) draws nothing; this catches that regression instead of just "something painted".
    @Test
    fun shadcnDrawerCloseButtonDrawsIconPathNotGlyph() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(400f, 600f, UiInputState())

        ui.column {
            shadcnDrawer(
                id = "drawer-close",
                expanded = true,
                onDismissRequest = {},
            ) {
                text("Drawer Content")
            }
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.any { it is UiDrawPrimitive.FilledPath },
            "expected the close button to draw a vector icon path, found: $primitives",
        )
    }
}
