// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.separator
import kotlin.test.Test
import kotlin.test.assertEquals

class SeparatorWidgetsTest {
    @Test
    fun horizontalSeparatorSpansFullWidth() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).separator(thickness = 1f.dp)
        val frame = ui.finishFrame()

        val quad = frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>().first()
        assertEquals(0f, quad.x)
        assertEquals(0f, quad.y)
        assertEquals(200f, quad.w)
        assertEquals(1f, quad.h)
    }

    @Test
    fun verticalSeparatorSpansFullHeightWithCustomColor() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).separator(
            thickness = 2f.dp,
            orientation = UiSeparatorOrientation.Vertical,
            color = Color(1f, 0f, 0f, 1f),
        )
        val frame = ui.finishFrame()

        val quad = frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>().first()
        assertEquals(0f, quad.x)
        assertEquals(0f, quad.y)
        assertEquals(2f, quad.w)
        assertEquals(100f, quad.h)
        assertEquals(Color(1f, 0f, 0f, 1f), quad.color)
    }
}
