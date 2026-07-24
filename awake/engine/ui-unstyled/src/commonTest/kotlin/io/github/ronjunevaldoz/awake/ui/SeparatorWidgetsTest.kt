// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import kotlin.test.Test
import kotlin.test.assertEquals

class SeparatorWidgetsTest {

    @Test
    fun separatorEmitsCenteredQuadWithinClaimedSlot() {
        val ui = UiContext()
        ui.beginFrame(160f, 80f, testSnapshot())

        val slot = ui.createColumn(x = 0f, y = 0f, width = 120f).separator(
            width = Dimension.FillMax,
            thickness = 2f.dp,
            color = Color.White
        )

        val quad = ui.endFrame().single() as UiDrawPrimitive.Quad
        assertEquals(120f, slot.width)
        assertEquals(2f, slot.height)
        assertEquals(0f, quad.x)
        assertEquals(0f, quad.y)
        assertEquals(120f, quad.w)
        assertEquals(2f, quad.h)
        assertEquals(Color.White, quad.color)
    }
}
