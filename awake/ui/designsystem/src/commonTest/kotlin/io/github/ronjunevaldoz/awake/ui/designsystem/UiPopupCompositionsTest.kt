// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.headless.UiAlertDialogResult
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuResult
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Popup recipes expose only the Headless popup result and entry contracts. */
class UiPopupCompositionsTest {

    @Test
    fun tooltipTextPlacesContentBelowAnchor() {
        lateinit var result: UiPopupResult
        renderShadcnComponent(width = 240f, height = 160f, input = UiInputState()) { _ ->
            result = shadcnTooltipText(
                anchorSlot = UiBounds(48f, 24f, 96f, 28f),
                visible = true,
                text = "Helpful hint",
            )
        }
        val slot = assertNotNull(result.slot)
        assertTrue(slot.y >= 52f)
    }

    @Test
    fun dropdownMenuRendersSelectableEntriesAndSeparators() {
        lateinit var result: UiMenuResult
        val frame = renderShadcnComponent(width = 320f, height = 240f, input = UiInputState()) { _ ->
            result = shadcnDropdownMenu(
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
        }
        assertTrue(result.slot != null)
        assertTrue(frame.semantics.any { it.id == "menu.item.0" && it.role == UiSemanticRole.MenuItem })
        assertTrue(frame.semantics.any { it.id == "menu.item.1" && it.role == UiSemanticRole.MenuItem })
    }

    @Test
    fun dialogAndAlertDialogUseHeadlessPopupResults() {
        lateinit var dialog: UiPopupResult
        lateinit var alert: UiAlertDialogResult
        val frame = renderShadcnComponent(width = 400f, height = 300f, input = UiInputState()) { _ ->
            dialog = shadcnDialog(
                id = "dialog",
                expanded = true,
                width = Dimension.Fixed(220f.dp),
            ) { text("Dialog content") }
            alert = shadcnAlertDialog(
                id = "alert",
                expanded = true,
                title = "Delete item",
                message = "This cannot be undone.",
            )
        }
        assertPopupVisible(dialog)
        assertPopupVisible(alert.popup)
        assertTrue(frame.primitives.isNotEmpty())
    }

    private fun assertPopupVisible(result: UiPopupResult) {
        assertNotNull(result.slot)
    }
}
