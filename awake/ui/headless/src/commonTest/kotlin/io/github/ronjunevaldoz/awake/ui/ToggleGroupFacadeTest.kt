// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.toggleGroup
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Facade-level smoke coverage for both [io.github.ronjunevaldoz.awake.ui.headless.toggleGroup]
 * overloads, driven only through the public `UiScope` API and its callback-based state contract
 * (`onIndexChange`/`onSelectedIndicesChange`) -- no `headless.internal.*` import, unlike
 * `ToggleGroupWidgetTest` (which drives `headless.internal.controls.toggleGroup` directly and has
 * no callback at all).
 */
class ToggleGroupFacadeTest {

    @Test
    fun singleSelectOverloadCallsOnIndexChangeWhenAnUnselectedOptionIsClicked() {
        var selectedIndex = 0
        val render: UiScope.() -> Unit = {
            toggleGroup(
                id = "toggleGroup.single",
                options = listOf("One", "Two", "Three"),
                selectedIndex = selectedIndex,
                modifier = Modifier.width(300f.dp).height(40f.dp),
                onIndexChange = { selectedIndex = it },
            )
        }
        uiTestSession(width = 300f, height = 80f) {
            val initial = frame { render() }
            val option = initial.bounds("toggleGroup.single.1")
            click(option.x + option.width / 2f, option.y + option.height / 2f) { render() }
        }

        assertEquals(1, selectedIndex, "clicking option 1 must call onIndexChange(1)")
    }

    @Test
    fun multiSelectOverloadAddsTheClickedIndexToSelectedIndices() {
        var selectedIndices = setOf<Int>()
        val render: UiScope.() -> Unit = {
            toggleGroup(
                id = "toggleGroup.multi",
                options = listOf("Bold", "Italic"),
                selectedIndices = selectedIndices,
                modifier = Modifier.width(300f.dp).height(40f.dp),
                onSelectedIndicesChange = { selectedIndices = it },
            )
        }
        uiTestSession(width = 300f, height = 80f) {
            val initial = frame { render() }
            val option = initial.bounds("toggleGroup.multi.0")
            click(option.x + option.width / 2f, option.y + option.height / 2f) { render() }
        }

        assertEquals(setOf(0), selectedIndices, "clicking option 0 must add it to selectedIndices")
    }
}
