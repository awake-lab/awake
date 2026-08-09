// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnAccordionTest {

    @Test
    fun shadcnAccordionSingleSelection() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(240f, 300f, UiInputState())

        val items = listOf("item-1", "item-2")
        var selectedId: String? = "item-1"

        ui.column {
            shadcnAccordion(
                items = items,
                selectedId = selectedId,
                onSelectId = { selectedId = it },
                idProvider = { it },
                titleProvider = { it.replace("-", " ").capitalize() },
            ) { item ->
                text("Content for $item")
            }
        }

        assertEquals("item-1", selectedId)
    }
}
