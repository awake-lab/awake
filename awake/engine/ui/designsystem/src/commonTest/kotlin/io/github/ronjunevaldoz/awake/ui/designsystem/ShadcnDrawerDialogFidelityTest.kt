// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaModeMatrix
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDrawerPosition
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity matrix validation tests for [shadcnDrawer] and popup overlays.
 */
class ShadcnDrawerDialogFidelityTest {

    @Test
    fun shadcnDrawerDialogMatrixFidelity() = runTest {
        val aggregatedReport = FigmaModeMatrix.runValidationMatrix { config ->
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            val theme =
                shadcnTheme(dark = config.mode == io.github.ronjunevaldoz.awake.testing.ui.FigmaMode.Dark)
            ui.pushTheme(theme)

            ui.beginFrame(
                400f * config.scale.scale,
                500f * config.scale.scale,
                testSnapshot(x = -100f, y = -100f, down = false),
            )
            ui.createAbsolute(slot = ui.frameBounds()).shadcnDrawer(
                id = "drawer-fidelity",
                expanded = true,
                onDismissRequest = {},
                position = ShadcnDrawerPosition.Bottom,
                sizeDp = 200f.dp,
                modifier = Modifier.testTag("drawer-panel"),
            ) {
                text("Drawer Content Body")
            }
            val frameOutput = ui.finishFrame()

            val validationConfig = AwakeUiPreviewValidationConfig(
                tokenRules = listOf(
                    AwakeUiPreviewTokenRule(
                        nodeId = "drawer-fidelity",
                        expectedBackgroundToken = "card",
                        expectedBorderToken = "border",
                    ),
                ),
            )

            validateAwakeUiPreview(
                metadata = AwakeUiPreviewMetadata(
                    id = "drawer-fidelity",
                    title = "Drawer Fidelity Matrix",
                    group = "Drawer",
                    summary = "Drawer panel fidelity check [${config.id}]",
                    width = (400 * config.scale.scale).toInt(),
                    height = (500 * config.scale.scale).toInt(),
                ),
                frame = AwakeUiPreviewFrame(
                    primitives = frameOutput.primitives,
                    background = ui.currentTheme.colors.background,
                    font = ui.currentFont,
                    semantics = frameOutput.semantics,
                ),
                config = validationConfig,
            )
        }

        if (!aggregatedReport.isClean) {
            println("DRAWER MATRIX REPORT: ${aggregatedReport.summary()}")
        }
        aggregatedReport.requireClean()
    }
}
