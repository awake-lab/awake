// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiPopupCompositionsTest {

    @Test
    fun tooltipTextUsesPopupPanelAboveAnchor() {
        val ui = UiContext()
        Input.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(240f, 160f)

        var result: UiPopupResult? = null
        ui.ui(x = 0f, y = 0f, width = 220f, font = BitmapFont()) {
            result = tooltipText(
                anchorSlot = UiSlot(48f, 24f, 96f, 28f),
                visible = true,
                text = "Helpful hint"
            )
        }

        val popupSlot = assertNotNull(assertNotNull(result).slot)
        val primitives = ui.endFrame()
        assertTrue(popupSlot.y >= 52f, "tooltip popup should be placed from the anchor by the popup position provider")
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.Quad })
        assertTrue(primitives.any { it is UiDrawPrimitive.Glyph })
    }

    @Test
    fun dropdownMenuReturnsPickedIndex() {
        val ui = UiContext()
        val anchor = UiSlot(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null

        Input.setPointer(down = true, x = 32f, y = 58f)
        ui.beginFrame(220f, 180f)
        ui.ui(x = 0f, y = 0f, width = 200f, font = BitmapFont()) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()
        assertEquals(null, assertNotNull(result).selectedIndex)

        Input.setPointer(down = false, x = 32f, y = 58f)
        ui.beginFrame(220f, 180f)
        ui.ui(x = 0f, y = 0f, width = 200f, font = BitmapFont()) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(UiDropdownMenuItem("Open"), UiDropdownMenuItem("Delete", destructive = true)),
                style = Style { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()

        assertEquals(0, assertNotNull(result).selectedIndex)
        assertFalse(assertNotNull(result).dismissed)
    }

    @Test
    fun dialogCentersContentAndDrawsScrim() {
        val ui = UiContext()
        Input.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(300f, 200f)

        var result: UiPopupResult? = null
        ui.ui(x = 0f, y = 0f, width = 280f, font = BitmapFont()) {
            result = dialog(
                id = "confirm",
                expanded = true,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(80f.px),
                header = {
                    text("Confirm")
                }
            ) {
                text("Delete file?")
            }
        }

        val popupSlot = assertNotNull(assertNotNull(result).slot)
        val primitives = ui.endFrame()
        val scrim = primitives.filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull {
            it.x == 0f && it.y == 0f && it.w == 300f && it.h == 200f
        }
        assertNotNull(scrim, "dialog should paint a fullscreen scrim behind the centered popup")
        assertEquals(90f, popupSlot.x)
        assertEquals(60f, popupSlot.y)
        assertFalse(assertNotNull(result).dismissed)
    }
}
