// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*

class UiPopupTest {

    @Test
    fun popupPositionsBelowAnchorByDefault() {
        val ui = UiContext()
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

        val result = scope.popup(
            anchorSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 30f, 120f, 32f),
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
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

        val result = scope.popup(
            anchorSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 30f, 120f, 32f),
            expanded = true,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px)
        ) {
            claimSlot(120f.toDimension(), 32f.toDimension())
        }

        assertTrue(result.dismissed)
    }

    @Test
    fun popupKeepsRenderingWhileFadingOutInsteadOfSnappingAway() {
        // Regression coverage for the "bare conditional collapse" audit finding: popup() used to
        // `if (!expanded) return UiPopupResult(slot = null, ...)` instantly on the very frame
        // expanded flips false. It must now keep drawing (dimmed) through the exit fade instead.
        val ui = UiContext()
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 30f, 120f, 32f)

        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        scope.popup(
            id = "regression-popup",
            anchorSlot = anchor,
            expanded = true,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px),
            fadeDurationMs = 200f
        ) { claimSlot(120f.toDimension(), 32f.toDimension()) }
        ui.endFrame()

        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        val firstExitFrame = scope.popup(
            id = "regression-popup",
            anchorSlot = anchor,
            expanded = false,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px),
            fadeDurationMs = 200f
        ) { claimSlot(120f.toDimension(), 32f.toDimension()) }
        ui.endFrame()

        assertNotNull(firstExitFrame.slot, "the first frame after collapsing must still be rendering (fading), not already gone")
        assertFalse(firstExitFrame.dismissed, "the fade window closing itself is not a click-outside dismissal")
    }

    @Test
    fun popupReallyStopsRenderingOnceItsExitFadeSettles() {
        val ui = UiContext()
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        val anchor = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 30f, 120f, 32f)

        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        scope.popup(
            id = "settle-popup",
            anchorSlot = anchor,
            expanded = true,
            width = Dimension.Fixed(120f.px),
            height = Dimension.Fixed(64f.px),
            fadeDurationMs = 100f
        ) { claimSlot(120f.toDimension(), 32f.toDimension()) }
        ui.endFrame()

        var lastResult: UiPopupResult? = null
        repeat(20) { // 20 frames * 1/60s ~= 333ms, well past a 100ms fade.
            ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
            lastResult = scope.popup(
                id = "settle-popup",
                anchorSlot = anchor,
                expanded = false,
                width = Dimension.Fixed(120f.px),
                height = Dimension.Fixed(64f.px),
                fadeDurationMs = 100f
            ) { claimSlot(120f.toDimension(), 32f.toDimension()) }
            ui.endFrame()
        }

        assertEquals(null, lastResult?.slot, "once the exit fade has settled, the popup must really stop rendering")
    }

    @Test
    fun dropdownUsesSharedPopupAndClosesAfterPickingOption() {
        val ui = UiContext()
        ui.createColumn(modifier = Modifier.offset(20f.dp, 20f.dp).width(160f.dp)).widgetState("dd").set("expanded", true)

        ui.beginFrame(240f, 200f, testSnapshot(x = 30f, y = 60f, down = true))
        var picked = ui.createColumn(modifier = Modifier.offset(20f.dp, 20f.dp).width(160f.dp)).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = Modifier.width(160f.px).height(32f.px))
        ui.endFrame()
        assertEquals(null, picked)

        ui.beginFrame(240f, 200f, testSnapshot(x = 30f, y = 60f, down = false))
        picked = ui.createColumn(modifier = Modifier.offset(20f.dp, 20f.dp).width(160f.dp)).dropdown("dd", listOf("A", "B"), selectedIndex = 0, modifier = Modifier.width(160f.px).height(32f.px))
        ui.endFrame()

        assertEquals(0, picked)
        assertFalse(ui.createColumn(modifier = Modifier.offset(20f.dp, 20f.dp).width(160f.dp)).widgetState("dd").get("expanded", true))
    }
}
