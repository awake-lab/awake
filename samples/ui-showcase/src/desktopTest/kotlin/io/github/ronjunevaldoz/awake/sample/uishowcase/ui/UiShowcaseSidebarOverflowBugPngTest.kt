// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewScene
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/** Repro for the sidebar-menu overflow bug: when a sidebar has enough items to overflow
 * its fixed height, the individual [shadcnSidebarMenuItem] buttons should not spill past
 * the sidebar's own rounded container edges. */
class UiShowcaseSidebarOverflowBugPngTest {

    @Test
    fun dumpOverflowRepro() {
        saveRepro("debug-sidebar-overflow-bug")
    }

    private fun saveRepro(id: String) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(320f, 400f, input.updateSnapshot().toUiInputState())
        ui.pushTheme(theme)
        ui.createUiScope(UiBounds(x = 24f, y = 24f, width = 272f, height = 352f)).shadcnSidebar(
            id = "overflow-sidebar",
            modifier = Modifier.width(272f.dp),
        ) { _ ->
            shadcnSidebarGroup(label = "OVERFLOW TEST") {
                shadcnSidebarMenu {
                    repeat(20) { index ->
                        shadcnSidebarMenuItem(
                            id = "item-$index",
                            label = "MenuItem $index",
                            onClick = { },
                        )
                    }
                }
            }
        }

        val output = ui.finishFrame()
        val scene = AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = id,
                title = id,
                group = "Debug",
                summary = "Repro for sidebar menu content spilling past container edges.",
                width = 320,
                height = 400,
                reportScale = 2,
            ),
            primitives = output.primitives,
            background = theme.colors.background,
            font = ui.currentFont,
            semantics = output.semantics,
        )
        saveAwakeUiPreview(scene)
    }
}
