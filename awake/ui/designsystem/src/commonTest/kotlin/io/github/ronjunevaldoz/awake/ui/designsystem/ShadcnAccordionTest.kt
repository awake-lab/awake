// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnAccordionTest {

    @Test
    fun shadcnAccordionSingleSelection() {
        ensureShadcnTestIconsInitialized()
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(240f, 300f, UiInputState())

        val items = listOf("item-1", "item-2")
        var selectedId: String? = "item-1"

        ui.createUiScope(UiBounds(0f, 0f, 240f, 300f)).column(modifier = Modifier.fillMaxSize()) {
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
