// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.resolveRootSlot
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.requestCursor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiFrameOutputTest {

    @Test
    fun finishFrameCachesOutputForCompatibilityApis() {
        val ui = UiContext()

        ui.beginFrame(200f, 100f, testSnapshot())
        ui.requestFocus("field")
        ui.createAbsolute(slot = ui.resolveRootSlot(Modifier.offset(10f.dp, 10f.dp), defaultWidth = Dimension.Fixed(0.dp), defaultHeight = Dimension.Fixed(0.dp))).recordSemantic(
            role = UiSemanticRole.Text,
            bounds = UiBounds(10f, 10f, 80f, 20f),
            id = "field.label",
            label = "Name",
        )

        val frame = ui.finishFrame()

        assertTrue(frame.ownership.isTextInputFocused)
        assertEquals(frame.primitives, ui.endFrame(), "compatibility endFrame() should reuse the finalized frame output")
        assertEquals(frame.semantics, ui.semanticNodes(), "semanticNodes() should reflect the finalized frame output")
        assertEquals(
            frame.ownership.isTextInputFocused,
            ui.inputResult().isTextInputFocused,
            "inputResult() should reflect the finalized frame ownership after finishFrame()",
        )
    }

    @Test
    fun requestedCursorResetsToDefaultEachFrame() {
        val ui = UiContext()

        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createAbsolute(x = 0f, y = 0f).requestCursor(UiCursor.ResizeHorizontal)
        assertEquals(
            UiCursor.ResizeHorizontal,
            ui.finishFrame().effects.cursor,
            "a widget's requestCursor call this frame should surface on effects.cursor",
        )

        ui.beginFrame(200f, 100f, testSnapshot())
        // No widget requests a cursor this frame.
        assertEquals(
            UiCursor.Default,
            ui.finishFrame().effects.cursor,
            "a cursor request must not carry over into a frame that never re-requested it",
        )
    }
}
