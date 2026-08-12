// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizableHandle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.UiResizableDirection
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Fidelity contract for [shadcnResizableHandle]: the headless `handle` only computes geometry
 * and drag state, so the visible line and grip pill must come entirely from this shadcn layer. */
class ShadcnResizableFidelityTest {

    private fun UiContext.drawGroup(withHandle: Boolean) {
        pushTheme(ShadcnTheme)
        beginFrame(400f, 200f, testSnapshot())
        createUiScope(UiBounds(0f, 0f, 400f, 200f)).shadcnResizablePanelGroup(
            id = "fidelity-group",
            direction = UiResizableDirection.Horizontal,
            modifier = Modifier.width(300f.dp).height(100f.dp),
        ) {
            shadcnResizablePanel(id = "left", defaultSize = 0.5f) { }
            shadcnResizableHandle(id = "handle", withHandle = withHandle)
            shadcnResizablePanel(id = "right", defaultSize = 0.5f) { }
        }
    }

    private fun gripNear(primitives: List<UiDrawPrimitive>, centerX: Float, centerY: Float): UiDrawPrimitive.RoundedQuad? =
        primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().firstOrNull { primitive ->
            val cx = primitive.x + primitive.w / 2f
            val cy = primitive.y + primitive.h / 2f
            abs(cx - centerX) < 20f && abs(cy - centerY) < 20f
        }

    @Test
    fun withHandleDrawsAGripPillCenteredOnTheHandle() = runTest {
        val ui = UiContext()
        ui.drawGroup(withHandle = true)
        val output = ui.finishFrame()

        val grip = gripNear(output.primitives, centerX = 150f, centerY = 50f)
        assertTrue(grip != null, "withHandle = true must draw a rounded grip pill centered on the handle")
    }

    @Test
    fun withoutHandleDrawsNoGripPill() = runTest {
        val ui = UiContext()
        ui.drawGroup(withHandle = false)
        val output = ui.finishFrame()

        val grip = gripNear(output.primitives, centerX = 150f, centerY = 50f)
        assertNull(grip, "withHandle = false (the default) must not draw a grip pill")
    }
}
