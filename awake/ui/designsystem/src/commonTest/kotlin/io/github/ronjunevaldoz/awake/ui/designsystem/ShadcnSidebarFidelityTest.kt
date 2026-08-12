// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnSidebar].
 */
class ShadcnSidebarFidelityTest {

    @Test
    fun shadcnSidebarFidelity() = runTest {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val sidebarWidth = 240f

        ui.beginFrame(400f, 600f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.headlessRoot().shadcnSidebar(
            id = "sidebar",
            modifier = Modifier.width(sidebarWidth.dp),
        ) {
            text("Sidebar content")
        }
        val frameOutput = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule(
                    nodeId = "sidebar",
                    exactWidth = sidebarWidth,
                ),
            ),
            // Headless validation checks resolved geometry; Core token IDs are intentionally
            // unavailable through the public receiver.
        )

        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "sidebar-fidelity",
                title = "Sidebar Fidelity",
                group = "Sidebar",
                summary = "Sidebar background/border fidelity check",
                width = 400,
                height = 600,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics,
            ),
            config = config,
        )
        if (!report.isClean) {
            println("REPORT: ${report.summary()}")
        }
        report.requireClean()
    }
}
