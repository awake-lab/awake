// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proof of the two gaps closed against real shadcn-compose: [shadcnToggle]'s content-slot
 * overload (icon-only toggle, no forced String label) and [shadcnToggleGroup]'s multi-select
 * form (two toggles active simultaneously -- impossible with the old `selectedIndex: Int`).
 */
class ShadcnToggleSlotAndGroupTest {

    @Test
    fun shadcnToggleSlotRendersIconOnlyContentWithNoTextLabel() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.beginFrame(200f, 80f, testSnapshot())

        ui.createUiScope(UiBounds(0f, 0f, 200f, 80f)).shadcnTheme {
            shadcnToggle(
                id = "bold",
                checked = true,
                modifier = Modifier.width(40f.dp).height(40f.dp),
                label = null,
            )
        }

        val primitives = ui.finishFrame().primitives
        assertTrue(primitives.isNotEmpty(), "icon-only toggle should still paint its surface")
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isEmpty(),
            "no text label was supplied via the content slot, so no glyphs should render",
        )
    }

    @Test
    fun shadcnToggleGroupMultiSelectKeepsBoldAndItalicBothActive() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())

        var selected = setOf(0)

        // Click index 1 ("Italic") while index 0 ("Bold") is already selected -- the exact case
        // the audit found impossible with the old selectedIndex: Int single-select API.
        ui.beginFrame(200f, 80f, testSnapshot(x = 90f, y = 20f, down = true))
        ui.createUiScope(UiBounds(0f, 0f, 200f, 80f)).shadcnTheme {
            shadcnToggleGroup(
                id = "format",
                options = listOf("Bold", "Italic"),
                selectedIndices = selected,
                modifier = Modifier.width(160f.dp).height(40f.dp),
                onSelectedIndicesChange = { selected = it },
            )
        }
        ui.finishFrame()

        ui.beginFrame(200f, 80f, testSnapshot(x = 90f, y = 20f, down = false))
        ui.createUiScope(UiBounds(0f, 0f, 200f, 80f)).shadcnTheme {
            shadcnToggleGroup(
                id = "format",
                options = listOf("Bold", "Italic"),
                selectedIndices = selected,
                modifier = Modifier.width(160f.dp).height(40f.dp),
                onSelectedIndicesChange = { selected = it },
            )
        }
        val semantics = ui.finishFrame().semantics.filter { it.role == UiSemanticRole.Toggle }

        assertEquals(setOf(0, 1), selected, "bold and italic must both end up selected, not just one index")
        assertTrue(semantics.count { it.selected == true } == 2, "both toggles must report selected=true in their semantics")
    }
}
