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
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/** Isolated, cropped-friendly repro of the sidebar-label-overflow bug: a single
 * [shadcnSidebarMenuItem] with a long real catalog title, in a 264dp sidebar (the real
 * production width), on a small canvas with the sidebar's own border visible so the overflow
 * past its edge is unambiguous in the rendered PNG. */
class UiShowcaseSidebarOverflowBugPngTest {

    @Test
    fun dumpOverflowRepro() {
        saveRepro("debug-sidebar-overflow-bug")
    }

    private fun saveRepro(id: String) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default(cellSize = 12)
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(360f, 90f, input.updateSnapshot().toUiInputState())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column {
            shadcnSidebar(
                id = "overflow-repro-sidebar",
                style = Style { border(1f.dp, theme.colors.foreground) },
                modifier = Modifier.width(264f.dp.toDimension()).height(Dimension.Fixed(80f.dp)),
            ) {
                shadcnSidebarGroup {
                    shadcnSidebarMenu {
                        shadcnSidebarMenuItem(
                            id = "overflow-repro-item",
                            label = "Dropdown Menu And Dialog",
                            active = false,
                        )
                    }
                }
            }
        }

        val scene = AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = id,
                title = id,
                group = "Debug",
                summary = "Isolated sidebar label overflow repro.",
                width = 360,
                height = 90,
                reportScale = 2,
            ),
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
        saveAwakeUiPreview(scene)
    }
}
