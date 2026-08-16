// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals

class SeparatorWidgetsTest {
    @Test
    fun horizontalSeparatorSpansFullWidth() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            separator(thickness = 1f.dp)
        }

        val quad = frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>().first()
        assertEquals(0f, quad.x)
        assertEquals(0f, quad.y)
        assertEquals(200f, quad.w)
        assertEquals(1f, quad.h)
    }

    @Test
    fun verticalSeparatorSpansFullHeightWithCustomColor() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            separator(
                thickness = 2f.dp,
                orientation = UiSeparatorOrientation.Vertical,
                style = Style { background(Color(1f, 0f, 0f, 1f)); shape(UiShape.none) },
            )
        }

        val quad = frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>().first()
        assertEquals(0f, quad.x)
        assertEquals(0f, quad.y)
        assertEquals(2f, quad.w)
        assertEquals(100f, quad.h)
        assertEquals(Color(1f, 0f, 0f, 1f), quad.color)
    }
}
