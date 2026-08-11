// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnCombobox
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnComboboxTest {

    @Test
    fun typingAFilterPrefixShrinksTheListThenClickSelectsAndClosesPopup() {
        val ui = UiContext()
        val font = BitmapFont()
        ui.pushFont(font)
        ui.pushTheme(ShadcnTheme)
        // "Cran" is a prefix only "Cranberry" (index 3) contains -- proves the list actually
        // shrinks to matches, not just re-orders.
        val options = listOf("Apple", "Banana", "Cherry", "Cranberry")
        var picked: String? = null

        fun render() {
            ui.createAbsolute(x = 20f, y = 20f).shadcnCombobox(
                id = "fruit",
                value = picked,
                options = options,
                onValueChange = { picked = it },
                modifier = Modifier.width(200f.dp),
            )
        }

        fun frame(input: Input) {
            ui.beginFrame(400f, 400f, input.updateSnapshot().toUiInputState())
            render()
        }

        // Closed trigger: find its bounds to click it open.
        frame(Input().apply { setPointer(false, -1f, -1f) })
        val triggerBounds = assertNotNull(ui.finishFrame().semantics.firstOrNull { it.id == "fruit" }).bounds
        val triggerX = triggerBounds.x + triggerBounds.width / 2f
        val triggerY = triggerBounds.y + triggerBounds.height / 2f

        // Press + release the trigger to open the popup.
        frame(Input().apply { setPointer(true, triggerX, triggerY) })
        ui.endFrame()
        frame(Input().apply { setPointer(false, triggerX, triggerY) })
        val opened = ui.finishFrame()
        assertTrue(
            inspectUiFrame(opened.primitives, UiBounds(0f, 0f, 400f, 400f), font).isClean,
            "open combobox popup must render clean",
        )
        val openOptionIds = opened.semantics.mapNotNull { it.id }.filter { it.startsWith("fruit.option.") }
        assertEquals(4, openOptionIds.size, "every option must render before any filter is typed")

        val filterBounds = assertNotNull(opened.semantics.firstOrNull { it.id == "fruit.filter" }).bounds
        val filterX = filterBounds.x + filterBounds.width / 2f
        val filterY = filterBounds.y + filterBounds.height / 2f

        // Click into the filter field to focus it -- same click-then-type shape as
        // TextFieldWidgetTest.clickingIntoAnEmptyFieldGrantsFocusAndAcceptsTypedText.
        frame(Input().apply { setPointer(true, filterX, filterY) })
        ui.endFrame()
        frame(Input().apply { setPointer(false, filterX, filterY) })
        ui.endFrame()

        // Type the filter prefix through the same input path shadcnInput/textField uses.
        val typed = Input().apply {
            setPointer(false, filterX, filterY)
            pushTypedText("Cran")
        }
        frame(typed)
        val filteredOutput = ui.finishFrame()
        val visibleOptionIds = filteredOutput.semantics.mapNotNull { it.id }.filter { it.startsWith("fruit.option.") }
        assertEquals(
            listOf("fruit.option.3"),
            visibleOptionIds,
            "typing \"Cran\" must shrink the option list to just Cranberry",
        )

        val optionBounds = assertNotNull(filteredOutput.semantics.firstOrNull { it.id == "fruit.option.3" }).bounds
        val optionX = optionBounds.x + optionBounds.width / 2f
        val optionY = optionBounds.y + optionBounds.height / 2f

        // Click the remaining match.
        frame(Input().apply { setPointer(true, optionX, optionY) })
        ui.endFrame()
        frame(Input().apply { setPointer(false, optionX, optionY) })
        ui.endFrame()
        assertEquals("Cranberry", picked, "clicking the filtered option must fire onValueChange")

        // popupState.close() (triggered by this frame's click) only takes effect for the *next*
        // frame's read of the popup-expanded flag -- immediate-mode, same "settle frame" shape as
        // TextFieldWidgetTest's own focus-clearing assertions. Render once more to observe it.
        val afterPick = run {
            frame(Input().apply { setPointer(false, optionX, optionY) })
            ui.finishFrame()
        }
        val trigger = assertNotNull(afterPick.semantics.firstOrNull { it.id == "fruit" })
        assertFalse(trigger.selected == true, "picking an option must close the popup")
        assertTrue(
            inspectUiFrame(afterPick.primitives, UiBounds(0f, 0f, 400f, 400f), font).isClean,
            "post-selection frame must render clean",
        )
    }
}
