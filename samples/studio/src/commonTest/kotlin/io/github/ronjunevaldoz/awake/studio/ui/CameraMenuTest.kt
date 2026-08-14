// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CameraMenuTest {

    private val viewportBounds = UiBounds(0f, 0f, 400f, 300f)

    @Test
    fun rightClickOverTheViewportOpensTheMenuAndPickingAnItemReportsItsIndex() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnTheme(dark = false))

        var picked: Int? = null

        fun frame(x: Float, y: Float, primaryDown: Boolean = false, secondaryDown: Boolean = false) {
            ui.beginFrame(
                800f,
                600f,
                UiInputState(pointerX = x, pointerY = y, pointerDown = primaryDown, secondaryPointerDown = secondaryDown),
            )
            ui.createUiScope(UiBounds(0f, 0f, 800f, 600f)).viewportCameraMenu(
                id = "test-viewport-menu",
                bounds = viewportBounds,
                onPick = { picked = it },
            )
        }

        // Right click inside the viewport bounds opens the menu.
        frame(50f, 40f, secondaryDown = true)
        ui.endFrame()

        // Menu now open -- find "Front" (index 1) to prove a non-first pick round-trips.
        frame(50f, 40f)
        val opened = ui.finishFrame().semantics
        val itemBounds = assertNotNull(opened.firstOrNull { it.id == "test-viewport-menu.menu.item.1" }).bounds
        val itemX = itemBounds.x + itemBounds.width / 2f
        val itemY = itemBounds.y + itemBounds.height / 2f

        // Press + release the item to click it (buttonSlot registers a click on release).
        frame(itemX, itemY, primaryDown = true)
        ui.endFrame()
        frame(itemX, itemY, primaryDown = false)
        ui.endFrame()

        assertEquals(1, picked)
    }

    @Test
    fun clickOutsideTheViewportDoesNotOpenTheMenu() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnTheme(dark = false))

        var picked: Int? = null
        ui.beginFrame(
            800f,
            600f,
            UiInputState(pointerX = 900f, pointerY = 900f, secondaryPointerDown = true),
        )
        ui.createUiScope(UiBounds(0f, 0f, 800f, 600f)).viewportCameraMenu(
            id = "test-viewport-menu-outside",
            bounds = viewportBounds,
            onPick = { picked = it },
        )
        val opened = ui.finishFrame().semantics

        assertEquals(null, picked)
        assertEquals(null, opened.firstOrNull { it.id?.startsWith("test-viewport-menu-outside.menu") == true })
    }
}
