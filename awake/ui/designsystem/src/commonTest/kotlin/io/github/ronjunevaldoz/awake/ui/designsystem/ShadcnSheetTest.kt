// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnSheetSide
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSheet
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FRAME_WIDTH = 400f
private const val FRAME_HEIGHT = 600f

class ShadcnSheetTest {

    /** Drives enough frames past [shadcnSheet]'s own 250ms slide-in tween for it to settle at
     * rest, mirroring how a real render loop would actually reach the resting frame. */
    private fun buildSettledFrame(side: ShadcnSheetSide) = shadcnTestSession(
        width = FRAME_WIDTH,
        height = FRAME_HEIGHT,
    ) {
        ensureShadcnTestIconsInitialized()
        var output = frame {
            shadcnSheet(
                id = "sheet-1",
                expanded = true,
                onDismissRequest = {},
                side = side,
            ) { _ -> text("Sheet Content") }
        }
        repeat(29) {
            output = frame {
                shadcnSheet(
                    id = "sheet-1",
                    expanded = true,
                    onDismissRequest = {},
                    side = side,
                ) { _ -> text("Sheet Content") }
            }
        }
        output
    }

    @Test
    fun expandedLeftSheetSettlesFlushAgainstLeftEdgeAtFullHeight() {
        val output = buildSettledFrame(ShadcnSheetSide.Left)

        val panel = output.semantics.first { it.role == UiSemanticRole.Panel && it.id == "sheet-1" }.bounds
        assertEquals(0f, panel.x, 0.5f, "expanded left sheet must settle flush against x=0, got $panel")
        assertEquals(FRAME_HEIGHT, panel.height, 0.5f, "left sheet must claim the full frame height, got $panel")
    }

    @Test
    fun scrimCoversTheFullFrame() {
        val primitives = buildSettledFrame(ShadcnSheetSide.Right).primitives

        val scrim = primitives.filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull {
            it.x == 0f && it.y == 0f && it.w == FRAME_WIDTH && it.h == FRAME_HEIGHT
        }
        assertTrue(scrim != null, "expected a scrim Quad covering the full frame, found quads: ${primitives.filterIsInstance<UiDrawPrimitive.Quad>()}")
    }

    @Test
    fun closeButtonDrawsIconPathNotGlyph() {
        val primitives = buildSettledFrame(ShadcnSheetSide.Right).primitives

        assertTrue(
            primitives.any { it is UiDrawPrimitive.FilledPath },
            "expected the close button to draw a vector icon path, found: $primitives",
        )
    }
}
