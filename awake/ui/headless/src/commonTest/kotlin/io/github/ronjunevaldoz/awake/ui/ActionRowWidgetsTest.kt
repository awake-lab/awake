// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.menubar
import io.github.ronjunevaldoz.awake.ui.headless.toolbar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionRowWidgetsTest {
    @Test
    fun menubarRecordsPanelSemanticAndLaysOutChildrenInARow() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            menubar(id = "menubar.test") {
                button(id = "file", label = "File")
                button(id = "edit", label = "Edit")
            }
        }

        val panel = frame.semantics.firstOrNull { it.id == "menubar.test" }
        assertNotNull(panel, "menubar must record a Panel semantic node under its own id")
        assertEquals(UiSemanticRole.Panel, panel.role)

        val file = frame.semantics.first { it.id == "file" }
        val edit = frame.semantics.first { it.id == "edit" }
        assertEquals(file.bounds.y, edit.bounds.y, "menubar children must lay out in a row, not stack")
    }

    @Test
    fun toolbarRecordsPanelSemanticAndLaysOutChildrenInARow() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            toolbar(id = "toolbar.test") {
                button(id = "bold", label = "B")
                button(id = "italic", label = "I")
            }
        }

        val panel = frame.semantics.firstOrNull { it.id == "toolbar.test" }
        assertNotNull(panel, "toolbar must record a Panel semantic node under its own id")
        assertEquals(UiSemanticRole.Panel, panel.role)

        val bold = frame.semantics.first { it.id == "bold" }
        val italic = frame.semantics.first { it.id == "italic" }
        assertEquals(bold.bounds.y, italic.bounds.y, "toolbar children must lay out in a row, not stack")
    }
}
