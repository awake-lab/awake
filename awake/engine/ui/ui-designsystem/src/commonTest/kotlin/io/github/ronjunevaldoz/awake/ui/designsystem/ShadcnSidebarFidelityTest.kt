// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnSidebar].
 */
class ShadcnSidebarFidelityTest {

    @Test
    fun shadcnSidebarFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val sidebarWidth = 240f

        ui.beginFrame(400f, 600f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnSidebar(
            id = "sidebar",
            modifier = Modifier.width(sidebarWidth.dp)
        ) {
            text("Sidebar content")
        }
        val frameOutput = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule(
                    nodeId = "sidebar",
                    exactWidth = sidebarWidth
                )
            ),
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "sidebar",
                    expectedBackgroundToken = "sidebar",
                    expectedBorderToken = "sidebar-border"
                )
            )
        )

        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "sidebar-fidelity",
                title = "Sidebar Fidelity",
                group = "Sidebar",
                summary = "Sidebar background/border fidelity check",
                width = 400,
                height = 600
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics
            ),
            config = config
        )
        if (!report.isClean) {
            println("REPORT: ${report.summary()}")
        }
        report.requireClean()
    }
}
