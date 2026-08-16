// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnButton].
 */
class ShadcnButtonFidelityTest {

    @Test
    fun shadcnButtonPrimaryFidelity() = runTest {
        val btnWidth = 160f
        val btnHeight = 40f

        val frameOutput = renderShadcnComponent(width = 200f, height = 100f) { _ ->
            shadcnButton(
                id = "btn-fidelity",
                label = "FIDELITY",
                variant = ShadcnButtonVariant.Primary,
                modifier = Modifier.width(btnWidth.dp).height(btnHeight.dp),
            )
        }

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "btn-fidelity",
                    exactHeight = btnHeight,
                    exactWidth = btnWidth,
                ),
            ),
        )

        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "btn-fidelity",
                title = "Button Fidelity",
                group = "Button",
                summary = "Primary fidelity check",
                width = 240,
                height = 100,
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

    @Test
    fun shadcnButtonOutlineFidelity() = runTest {
        val btnWidth = 160f
        val btnHeight = 40f

        val frameOutput = renderShadcnComponent(width = 200f, height = 100f) { _ ->
            shadcnButton(
                id = "btn-outline",
                label = "OUTLINE",
                variant = ShadcnButtonVariant.Outline,
                modifier = Modifier.width(btnWidth.dp).height(btnHeight.dp),
            )
        }

        val config = AwakeUiPreviewValidationConfig()

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "btn-outline",
                title = "Button Outline Fidelity",
                group = "Button",
                summary = "Outline fidelity check",
                width = 200,
                height = 100,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ShadcnTheme.colors.background,
                font = io.github.ronjunevaldoz.awake.ui.font.UiFonts.default(),
                semantics = frameOutput.semantics,
            ),
            config = config,
        ).requireClean()
    }
}
