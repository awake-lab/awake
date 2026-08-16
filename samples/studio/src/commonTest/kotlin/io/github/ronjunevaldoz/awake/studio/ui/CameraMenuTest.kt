// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CameraMenuTest {

    private val viewportBounds = UiBounds(0f, 0f, 400f, 300f)

    @Test
    fun rightClickOverTheViewportOpensTheMenuAndPickingAnItemReportsItsIndex() =
        uiTestSession(width = 800f, height = 600f, font = BitmapFont()) {
            var picked: Int? = null

            fun menuFrame(x: Float, y: Float, primaryDown: Boolean = false, secondaryDown: Boolean = false): UiComponentFrame {
                input.setSecondaryPointer(secondaryDown)
                return frame(x = x, y = y, down = primaryDown) {
                    shadcnTheme(theme = shadcnThemeValues(dark = false)) {
                        viewportCameraMenu(
                            id = "test-viewport-menu",
                            bounds = viewportBounds,
                            onPick = { picked = it },
                        )
                    }
                }
            }

            // Right click inside the viewport bounds opens the menu.
            menuFrame(50f, 40f, secondaryDown = true)

            // Menu now open -- find "Front" (index 1) to prove a non-first pick round-trips.
            val opened = menuFrame(50f, 40f).semantics
            val itemBounds = assertNotNull(opened.firstOrNull { it.id == "test-viewport-menu.menu.item.1" }).bounds
            val itemX = itemBounds.x + itemBounds.width / 2f
            val itemY = itemBounds.y + itemBounds.height / 2f

            // Press + release the item to click it (buttonSlot registers a click on release).
            menuFrame(itemX, itemY, primaryDown = true)
            menuFrame(itemX, itemY, primaryDown = false)

            assertEquals(1, picked)
        }

    @Test
    fun clickOutsideTheViewportDoesNotOpenTheMenu() {
        var picked: Int? = null
        val opened = renderUiComponent(
            width = 800f,
            height = 600f,
            font = BitmapFont(),
            input = UiInputState(pointerX = 900f, pointerY = 900f, secondaryPointerDown = true),
        ) {
            shadcnTheme(theme = shadcnThemeValues(dark = false)) {
                viewportCameraMenu(
                    id = "test-viewport-menu-outside",
                    bounds = viewportBounds,
                    onPick = { picked = it },
                )
            }
        }.semantics

        assertEquals(null, picked)
        assertEquals(null, opened.firstOrNull { it.id?.startsWith("test-viewport-menu-outside.menu") == true })
    }
}
