// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnContextMenuTest {

    @Test
    fun shadcnContextMenuTriggersOnSecondaryPointer() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())

        // Right click at (50, 20) inside target bounds (0, 0, 100, 40)
        ui.beginFrame(
            240f,
            160f,
            UiInputState(pointerX = 50f, pointerY = 20f, secondaryPointerDown = true),
        )

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

    /**
     * The menu used to paint edge to edge. It asks popup() for [Dimension.WrapContent], but
     * popup() resolves that by measuring content in a full-window-wide box, and each menu item
     * sizes to the width it is handed -- so the measure handed the window width straight back.
     * shadcnDropdownMenu now supplies an intrinsic width instead.
     */
    @Test
    fun shadcnContextMenuSizesTheMenuToItsContentNotTheWindow() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val frameWidth = 640f
        ui.beginFrame(
            frameWidth,
            400f,
            UiInputState(pointerX = 50f, pointerY = 20f, secondaryPointerDown = false),
        )

        ui.column {
            shadcnContextMenu(
                id = "ctx-width",
                expanded = true,
                onExpandedChange = {},
                items = listOf(
                    UiDropdownMenuItem(label = "Copy"),
                    UiDropdownMenuItem(label = "Paste"),
                ),
            ) {
                text("Right click me", modifier = Modifier.width(100f.dp).height(40f.dp))
            }
        }

        val menu = ui.endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .maxByOrNull { it.w * it.h }
        assertNotNull(menu, "expected the menu surface to be drawn")
        assertTrue(
            menu.w < frameWidth / 2f,
            "menu should hug its items, but spans ${menu.w} of a ${frameWidth}px frame",
        )
    }
}
