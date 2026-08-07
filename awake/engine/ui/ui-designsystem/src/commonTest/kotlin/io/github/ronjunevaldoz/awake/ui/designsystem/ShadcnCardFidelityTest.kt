// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewExactPaddingRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaModeMatrix
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity matrix validation tests for [shadcnCard].
 */
class ShadcnCardFidelityTest {

    @Test
    fun shadcnCardMatrixFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")

        val aggregatedReport = FigmaModeMatrix.runValidationMatrix { config ->
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            val theme = shadcnTheme(dark = config.mode == io.github.ronjunevaldoz.awake.testing.ui.FigmaMode.Dark)
            ui.pushTheme(theme)

            val cardWidth = 300f * config.scale.scale
            val cardHeight = 180f * config.scale.scale

            ui.beginFrame(400f * config.scale.scale, 300f * config.scale.scale, testSnapshot(x = -100f, y = -100f, down = false))
            ui.createAbsolute(slot = ui.frameBounds()).shadcnCard(
                id = "card-fidelity",
                modifier = Modifier.testTag("card-fidelity").width(cardWidth.dp).height(cardHeight.dp),
                header = { text("Card Title") }
            ) {
                text("Card Body Content")
            }
            val frameOutput = ui.finishFrame()

            val validationConfig = AwakeUiPreviewValidationConfig(
                tokenRules = listOf(
                    AwakeUiPreviewTokenRule(
                        nodeId = "card-fidelity",
                        expectedBackgroundToken = "card",
                        expectedBorderToken = "border"
                    )
                ),
                exactPaddingRules = listOf(
                    AwakeUiPreviewExactPaddingRule(
                        nodeId = "card-fidelity",
                        exactPaddingPx = figma.getPx("spacing-lg") * config.scale.scale
                    )
                )
            )

            validateAwakeUiPreview(
                metadata = AwakeUiPreviewMetadata(
                    id = "card-fidelity",
                    title = "Card Fidelity Matrix",
                    group = "Card",
                    summary = "Card surface fidelity check [${config.id}]",
                    width = (400 * config.scale.scale).toInt(),
                    height = (300 * config.scale.scale).toInt()
                ),
                frame = AwakeUiPreviewFrame(
                    primitives = frameOutput.primitives,
                    background = ui.currentTheme.colors.background,
                    font = ui.currentFont,
                    semantics = frameOutput.semantics
                ),
                config = validationConfig
            )
        }

        println("DEBUG CARD REPORT: ${aggregatedReport.summary()}")
        aggregatedReport.requireClean()
    }
}
