// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

private const val FRAME_WIDTH = 320f
private const val FRAME_HEIGHT = 400f

class ShadcnEmptyTest {

    @Test
    fun titleDescriptionAndActionStackCentered() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(FRAME_WIDTH, FRAME_HEIGHT, UiInputState())
        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnEmpty(
                id = "empty-1",
                title = "No results",
                description = "Try adjusting your filters.",
                action = { shadcnButton(id = "empty-1.action", label = "Reset filters", onClick = {}) },
            )
        }
        val output = ui.finishFrame()

        val report = inspectUiFrame(output.primitives, ui.frameBounds(), font = ui.currentFont)
        assertTrue(report.isClean, report.summary())

        val titleNode = output.semantics.first { it.role == UiSemanticRole.Text && it.label == "No results" }
        val descriptionNode = output.semantics.first {
            it.role == UiSemanticRole.Text && it.label == "Try adjusting your filters."
        }
        val actionBounds = output.semantics
            .first { it.role == UiSemanticRole.Button && it.id == "empty-1.action" }
            .bounds

        // Centered: each text's own ink (contentBounds) sits centered within the frame width,
        // not flush against the left edge.
        val frameCenter = FRAME_WIDTH / 2f
        val titleInk = titleNode.contentBounds ?: titleNode.bounds
        val descriptionInk = descriptionNode.contentBounds ?: descriptionNode.bounds
        assertTrue(
            abs((titleInk.x + titleInk.width / 2f) - frameCenter) < 5f,
            "expected title centered around x=$frameCenter, got $titleInk",
        )
        assertTrue(
            abs((descriptionInk.x + descriptionInk.width / 2f) - frameCenter) < 5f,
            "expected description centered around x=$frameCenter, got $descriptionInk",
        )

        // Stack order: title above description above the action.
        assertTrue(
            titleNode.bounds.y < descriptionNode.bounds.y,
            "expected title above description, title=${titleNode.bounds} description=${descriptionNode.bounds}",
        )
        assertTrue(
            descriptionNode.bounds.y + descriptionNode.bounds.height <= actionBounds.y + 1f,
            "expected action below description, description=${descriptionNode.bounds} action=$actionBounds",
        )
    }

    @Test
    fun titleOnlyRendersCleanWithoutOptionalSlots() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(FRAME_WIDTH, FRAME_HEIGHT, UiInputState())
        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnEmpty(id = "empty-2", title = "Nothing here")
        }
        val output = ui.finishFrame()

        val report = inspectUiFrame(output.primitives, ui.frameBounds(), font = ui.currentFont)
        assertTrue(report.isClean, report.summary())
    }
}
