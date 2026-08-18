// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnContextMenuTest {

    @Test
    fun shadcnContextMenuTriggersOnSecondaryPointer() {
        // Right click at (50, 20) inside target bounds (0, 0, 100, 40)
        var open = false
        renderShadcnComponent(
            width = 240f,
            height = 160f,
            input = UiInputState(pointerX = 50f, pointerY = 20f, secondaryPointerDown = true),
        ) {
            shadcnContextMenu(
                id = "ctx-1",
                expanded = open,
                onExpandedChange = { open = it },
                items = listOf(ShadcnMenuItem(label = "Copy")),
                target = { text("Right click me", modifier = Modifier.width(100f.dp).height(40f.dp)) },
            )
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
        val frameWidth = 640f
        val frame = renderShadcnComponent(
            width = frameWidth,
            height = 400f,
            input = UiInputState(pointerX = 50f, pointerY = 20f, secondaryPointerDown = false),
        ) {
            shadcnContextMenu(
                id = "ctx-width",
                expanded = true,
                onExpandedChange = {},
                items = listOf(ShadcnMenuItem(label = "Copy"), ShadcnMenuItem(label = "Paste")),
                target = { text("Right click me", modifier = Modifier.width(100f.dp).height(40f.dp)) },
            )
        }

        val menu = frame.primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .maxByOrNull { it.w * it.h }
        assertNotNull(menu, "expected the menu surface to be drawn")
        assertTrue(
            menu.w < frameWidth / 2f,
            "menu should hug its items, but spans ${menu.w} of a ${frameWidth}px frame",
        )
    }
}
