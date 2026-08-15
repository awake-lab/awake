// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Drives the Resizable page's real hero (the exact showcase composition: preview card surface
 * wrapping the fillMaxWidth panel group) and asserts the drag contract the desktop app breaks:
 * the card must not grow, the far panel must give up the space, the handle must track the
 * pointer 1:1. The isolated ResizablePanelGroup tests pass while this composition misbehaves.
 */
class ResizablePageDragProbeTest {

    private fun UiContext.bounds(id: String): UiBounds =
        semanticNodes().first { it.id == id }.bounds

    @Test
    fun draggingTheShowcaseHandleRedistributesWithoutGrowingTheCard() =
        dragProbe(densityScale = 1f)

    // The live desktop app runs at Retina density (UiDensity.scale = 2) while every UI test
    // defaults to 1 -- any px/dp mixing in the drag math only shows up here.
    @Test
    fun draggingTheShowcaseHandleStaysExactAtRetinaDensity() =
        dragProbe(densityScale = 2f)

    private fun dragProbe(densityScale: Float) {
        val previousScale = UiDensity.scale
        UiDensity.scale = densityScale
        try {
            runDragProbe()
        } finally {
            UiDensity.scale = previousScale
        }
    }

    private fun runDragProbe() {
        val ui = UiContext()
        val input = Input()
        val state = UiShowcaseRuntimeState()
        val frameW = 640f * UiDensity.scale
        val frameH = 450f * UiDensity.scale

        fun frame(down: Boolean, x: Float, y: Float) {
            input.setPointer(down = down, x = x, y = y)
            ui.beginFrame(frameW, frameH, input.updateSnapshot().toUiInputState())
            ui.pushFont(BitmapFont())
            ui.pushTheme(shadcnThemeValues(dark = false))
            ui.createUiScope(UiBounds(0f, 0f, frameW, frameH)).column(
                modifier = Modifier.padding(24f.dp).fillMaxWidth().fillMaxHeight(),
            ) {
                shadcnSurface(id = "probe-preview-card", modifier = Modifier) {
                    renderUiShowcasePagePreview(showcasePageById("resizable"), state)
                }
            }
            ui.endFrame()
        }

        frame(down = false, x = -100f, y = -100f)
        frame(down = false, x = -100f, y = -100f)


        val cardBefore = ui.bounds("probe-preview-card")
        val leftBefore = ui.bounds("showcase-resizable-left")
        val rightBefore = ui.bounds("showcase-resizable-right")
        val handleBefore = ui.bounds("showcase-resizable-handle")
        val handleCenterX = handleBefore.x + handleBefore.width / 2f
        val handleCenterY = handleBefore.y + handleBefore.height / 2f

        frame(down = true, x = handleCenterX, y = handleCenterY)
        frame(down = true, x = handleCenterX + 100f, y = handleCenterY)
        frame(down = true, x = handleCenterX + 100f, y = handleCenterY)
        frame(down = false, x = handleCenterX + 100f, y = handleCenterY)
        frame(down = false, x = -100f, y = -100f)

        val cardAfter = ui.bounds("probe-preview-card")
        val leftAfter = ui.bounds("showcase-resizable-left")
        val rightAfter = ui.bounds("showcase-resizable-right")
        val handleAfter = ui.bounds("showcase-resizable-handle")

        assertEquals(
            cardBefore.width,
            cardAfter.width,
            0.5f,
            "the preview card must not grow while a handle inside it is dragged",
        )
        assertEquals(
            leftBefore.width + 100f,
            leftAfter.width,
            1f,
            "left panel must grow by exactly the 100px pointer delta",
        )
        assertEquals(
            rightBefore.width - 100f,
            rightAfter.width,
            1f,
            "right panel must shrink by exactly the 100px pointer delta",
        )
        assertEquals(
            handleCenterX + 100f,
            handleAfter.x + handleAfter.width / 2f,
            2f,
            "handle center must track the pointer 1:1",
        )
        assertEquals(
            leftBefore.width + rightBefore.width,
            leftAfter.width + rightAfter.width,
            1f,
            "panel pair must conserve its total width",
        )
    }
}
