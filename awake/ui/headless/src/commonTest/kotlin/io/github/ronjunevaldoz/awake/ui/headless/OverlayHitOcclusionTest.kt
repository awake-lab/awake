// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.simulateClick
import io.github.ronjunevaldoz.awake.ui.simulateFrame
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverlayHitOcclusionTest {

    @Test
    fun popupBlocksHitTestForBaseLayerElementsUnderneath() {
        val ui = UiContext()
        var backgroundClicked = false
        var popupClicked = false
        var popupExpanded = true

        fun render() {
            val scope = ui.createUiScope(UiBounds(0f, 0f, 200f, 200f))
            // Base layer button underneath the popup
            backgroundClicked = scope.button(
                id = "bg-button",
                label = "Background",
                modifier = Modifier.width(120f.dp).height(60f.dp),
            )

            // Overlay popup sitting directly on top of the background button
            scope.popup(
                id = "test-popup",
                anchorSlot = UiBounds(0f, 0f, 120f, 60f),
                expanded = popupExpanded,
                positionProvider = UiPopupDefaults.aligned(UiAlignment.TopStart, UiAlignment.TopStart),
                width = Dimension.Fixed(120f.dp),
                height = Dimension.Fixed(60f.dp),
            ) {
                popupClicked = button(
                    id = "popup-button",
                    label = "Popup Action",
                    modifier = Modifier.width(120f.dp).height(60f.dp),
                )
            }
        }

        // Frame 1: Render initial open state so occlusion zone is registered
        ui.simulateFrame(pointerDown = false, x = 30f, y = 30f, screenHeight = 200f) {
            render()
        }

        // Frame 2: Click on the popup (which is directly over the background button)
        ui.simulateClick(x = 30f, y = 30f, screenHeight = 200f) {
            render()
        }

        assertFalse(backgroundClicked, "Background button should NOT be clicked when covered by an active popup")
        assertTrue(popupClicked, "Button inside the popup SHOULD receive the click")
    }

    @Test
    fun modalScrimBlocksClicksToBackgroundElements() {
        val ui = UiContext()
        var backgroundClicked = false

        fun render() {
            val scope = ui.createUiScope(UiBounds(0f, 0f, 200f, 200f))
            backgroundClicked = scope.button(
                id = "bg-button-modal",
                label = "Background",
                modifier = Modifier.width(120f.dp).height(60f.dp),
            )

            scope.dialog(
                id = "test-dialog",
                expanded = true,
                width = Dimension.Fixed(80f.dp),
                height = Dimension.Fixed(40f.dp),
                properties = DialogProperties(showScrim = true, scrimColor = Color.Black),
            ) { }
        }

        // Frame 1: Render open dialog with scrim
        ui.simulateFrame(pointerDown = false, x = 30f, y = 30f, screenHeight = 200f) {
            render()
        }

        // Frame 2: Click on background button location
        ui.simulateClick(x = 30f, y = 30f, screenHeight = 200f) {
            render()
        }

        assertFalse(backgroundClicked, "Background button should NOT be clicked when a modal scrim is active")
    }
}
