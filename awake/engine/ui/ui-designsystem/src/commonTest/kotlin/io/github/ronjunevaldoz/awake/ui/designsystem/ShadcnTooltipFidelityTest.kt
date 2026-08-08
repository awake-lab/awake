// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaModeMatrix
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity matrix validation tests for [shadcnTooltipText].
 */
class ShadcnTooltipFidelityTest {

    @Test
    fun shadcnTooltipMatrixFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")

        val aggregatedReport = FigmaModeMatrix.runValidationMatrix { config ->
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            val theme = shadcnTheme(dark = config.mode == io.github.ronjunevaldoz.awake.testing.ui.FigmaMode.Dark)
            ui.pushTheme(theme)

            ui.beginFrame(300f * config.scale.scale, 150f * config.scale.scale, testSnapshot(x = -100f, y = -100f, down = false))
            val anchor = UiBounds(x = 100f, y = 50f, width = 100f, height = 30f)
            ui.createAbsolute(slot = ui.frameBounds()).shadcnTooltipText(
                anchorSlot = anchor,
                visible = true,
                text = "Tooltip Info",
                id = "tooltip-fidelity",
            )
            val frameOutput = ui.finishFrame()

            // Real shadcn's TooltipContent is a solid inverted-color pill (`bg-foreground
            // text-background`), not a card -- no border either.
            val validationConfig = AwakeUiPreviewValidationConfig(
                tokenRules = listOf(
                    AwakeUiPreviewTokenRule(
                        nodeId = "tooltip-fidelity",
                        expectedBackgroundToken = "foreground",
                    ),
                ),
            )

            validateAwakeUiPreview(
                metadata = AwakeUiPreviewMetadata(
                    id = "tooltip-fidelity",
                    title = "Tooltip Fidelity Matrix",
                    group = "Tooltip",
                    summary = "Tooltip content fidelity check [${config.id}]",
                    width = (300 * config.scale.scale).toInt(),
                    height = (150 * config.scale.scale).toInt(),
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
            println("TOOLTIP MATRIX REPORT: ${aggregatedReport.summary()}")
        }
        aggregatedReport.requireClean()
    }
}
