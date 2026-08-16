// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnAccordionTest {

    @Test
    fun shadcnAccordionSingleSelection() {
        ensureShadcnTestIconsInitialized()
        val items = listOf("item-1", "item-2")
        var selectedId: String? = "item-1"

        renderShadcnComponent(width = 240f, height = 300f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
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
        }

        assertEquals("item-1", selectedId)
    }
}
