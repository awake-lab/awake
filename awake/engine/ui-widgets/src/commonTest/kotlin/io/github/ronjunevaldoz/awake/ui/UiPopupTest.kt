// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiPopupTest {

    @Test
    fun popupPositionsBelowAnchorByDefault() {
        val ui = UiContext()
        Input.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(300f, 200f)
        val scope = ui.absolute(0f, 0f)

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
        Input.setPointer(down = true, x = 280f, y = 180f)
        ui.beginFrame(300f, 200f)
        val scope = ui.absolute(0f, 0f)

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
        ui.column(20f, 20f, 160f).widgetState("dd").set("expanded", true)

        Input.setPointer(down = true, x = 30f, y = 60f)
        ui.beginFrame(240f, 200f)
        var picked = ui.column(20f, 20f, 160f).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = UiModifier().width(160f.px).height(32f.px))
        ui.endFrame()
        assertEquals(null, picked)

        Input.setPointer(down = false, x = 30f, y = 60f)
        ui.beginFrame(240f, 200f)
        picked = ui.column(20f, 20f, 160f).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = UiModifier().width(160f.px).height(32f.px))
        ui.endFrame()

        assertEquals(0, picked)
        assertFalse(ui.column(20f, 20f, 160f).widgetState("dd").get("expanded", true))
    }
}
