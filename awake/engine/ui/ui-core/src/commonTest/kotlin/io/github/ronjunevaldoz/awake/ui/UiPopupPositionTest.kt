// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.UiPopupSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression coverage for [UiPopupDefaults.aligned]'s flip-on-no-room + clamp-to-window
 * behavior -- the bug this guards against: a dropdown/tooltip/popover anchored near a screen
 * edge rendering partially or fully off-screen because nothing corrected for insufficient
 * space in the preferred direction or clamped the final position back on-screen.
 */
class UiPopupPositionTest {
    private val window = UiBounds(x = 0f, y = 0f, width = 400f, height = 300f)
    private val popupSize = UiPopupSize(width = 120f, height = 60f)

    private fun assertWithinWindow(bounds: UiBounds) {
        assertTrue(bounds.x >= window.x, "x=${bounds.x} left of window")
        assertTrue(bounds.y >= window.y, "y=${bounds.y} above window")
        assertTrue(bounds.x + bounds.width <= window.x + window.width, "right edge ${bounds.x + bounds.width} exceeds window")
        assertTrue(bounds.y + bounds.height <= window.y + window.height, "bottom edge ${bounds.y + bounds.height} exceeds window")
    }

    @Test
    fun dropdown_anchoredNearTopEdge_staysWithinWindow() {
        // Anchor pinned at the window's top-left corner (0,0) -- exactly the "far edge" case
        // called out as a floating-point/off-by-one risk.
        val anchor = UiBounds(x = 0f, y = 0f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.dropdown().calculatePosition(anchor, window, popupSize)
        assertWithinWindow(bounds)
    }

    @Test
    fun dropdown_anchoredAtBottomRightCorner_flipsAndClamps() {
        // Anchor pinned exactly at the window's bottom-right corner -- no room below or to the
        // right; must flip up/left and clamp fully within the window.
        val anchor = UiBounds(x = window.width - 40f, y = window.height - 20f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.dropdown().calculatePosition(anchor, window, popupSize)
        assertWithinWindow(bounds)
        // Flipped above the anchor since there's no room below.
        assertTrue(bounds.y + bounds.height <= anchor.y + 0.01f, "expected popup to flip above anchor, got y=${bounds.y}")
    }

    @Test
    fun dropdown_anchoredNearBottomEdge_flipsAbove() {
        val anchor = UiBounds(x = 150f, y = window.height - 25f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.dropdown().calculatePosition(anchor, window, popupSize)
        assertWithinWindow(bounds)
        assertTrue(bounds.y + bounds.height <= anchor.y + 0.01f, "expected flip above; y=${bounds.y}")
    }

    @Test
    fun dropdown_anchoredNearLeftEdge_staysWithinWindow() {
        val anchor = UiBounds(x = 0f, y = 100f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.dropdown().calculatePosition(anchor, window, popupSize)
        assertWithinWindow(bounds)
    }

    @Test
    fun dropdown_plentyOfRoomBelow_doesNotFlipUnnecessarily() {
        // Anchor comfortably in the middle of the window -- the preferred "below" placement has
        // plenty of room, so the popup must NOT flip above the anchor.
        val anchor = UiBounds(x = 150f, y = 100f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.dropdown().calculatePosition(anchor, window, popupSize)
        assertWithinWindow(bounds)
        assertEquals(anchor.y + anchor.height, bounds.y, "expected no flip: popup should sit directly below anchor")
    }

    @Test
    fun popover_widerThanWindow_clampsWithoutCrashing() {
        // Popup bigger than the whole viewport must not throw (coerceIn(min, max) with
        // max < min) and must still clamp to the window's near edge instead of overshooting.
        val hugeSize = UiPopupSize(width = window.width + 200f, height = window.height + 200f)
        val anchor = UiBounds(x = 0f, y = 0f, width = 40f, height = 20f)
        val bounds = UiPopupDefaults.popover().calculatePosition(anchor, window, hugeSize)
        assertEquals(0f, bounds.x)
        assertEquals(0f, bounds.y)
    }
}
