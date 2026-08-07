// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleCompositionTest {

    @Test
    fun dslToggleUsesSharedWidgetBehavior() {
        val ui = UiContext()
        ui.beginFrame(180f, 80f, testSnapshot())

        var checked = true
        ui.pushFont(BitmapFont())
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(140f.dp).height(80f.dp)) {
            checked = toggle(
                id = "grid",
                checked = checked,
                label = "GRID",
                modifier = Modifier.fillMaxWidth().height(32f.px),
            )
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.any { it is UiDrawPrimitive.Quad || it is UiDrawPrimitive.RoundedQuad },
            "toggle should emit its background shape",
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "toggle should render its label through the shared glyph pipeline",
        )
        assertFalse(!checked, "toggle should remain unchanged without interaction")
    }
}
