// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnDrawerTest {

    @Test
    fun shadcnDrawerRendersWhenExpanded() {
        ensureShadcnTestIconsInitialized()
        var dismissed = false
        renderShadcnComponent(width = 400f, height = 600f) {
            shadcnDrawer(
                id = "drawer-1",
                expanded = true,
                onDismissRequest = { dismissed = true },
            ) { text("Settings Drawer\nDrawer Content") }
        }

        assertFalse(dismissed)
    }

    // Nothing else in a drawer's own chrome (rounded panel, drag handle, text) emits a
    // FilledPath -- only an icon glyph does. A text-glyph close button (missing from the font
    // atlas) draws nothing; this catches that regression instead of just "something painted".
    @Test
    fun shadcnDrawerCloseButtonDrawsIconPathNotGlyph() {
        ensureShadcnTestIconsInitialized()
        val frame = renderShadcnComponent(width = 400f, height = 600f) {
            shadcnDrawer(
                id = "drawer-close",
                expanded = true,
                onDismissRequest = {},
            ) { text("Drawer Content") }
        }

        val primitives = frame.primitives
        assertTrue(
            primitives.any { it is UiDrawPrimitive.FilledPath },
            "expected the close button to draw a vector icon path, found: $primitives",
        )
    }
}
