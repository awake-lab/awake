// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.testSnapshot
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnSidebar].
 */
class ShadcnSidebarFidelityTest {

    @Test
    fun shadcnSidebarFidelity() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(400f, 600f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnSidebar(
            id = "sidebar",
            modifier = Modifier.width(240f.dp)
        ) {
            text("Sidebar content")
        }
        val frame = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "sidebar",
                    expectedBackgroundToken = "sidebar",
                    expectedBorderToken = "sidebar-border"
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(id = "sidebar-fidelity", title = "Sidebar Fidelity"),
            frame = AwakeUiPreviewFrame(
                primitives = frame.primitives,
                background = frame.background,
                font = frame.font,
                semantics = frame.semantics
            ),
            config = config
        ).requireClean()
    }
}
