// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("CyclomaticComplexMethod")

package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnComboboxHeadlessTest {
    @Test
    fun controlledComboboxFiltersOptionsAndReportsSelection() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        val options = listOf("Apple", "Banana", "Cherry", "Cranberry")
        var selected: String? = null

        fun render() {
            val scope: UiScope = ui.createUiScope(UiBounds(0f, 0f, 400f, 400f))
            scope.combobox(
                id = "fruit",
                options = options,
                selectedIndex = options.indexOf(selected).takeIf { it >= 0 },
                modifier = Modifier.width(200f.dp),
            )?.let { selected = options[it] }
        }

        fun frame(input: Input) {
            ui.beginFrame(UiFrameInput(400f, 400f, input.updateSnapshot().toUiInputState()))
            render()
        }

        frame(Input().apply { setPointer(false, -1f, -1f) })
        val trigger = assertNotNull(ui.finishFrame().semantics.firstOrNull { it.id == "fruit" })

        val triggerX = trigger.bounds.x + trigger.bounds.width / 2f
        val triggerY = trigger.bounds.y + trigger.bounds.height / 2f
        frame(Input().apply { setPointer(true, triggerX, triggerY) })
        ui.finishFrame()
        frame(Input().apply { setPointer(false, triggerX, triggerY) })
        val opened = ui.finishFrame()
        val filter = assertNotNull(opened.semantics.firstOrNull { it.id == "fruit.filter" })
        assertEquals(
            4,
            opened.semantics.count { semantic ->
                semantic.id?.let { id -> id.startsWith("fruit.option.") && !id.endsWith(".label") } == true
            },
            opened.semantics.mapNotNull { it.id }.toString(),
        )

        val filterX = filter.bounds.x + filter.bounds.width / 2f
        val filterY = filter.bounds.y + filter.bounds.height / 2f
        frame(Input().apply { setPointer(true, filterX, filterY) })
        ui.finishFrame()
        frame(Input().apply { setPointer(false, filterX, filterY) })
        ui.finishFrame()
        frame(
            Input().apply {
                setPointer(false, filterX, filterY)
                pushTypedText("Cran")
            },
        )
        val filtered = ui.finishFrame()
        assertEquals(
            listOf("fruit.option.3"),
            filtered.semantics.mapNotNull { it.id }
                .filter { id -> id.startsWith("fruit.option.") && !id.endsWith(".label") },
        )

        val option = assertNotNull(filtered.semantics.firstOrNull { it.id == "fruit.option.3" })
        val optionX = option.bounds.x + option.bounds.width / 2f
        val optionY = option.bounds.y + option.bounds.height / 2f
        frame(Input().apply { setPointer(true, optionX, optionY) })
        ui.finishFrame()
        frame(Input().apply { setPointer(false, optionX, optionY) })
        ui.finishFrame()
        assertEquals("Cranberry", selected)
        frame(Input().apply { setPointer(false, optionX, optionY) })
        assertTrue(ui.finishFrame().semantics.isNotEmpty())
    }
}
