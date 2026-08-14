// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toggleGroup
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToggleGroupWidgetTest {

    @Test
    fun toggleGroupGivesEveryOptionAnEqualShareOfTheRowWidthNotJustTheFirst() {
        // Task #40: toggleGroup()'s per-option toggle() relied on FillMax (via
        // withSizeFallback) inside a row() with no per-item space division -- the first
        // option's FillMax claimed the whole row, leaving zero width for every option after it.
        val ui = UiContext()
        ui.beginFrame(300f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 300f)

        root.toggleGroup(
            id = "group",
            options = listOf("One", "Two", "Three"),
            selectedIndex = 0,
            modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(40f.px)),
        )

        val toggleBounds = ui.finishFrame().semantics
            .filter { it.role == UiSemanticRole.Toggle }
            .sortedBy { it.id }
            .map { it.bounds.width }

        assertEquals(3, toggleBounds.size, "expected 3 recorded toggle semantics")
        val expectedWidth = 300f / 3f
        toggleBounds.forEach { width ->
            assertTrue(width > 0f, "every option's width must be positive, was $width")
            assertEquals(
                expectedWidth,
                width,
                0.5f,
                "each option must get an equal 1/3 share of the group width",
            )
        }
    }
}
