// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiPopupTest {

    @Test
    fun popupPositionsBelowAnchorByDefault() {
        val ui = UiContext()
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        val scope = ui.createAbsolute(0f, 0f)

        val result = scope.popup(
            anchorSlot = UiSlot(20f, 30f, 120f, 32f),
            expanded = true,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px)
        ) { }

        val popupSlot = assertNotNull(result.slot)
        assertEquals(20f, popupSlot.x)
        assertEquals(62f, popupSlot.y)
        assertFalse(result.dismissed)
    }

    @Test
    fun popupDismissesOnOutsidePointerPress() {
        val ui = UiContext()
        ui.beginFrame(300f, 200f, testSnapshot(x = 280f, y = 180f, down = true))
        val scope = ui.createAbsolute(0f, 0f)

        val result = scope.popup(
            anchorSlot = UiSlot(20f, 30f, 120f, 32f),
            expanded = true,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px)
        ) {
            claimSlot(120f.toDimension(), 32f.toDimension())
        }

        assertTrue(result.dismissed)
    }

    @Test
    fun dropdownUsesSharedPopupAndClosesAfterPickingOption() {
        val ui = UiContext()
        ui.createColumn(20f, 20f, 160f).widgetState("dd").set("expanded", true)

        ui.beginFrame(240f, 200f, testSnapshot(x = 30f, y = 60f, down = true))
        var picked = ui.createColumn(20f, 20f, 160f).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = UiModifier().width(160f.px).height(32f.px))
        ui.endFrame()
        assertEquals(null, picked)

        ui.beginFrame(240f, 200f, testSnapshot(x = 30f, y = 60f, down = false))
        picked = ui.createColumn(20f, 20f, 160f).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = UiModifier().width(160f.px).height(32f.px))
        ui.endFrame()

        assertEquals(0, picked)
        assertFalse(ui.createColumn(20f, 20f, 160f).widgetState("dd").get("expanded", true))
    }
}
