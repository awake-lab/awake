// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiFrameOutputTest {

    @Test
    fun finishFrameCachesOutputForCompatibilityApis() {
        val ui = UiContext()

        ui.beginFrame(200f, 100f, testSnapshot())
        ui.requestFocus("field")
        ui.createAbsolute(modifier = Modifier.offset(10f.dp, 10f.dp)).recordSemantic(
            role = UiSemanticRole.Text,
            bounds = UiSlot(10f, 10f, 80f, 20f),
            id = "field.label",
            label = "Name"
        )

        val frame = ui.finishFrame()

        assertTrue(frame.ownership.isTextInputFocused)
        assertEquals(frame.primitives, ui.endFrame(), "compatibility endFrame() should reuse the finalized frame output")
        assertEquals(frame.semantics, ui.semanticNodes(), "semanticNodes() should reflect the finalized frame output")
        assertEquals(
            frame.ownership.isTextInputFocused,
            ui.inputResult().isTextInputFocused,
            "inputResult() should reflect the finalized frame ownership after finishFrame()"
        )
    }
}
