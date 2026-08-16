// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.requestFocus
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnBehaviorParityTest {

    @Test
    fun buttonClickTriggersActivation() = showcaseTestSession(
        width = 200f,
        height = 100f,
        theme = shadcnThemeValues(),
    ) {
        var clicked = false

        // Frame 1: Pointer down over button at (30, 30)
        frame(x = 30f, y = 30f, down = true) {
            shadcnButton(
                id = "btn-1",
                label = "Submit",
                modifier = Modifier.offset(10f.dp, 10f.dp).width(100f.dp).height(40f.dp),
            ).also { if (it) clicked = true }
        }

        // Frame 2: Pointer up over button at (30, 30)
        frame(x = 30f, y = 30f, down = false) {
            shadcnButton(
                id = "btn-1",
                label = "Submit",
                modifier = Modifier.offset(10f.dp, 10f.dp).width(100f.dp).height(40f.dp),
            ).also { if (it) clicked = true }
        }

        assertTrue(clicked, "shadcnButton should emit click activation when pressed and released")
    }

    @Test
    fun buttonSpaceKeyTriggersActivation() = showcaseTestSession(
        width = 200f,
        height = 100f,
        theme = shadcnThemeValues(),
    ) {
        var clicked = false

        // Set focus to "btn-1"
        frame {
            requestFocus("btn-1")
            shadcnButton("btn-1", label = "Submit")
        }

        // Press Space key while focused
        input.setKeyDown(Key.Space, down = true)
        frame {
            shadcnButton("btn-1", label = "Submit")
                .also { if (it) clicked = true }
        }

        assertTrue(clicked, "shadcnButton should activate on Space key when focused")
    }

    @Test
    fun switchTogglesStateOnClick() = showcaseTestSession(
        width = 200f,
        height = 100f,
        theme = shadcnThemeValues(),
    ) {
        var state = false

        // Press down
        frame(x = 20f, y = 20f, down = true) {
            state = shadcnSwitch(
                id = "switch-1",
                checked = state,
                modifier = Modifier.offset(10f.dp, 10f.dp).width(44f.dp).height(24f.dp),
            )
        }

        // Release pointer
        frame(x = 20f, y = 20f, down = false) {
            state = shadcnSwitch(
                id = "switch-1",
                checked = state,
                modifier = Modifier.offset(10f.dp, 10f.dp).width(44f.dp).height(24f.dp),
            )
        }

        assertTrue(state, "shadcnSwitch should toggle state to true on click")
    }

    @Test
    fun checkboxTogglesStateOnClick() = showcaseTestSession(
        width = 200f,
        height = 100f,
        theme = shadcnThemeValues(),
    ) {
        var state = false

        // Press down
        frame(x = 20f, y = 20f, down = true) {
            state = shadcnCheckbox(
                id = "check-1",
                checked = state,
                modifier = Modifier.offset(10f.dp, 10f.dp).width(16f.dp).height(16f.dp),
            )
        }

        // Release pointer
        frame(x = 20f, y = 20f, down = false) {
            state = shadcnCheckbox(
                id = "check-1",
                checked = state,
                modifier = Modifier.offset(10f.dp, 10f.dp).width(16f.dp).height(16f.dp),
            )
        }

        assertTrue(state, "shadcnCheckbox should toggle state to true on click")
    }

    @Test
    fun dropdownMenuSelectsItemOnClick() = showcaseTestSession(
        width = 300f,
        height = 300f,
        theme = shadcnThemeValues(),
    ) {
        val triggerSlot = UiBounds(10f, 10f, 100f, 36f)
        var selectedIndex: Int? = null

        // Render expanded menu (positioned at y = 46; Item 0 is 50..82px; Item 1 is 82..114px)
        frame {
            shadcnDropdownMenu(
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
        }

        // Click on Option 1 (y offset inside menu item 1 at y = 95)
        frame(x = 30f, y = 95f, down = true) {
            shadcnDropdownMenu(
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
        }

        frame(x = 30f, y = 95f, down = false) {
            shadcnDropdownMenu(
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
        }

        assertEquals(1, selectedIndex, "shadcnDropdownMenu should return selectedIndex = 1 when Option 1 is clicked")
    }

    @Test
    fun dialogDismissesOnClickOutside() = showcaseTestSession(
        width = 400f,
        height = 400f,
        theme = shadcnThemeValues(),
    ) {
        var dismissed = false

        // Frame 1: Dialog rendered open
        frame {
            shadcnDialog(id = "dialog-1", expanded = true) { _ -> }
                .also { dismissed = it.dismissed }
        }

        // Frame 2: Pointer click outside dialog bounds at (10, 10)
        frame(x = 10f, y = 10f, down = true) {
            shadcnDialog(id = "dialog-1", expanded = true) { _ -> }
                .also { if (it.dismissed) dismissed = true }
        }

        assertTrue(dismissed, "shadcnDialog should report dismissed = true when clicked outside")
    }
}
