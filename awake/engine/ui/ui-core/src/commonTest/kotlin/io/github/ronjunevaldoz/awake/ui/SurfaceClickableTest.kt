// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.resolveRootSlot
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.clickable
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pilots `Modifier.clickable(enabled, onClick)` on the plain [io.github.ronjunevaldoz.awake.ui.layouts.surface]
 * widget -- `surface()` has no click semantics of its own (unlike `button`/`checkbox`, which
 * hand-roll their own `interact()` call in `ui-headless`), so it's the clearest proof that the
 * modifier-attached handler works standalone. Two-frame press-then-release sequence mirrors
 * `ui-headless`'s `CheckboxTest`/`UiTestHarness.simulateClick`, since `ui-core` has no shared
 * harness of its own yet.
 */
class SurfaceClickableTest {

    private fun UiContext.frame(input: Input, down: Boolean, x: Float, y: Float, body: () -> Unit) {
        input.setPointer(down, x, y)
        beginFrame(200f, 100f, input.updateSnapshot().toUiInputState())
        body()
        endFrame()
    }

    private fun UiContext.click(input: Input, downAt: Pair<Float, Float>, upAt: Pair<Float, Float>, body: () -> Unit) {
        frame(input, down = true, x = downAt.first, y = downAt.second, body = body)
        frame(input, down = false, x = upAt.first, y = upAt.second, body = body)
    }

    @Test
    fun clickFiresWhenPressAndReleaseAreBothInsideBounds() {
        val ui = UiContext()
        var clicks = 0
        val body = {
            ui.createAbsolute(slot = ui.resolveRootSlot(Modifier.offset(20f.dp, 20f.dp), defaultWidth = Dimension.Fixed(0.dp), defaultHeight = Dimension.Fixed(0.dp)))
                .surface(
                    id = "s",
                    modifier = Modifier.width(160f.px).height(40f.px).clickable { clicks++ }
                ) {}
            Unit
        }
        ui.click(Input(), downAt = 100f to 40f, upAt = 100f to 40f, body = body)

        assertEquals(1, clicks, "press and release both inside the surface's bounds must fire onClick exactly once")
    }

    @Test
    fun clickDoesNotFireWhenDisabled() {
        val ui = UiContext()
        var clicks = 0
        val body = {
            ui.createAbsolute(slot = ui.resolveRootSlot(Modifier.offset(20f.dp, 20f.dp), defaultWidth = Dimension.Fixed(0.dp), defaultHeight = Dimension.Fixed(0.dp)))
                .surface(
                    id = "s",
                    modifier = Modifier.width(160f.px).height(40f.px).clickable(enabled = false) { clicks++ }
                ) {}
            Unit
        }
        ui.click(Input(), downAt = 100f to 40f, upAt = 100f to 40f, body = body)

        assertEquals(0, clicks, "enabled = false must suppress onClick even on an otherwise valid press+release")
    }

    @Test
    fun clickDoesNotFireWhenReleasedOutsideBounds() {
        val ui = UiContext()
        var clicks = 0
        val body = {
            ui.createAbsolute(slot = ui.resolveRootSlot(Modifier.offset(20f.dp, 20f.dp), defaultWidth = Dimension.Fixed(0.dp), defaultHeight = Dimension.Fixed(0.dp)))
                .surface(
                    id = "s",
                    modifier = Modifier.width(160f.px).height(40f.px).clickable { clicks++ }
                ) {}
            Unit
        }
        ui.click(Input(), downAt = 100f to 40f, upAt = 5000f to 5000f, body = body)

        assertEquals(0, clicks, "releasing the pointer outside the surface's bounds must not fire onClick")
    }
}
