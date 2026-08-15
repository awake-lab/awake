// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.requestFocus
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnBehaviorParityTest {

    private fun createContext(): UiContext = UiContext().apply {
        pushTheme(ShadcnTheme)
    }

    @Test
    fun buttonClickTriggersActivation() {
        val ui = createContext()
        var clicked = false
        val input = Input()

        // Frame 1: Pointer down over button at (30, 30)
        input.setPointer(down = true, x = 30f, y = 30f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnButton(
            id = "btn-1",
            label = "Submit",
            modifier = Modifier.offset(10f.dp, 10f.dp).width(100f.dp).height(40f.dp),
        ).also { if (it) clicked = true }
        ui.finishFrame()

        // Frame 2: Pointer up over button at (30, 30)
        input.setPointer(down = false, x = 30f, y = 30f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnButton(
            id = "btn-1",
            label = "Submit",
            modifier = Modifier.offset(10f.dp, 10f.dp).width(100f.dp).height(40f.dp),
        ).also { if (it) clicked = true }
        ui.finishFrame()

        assertTrue(clicked, "shadcnButton should emit click activation when pressed and released")
    }

    @Test
    fun buttonSpaceKeyTriggersActivation() {
        val ui = createContext()
        var clicked = false
        val input = Input()

        // Set focus to "btn-1"
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        val scope = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f))
        scope.requestFocus("btn-1")
        scope.shadcnButton("btn-1", label = "Submit")
        ui.finishFrame()

        // Press Space key while focused
        input.setKeyDown(Key.Space, down = true)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnButton("btn-1", label = "Submit")
            .also { if (it) clicked = true }
        ui.finishFrame()

        assertTrue(clicked, "shadcnButton should activate on Space key when focused")
    }

    @Test
    fun switchTogglesStateOnClick() {
        val ui = createContext()
        var state = false
        val input = Input()

        // Press down
        input.setPointer(down = true, x = 20f, y = 20f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        state = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnSwitch(
            id = "switch-1",
            checked = state,
            modifier = Modifier.offset(10f.dp, 10f.dp).width(44f.dp).height(24f.dp),
        )
        ui.finishFrame()

        // Release pointer
        input.setPointer(down = false, x = 20f, y = 20f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        state = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnSwitch(
            id = "switch-1",
            checked = state,
            modifier = Modifier.offset(10f.dp, 10f.dp).width(44f.dp).height(24f.dp),
        )
        ui.finishFrame()

        assertTrue(state, "shadcnSwitch should toggle state to true on click")
    }

    @Test
    fun checkboxTogglesStateOnClick() {
        val ui = createContext()
        var state = false
        val input = Input()

        // Press down
        input.setPointer(down = true, x = 20f, y = 20f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        state = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnCheckbox(
            id = "check-1",
            checked = state,
            modifier = Modifier.offset(10f.dp, 10f.dp).width(16f.dp).height(16f.dp),
        )
        ui.finishFrame()

        // Release pointer
        input.setPointer(down = false, x = 20f, y = 20f)
        ui.beginFrame(UiFrameInput(200f, 100f, input.updateSnapshot().toUiInputState()))
        state = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).shadcnCheckbox(
            id = "check-1",
            checked = state,
            modifier = Modifier.offset(10f.dp, 10f.dp).width(16f.dp).height(16f.dp),
        )
        ui.finishFrame()

        assertTrue(state, "shadcnCheckbox should toggle state to true on click")
    }

    @Test
    fun dropdownMenuSelectsItemOnClick() {
        val ui = createContext()
        val input = Input()
        val triggerSlot = UiBounds(10f, 10f, 100f, 36f)
        var selectedIndex: Int? = null

        // Render expanded menu (positioned at y = 46; Item 0 is 50..82px; Item 1 is 82..114px)
        ui.beginFrame(UiFrameInput(300f, 300f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 300f, 300f)).shadcnDropdownMenu(
            id = "menu-1",
            anchorSlot = triggerSlot,
            expanded = true,
            items = listOf(
                ShadcnDropdownMenuItem("Option 0"),
                ShadcnDropdownMenuItem("Option 1"),
                ShadcnDropdownMenuItem("Option 2"),
            ),
            width = Dimension.Fixed(160f.dp),
        ).also { selectedIndex = it.selectedIndex }
        ui.finishFrame()

        // Click on Option 1 (y offset inside menu item 1 at y = 95)
        input.setPointer(down = true, x = 30f, y = 95f)
        ui.beginFrame(UiFrameInput(300f, 300f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 300f, 300f)).shadcnDropdownMenu(
            id = "menu-1",
            anchorSlot = triggerSlot,
            expanded = true,
            items = listOf(
                ShadcnDropdownMenuItem("Option 0"),
                ShadcnDropdownMenuItem("Option 1"),
                ShadcnDropdownMenuItem("Option 2"),
            ),
            width = Dimension.Fixed(160f.dp),
        ).also { selectedIndex = it.selectedIndex }
        ui.finishFrame()

        input.setPointer(down = false, x = 30f, y = 95f)
        ui.beginFrame(UiFrameInput(300f, 300f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 300f, 300f)).shadcnDropdownMenu(
            id = "menu-1",
            anchorSlot = triggerSlot,
            expanded = true,
            items = listOf(
                ShadcnDropdownMenuItem("Option 0"),
                ShadcnDropdownMenuItem("Option 1"),
                ShadcnDropdownMenuItem("Option 2"),
            ),
            width = Dimension.Fixed(160f.dp),
        ).also { selectedIndex = it.selectedIndex }
        ui.finishFrame()

        assertEquals(1, selectedIndex, "shadcnDropdownMenu should return selectedIndex = 1 when Option 1 is clicked")
    }

    @Test
    fun dialogDismissesOnClickOutside() {
        val ui = createContext()
        val input = Input()
        var dismissed = false

        // Frame 1: Dialog rendered open
        ui.beginFrame(UiFrameInput(400f, 400f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 400f, 400f)).shadcnDialog(id = "dialog-1", expanded = true) { _ -> }
            .also { dismissed = it.dismissed }
        ui.finishFrame()

        // Frame 2: Pointer click outside dialog bounds at (10, 10)
        input.setPointer(down = true, x = 10f, y = 10f)
        ui.beginFrame(UiFrameInput(400f, 400f, input.updateSnapshot().toUiInputState()))
        ui.createUiScope(UiBounds(0f, 0f, 400f, 400f)).shadcnDialog(id = "dialog-1", expanded = true) { _ -> }
            .also { if (it.dismissed) dismissed = true }
        ui.finishFrame()

        assertTrue(dismissed, "shadcnDialog should report dismissed = true when clicked outside")
    }
}
