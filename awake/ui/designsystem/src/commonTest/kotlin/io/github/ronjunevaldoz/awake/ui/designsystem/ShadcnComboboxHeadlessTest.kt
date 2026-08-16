// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("CyclomaticComplexMethod")

package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnComboboxHeadlessTest {
    @Test
    fun controlledComboboxFiltersOptionsAndReportsSelection() {
        val options = listOf("Apple", "Banana", "Cherry", "Cranberry")
        var selected: String? = null

        uiTestSession(
            width = 400f,
            height = 400f,
            font = BitmapFont(),
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            fun UiScope.render() {
                combobox(
                    id = "fruit",
                    options = options,
                    selectedIndex = options.indexOf(selected).takeIf { it >= 0 },
                    modifier = Modifier.width(200f.dp),
                )?.let { selected = options[it] }
            }

            val trigger = assertNotNull(frame { render() }.nodeOrNull("fruit"))

            val triggerX = trigger.bounds.x + trigger.bounds.width / 2f
            val triggerY = trigger.bounds.y + trigger.bounds.height / 2f
            val opened = click(triggerX, triggerY) { render() }
            val filter = assertNotNull(opened.nodeOrNull("fruit.filter"))
            assertEquals(
                4,
                opened.semantics.count { semantic ->
                    semantic.id?.let { id -> id.startsWith("fruit.option.") && !id.endsWith(".label") } == true
                },
                opened.semantics.mapNotNull { it.id }.toString(),
            )

            val filterX = filter.bounds.x + filter.bounds.width / 2f
            val filterY = filter.bounds.y + filter.bounds.height / 2f
            click(filterX, filterY) { render() }
            input.pushTypedText("Cran")
            val filtered = frame(x = filterX, y = filterY) { render() }
            assertEquals(
                listOf("fruit.option.3"),
                filtered.semantics.mapNotNull { it.id }
                    .filter { id -> id.startsWith("fruit.option.") && !id.endsWith(".label") },
            )

            val option = assertNotNull(filtered.nodeOrNull("fruit.option.3"))
            assertEquals(UiSemanticRole.MenuItem, option.role)
            assertTrue(
                filtered.semantics.none { semantic ->
                    semantic.id?.startsWith("fruit.option.") == true && semantic.role == UiSemanticRole.Button
                },
                "combobox options must be menu items, not trigger buttons",
            )
            val optionX = option.bounds.x + option.bounds.width / 2f
            val optionY = option.bounds.y + option.bounds.height / 2f
            click(optionX, optionY) { render() }
            assertEquals("Cranberry", selected)
            assertTrue(frame(x = optionX, y = optionY) { render() }.semantics.isNotEmpty())
        }
    }
}
