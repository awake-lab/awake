// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Builds a one-off [InputSnapshot] for a test frame -- [Input] is a per-session instance
 * now (no longer a global object), so tests construct their own throwaway one instead of
 * writing into shared static state. */
private fun testSnapshot(x: Float = -100f, y: Float = -100f, down: Boolean = false, scrollDeltaY: Float = 0f): InputSnapshot {
    val input = Input()
    input.setPointer(down, x, y)
    input.scrollDeltaY = scrollDeltaY
    return input.updateSnapshot()
}

class UiPopupCompositionsTest {

    @Test
    fun tooltipTextUsesPopupPanelAboveAnchor() {
        val ui = UiContext()
        ui.beginFrame(240f, 160f, testSnapshot(x = -100f, y = -100f, down = false))

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

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = true))
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

        ui.beginFrame(220f, 180f, testSnapshot(x = 32f, y = 58f, down = false))
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
    fun dropdownMenuSupportsSeparatorsAndDisabledItems() {
        val ui = UiContext()
        val anchor = UiSlot(20f, 16f, 120f, 28f)
        var result: UiDropdownMenuResult? = null

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = true))
        ui.ui(x = 0f, y = 0f, width = 220f, font = BitmapFont()) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del")
                ),
                style = Style { contentPadding(0f.dp) }
            )
        }
        ui.endFrame()

        ui.beginFrame(240f, 220f, testSnapshot(x = 32f, y = 92f, down = false))
        ui.ui(x = 0f, y = 0f, width = 220f, font = BitmapFont()) {
            result = dropdownMenu(
                id = "menu",
                anchorSlot = anchor,
                expanded = true,
                items = listOf(
                    UiDropdownMenuItem("Pinned", enabled = false),
                    UiDropdownMenuSeparator,
                    UiDropdownMenuItem("Delete", destructive = true, trailingLabel = "Del")
                ),
                style = Style { contentPadding(0f.dp) }
            )
        }

        val primitives = ui.endFrame()
        assertEquals(1, assertNotNull(result).selectedIndex, "separators should not consume the selectable index space")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Quad>().isNotEmpty(), "separator should emit a line quad")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size >= 3, "menu entry metadata should still render text glyphs")
    }

    @Test
    fun dialogCentersContentAndDrawsScrim() {
        val ui = UiContext()
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

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

    @Test
    fun alertDialogReturnsConfirmAction() {
        val ui = UiContext()
        var result: UiAlertDialogResult? = null

        ui.beginFrame(320f, 220f, testSnapshot(x = 208f, y = 117f, down = true))
        ui.ui(x = 0f, y = 0f, width = 300f, font = BitmapFont()) {
            result = alertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?"
            )
        }
        ui.endFrame()

        ui.beginFrame(320f, 220f, testSnapshot(x = 208f, y = 117f, down = false))
        ui.ui(x = 0f, y = 0f, width = 300f, font = BitmapFont()) {
            result = alertDialog(
                id = "confirm",
                expanded = true,
                title = "Delete",
                message = "Delete this scene?"
            )
        }
        ui.endFrame()

        assertEquals(UiAlertDialogAction.Confirm, assertNotNull(result).action)
        assertFalse(assertNotNull(result).popup.dismissed)
    }

    @Test
    fun popupMeasurementDoesNotInflateWrapContentLayouts() {
        val ui = UiContext()

        val measured = ui.measureDslColumnContent(
            width = 220f,
            font = BitmapFont(),
            theme = CoreUiTheme
        ) { _ ->
            text("Popup proof")
            alertDialog(
                id = "measure-only-dialog",
                expanded = true,
                title = "Delete this scene?",
                message = "The dialog should not affect the parent column height while measuring."
            )
            text("Footer")
        }

        assertTrue(measured.height < 120f, "overlay popups should not poison wrap-content measurement")
        assertNotEquals(0f, measured.height, "normal inline content should still contribute to measurement")
    }
}
