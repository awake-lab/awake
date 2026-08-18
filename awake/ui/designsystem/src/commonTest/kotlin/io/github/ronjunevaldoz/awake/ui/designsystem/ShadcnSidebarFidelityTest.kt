// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnSidebar].
 */
class ShadcnSidebarFidelityTest {

    @Test
    fun shadcnSidebarFidelity() = runTest {
        val sidebarWidth = 240f

        val frameOutput = renderUiComponent(
            width = 400f,
            height = 600f,
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            shadcnSidebar(
                id = "sidebar",
                modifier = Modifier.width(sidebarWidth.dp),
            ) {
                text("Sidebar content")
            }
        }

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
                background = ShadcnTheme.colors.background,
                font = frameOutput.font,
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
