// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Drives the Resizable page's real hero (the exact showcase composition: preview card surface
 * wrapping the fillMaxWidth panel group) and asserts the drag contract the desktop app broke:
 * the card must not grow, the far panel must give up the space, the handle must track the
 * pointer 1:1. The isolated ResizablePanelGroup tests passed while this composition misbehaved.
 */
class ResizablePageDragProbeTest {

    @Test
    fun draggingTheShowcaseHandleRedistributesWithoutGrowingTheCard() = dragProbe(density = 1f)

    // The live desktop app runs at Retina density while unit tests default to 1 -- any px/dp
    // mixing in the drag math only shows up here.
    @Test
    fun draggingTheShowcaseHandleStaysExactAtRetinaDensity() = dragProbe(density = 2f)

    private fun dragProbe(density: Float) = uiTestSession(
        width = 640f * density,
        height = 450f * density,
        theme = shadcnThemeValues(dark = false),
        density = density,
    ) {
        val state = UiShowcaseRuntimeState()
        fun pageFrame(x: Float, y: Float, down: Boolean) = frame(x = x, y = y, down = down) {
            column(modifier = Modifier.padding(24f.dp).fillMaxWidth().fillMaxHeight()) {
                shadcnSurface(id = "probe-preview-card", modifier = Modifier) {
                    renderUiShowcasePagePreview(showcasePageById("resizable"), state)
                }
            }
        }

        pageFrame(-100f, -100f, down = false)
        val settled = pageFrame(-100f, -100f, down = false)

        val cardBefore = settled.bounds("probe-preview-card")
        val leftBefore = settled.bounds("showcase-resizable-left")
        val rightBefore = settled.bounds("showcase-resizable-right")
        val handle = settled.bounds("showcase-resizable-handle")
        val hx = handle.x + handle.width / 2f
        val hy = handle.y + handle.height / 2f

        pageFrame(hx, hy, down = true)
        pageFrame(hx + 100f, hy, down = true)
        pageFrame(hx + 100f, hy, down = false)
        val after = pageFrame(-100f, -100f, down = false)

        assertEquals(
            cardBefore.width,
            after.bounds("probe-preview-card").width,
            0.5f,
            "the preview card must not grow while a handle inside it is dragged",
        )
        assertEquals(
            leftBefore.width + 100f,
            after.bounds("showcase-resizable-left").width,
            1f,
            "left panel must grow by exactly the 100px pointer delta",
        )
        assertEquals(
            rightBefore.width - 100f,
            after.bounds("showcase-resizable-right").width,
            1f,
            "right panel must shrink by exactly the 100px pointer delta",
        )
        assertEquals(
            hx + 100f,
            after.bounds("showcase-resizable-handle").let { it.x + it.width / 2f },
            2f,
            "handle center must track the pointer 1:1",
        )
        assertEquals(
            leftBefore.width + rightBefore.width,
            after.bounds("showcase-resizable-left").width +
                after.bounds("showcase-resizable-right").width,
            1f,
            "panel pair must conserve its total width",
        )
    }
}
