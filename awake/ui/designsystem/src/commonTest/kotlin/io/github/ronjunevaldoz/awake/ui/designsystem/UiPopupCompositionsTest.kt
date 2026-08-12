// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Popup recipes expose only the Headless popup result and entry contracts. */
class UiPopupCompositionsTest {

    private fun context(): UiContext = UiContext().also {
        it.pushFont(BitmapFont())
        it.pushTheme(ShadcnTheme)
    }

    @Test
    fun tooltipTextPlacesContentBelowAnchor() {
        val ui = context()
        ui.beginFrame(240f, 160f, UiInputState())
        val result = ui.headlessRoot().shadcnTooltipText(
            anchorSlot = UiBounds(48f, 24f, 96f, 28f),
            visible = true,
            text = "Helpful hint",
        )
        val slot = assertNotNull(result.slot)
        assertTrue(slot.y >= 52f)
        ui.finishFrame()
    }

    @Test
    fun dropdownMenuRendersSelectableEntriesAndSeparators() {
        val ui = context()
        ui.beginFrame(320f, 240f, UiInputState())
        val result = ui.headlessRoot().shadcnDropdownMenu(
            id = "menu",
            anchorSlot = UiBounds(20f, 16f, 120f, 28f),
            expanded = true,
            items = listOf(
                ShadcnDropdownMenuItem("Open"),
                ShadcnDropdownMenuSeparator,
                ShadcnDropdownMenuItem("Delete", destructive = true),
            ),
            width = Dimension.Fixed(180f.dp),
        )
        assertTrue(result.slot != null)
        val frame = ui.finishFrame()
        assertTrue(frame.semantics.any { it.id == "menu.item.0" && it.role == UiSemanticRole.MenuItem })
        assertTrue(frame.semantics.any { it.id == "menu.item.1" && it.role == UiSemanticRole.MenuItem })
    }

    @Test
    fun dialogAndAlertDialogUseHeadlessPopupResults() {
        val ui = context()
        ui.beginFrame(400f, 300f, UiInputState())
        val dialog = ui.headlessRoot().shadcnDialog(
            id = "dialog",
            expanded = true,
            width = Dimension.Fixed(220f.dp),
        ) { text("Dialog content") }
        val alert = ui.headlessRoot().shadcnAlertDialog(
            id = "alert",
            expanded = true,
            title = "Delete item",
            message = "This cannot be undone.",
        )
        assertPopupVisible(dialog)
        assertPopupVisible(alert.popup)
        assertTrue(ui.finishFrame().primitives.isNotEmpty())
    }

    private fun assertPopupVisible(result: UiPopupResult) {
        assertNotNull(result.slot)
    }
}
