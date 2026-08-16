// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaModeMatrix
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity matrix validation tests for [shadcnSelect].
 */
class ShadcnSelectFidelityTest {

    @Test
    fun shadcnSelectMatrixFidelity() = runTest {
        val aggregatedReport = FigmaModeMatrix.runValidationMatrix { config ->
            val theme = shadcnThemeValues(dark = config.mode == io.github.ronjunevaldoz.awake.testing.ui.FigmaMode.Dark)
            val frameOutput = renderShadcnComponent(
                width = 300f * config.scale.scale,
                height = 100f * config.scale.scale,
                theme = theme,
            ) { _ ->
                shadcnSelect(
                    id = "select-fidelity",
                    options = listOf("Option 1", "Option 2"),
                    selectedIndex = 0,
                    modifier = Modifier.width(200f.dp).height(40f.dp),
                )
            }

            val validationConfig = AwakeUiPreviewValidationConfig()

            validateAwakeUiPreview(
                metadata = AwakeUiPreviewMetadata(
                    id = "select-fidelity",
                    title = "Select Fidelity Matrix",
                    group = "Select",
                    summary = "Select trigger fidelity check [${config.id}]",
                    width = (300 * config.scale.scale).toInt(),
                    height = (100 * config.scale.scale).toInt(),
                ),
                frame = AwakeUiPreviewFrame(
                    primitives = frameOutput.primitives,
                    background = theme.colors.background,
                    font = io.github.ronjunevaldoz.awake.ui.font.UiFonts.default(),
                    semantics = frameOutput.semantics,
                ),
                config = validationConfig,
            )
        }

        if (!aggregatedReport.isClean) {
            println("SELECT MATRIX REPORT: ${aggregatedReport.summary()}")
        }
        aggregatedReport.requireClean()
    }
}
