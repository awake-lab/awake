// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnBadge].
 */
class ShadcnBadgeFidelityTest {

    @Test
    fun shadcnBadgePrimaryFidelity() = runTest {
        val frameOutput = renderShadcnComponent(width = 200f, height = 80f) { _ ->
            shadcnBadge(
                id = "badge-fidelity",
                label = "BETA",
                variant = ShadcnBadgeVariant.Primary,
            )
        }

        val config = AwakeUiPreviewValidationConfig()

        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "badge-fidelity",
                title = "Badge Fidelity",
                group = "Badge",
                summary = "Primary fidelity check",
                width = 200,
                height = 80,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ShadcnTheme.colors.background,
                font = io.github.ronjunevaldoz.awake.ui.font.UiFonts.default(),
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
