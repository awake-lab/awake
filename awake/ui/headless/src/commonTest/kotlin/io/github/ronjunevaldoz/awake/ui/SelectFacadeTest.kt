// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.select
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.select] driven only
 * through the public `UiScope` API -- no existing test file owned the facade `select`;
 * `UiPopupTest` exercises `headless.internal.controls.select` instead.
 */
class SelectFacadeTest {

    @Test
    fun triggerRendersAndOptionClickSelects() {
        val options = listOf("One", "Two", "Three")
        var selectedIndex: Int? = null

        uiTestSession(width = 300f, height = 300f) {
            val initial = frame {
                selectedIndex = select(
                    id = "select.smoke",
                    options = options,
                    selectedIndex = selectedIndex,
                    modifier = Modifier.width(160f.dp).height(36f.dp),
                )
            }
            val trigger = initial.bounds("select.smoke.trigger")
            assertTrue(trigger.width > 0f && trigger.height > 0f, "trigger must render with real bounds")

            val afterOpen = click(trigger.x + trigger.width / 2f, trigger.y + trigger.height / 2f) {
                selectedIndex = select(
                    id = "select.smoke",
                    options = options,
                    selectedIndex = selectedIndex,
                    modifier = Modifier.width(160f.dp).height(36f.dp),
                )
            }
            val option = afterOpen.bounds("select.smoke.option0")

            click(option.x + option.width / 2f, option.y + option.height / 2f) {
                selectedIndex = select(
                    id = "select.smoke",
                    options = options,
                    selectedIndex = selectedIndex,
                    modifier = Modifier.width(160f.dp).height(36f.dp),
                )
            }
        }

        assertEquals(0, selectedIndex, "clicking the first option must select it")
    }
}
