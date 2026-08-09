// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScrollPanelCompositionTest {

    @Test
    fun dslScrollPanelDelegatesToSharedWidgetPrimitive() {
        val ui = UiContext()
        val scrollState = UiScrollState()

        ui.beginFrame(220f, 200f, testSnapshot(x = 24f, y = 24f, scrollDeltaY = -1f))
        ui.column(modifier = Modifier.offset(12f.dp, 12f.dp).width(160f.dp).height(176f.dp)) {
            scrollPanel(
                id = "dsl-scroll",
                modifier = Modifier
                    .width(Dimension.FillMax)
                    .height(80f.toDimension())
                    .verticalScroll(
                        state = scrollState,
                        config = UiScrollConfig(scrollSpeed = 20f),
                    ),
            ) {
                repeat(8) { index ->
                    text("Row $index")
                }
            }
        }
        ui.endFrame()

        assertEquals(20f, scrollState.offsetY)
        assertTrue(scrollState.canScroll)
    }
}
