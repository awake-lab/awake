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
import io.github.ronjunevaldoz.awake.ui.designsystem.components.navigation.ShadcnTabItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.navigation.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity matrix validation tests for [shadcnTabs].
 */
class ShadcnTabsFidelityTest {

    @Test
    fun shadcnTabsMatrixFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")

        val aggregatedReport = FigmaModeMatrix.runValidationMatrix { config ->
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            val theme = shadcnTheme(dark = config.mode == io.github.ronjunevaldoz.awake.testing.ui.FigmaMode.Dark)
            ui.pushTheme(theme)

            ui.beginFrame(400f * config.scale.scale, 200f * config.scale.scale, testSnapshot(x = -100f, y = -100f, down = false))
            ui.createColumn(slot = ui.frameBounds()).shadcnTabs(
                id = "tabs-fidelity",
                items = listOf(
                    ShadcnTabItem("account", "Account"),
                    ShadcnTabItem("password", "Password"),
                ),
                selected = "account",
                modifier = Modifier.testTag("tabs-fidelity"),
            )
            val frameOutput = ui.finishFrame()

            val validationConfig = AwakeUiPreviewValidationConfig(
                tokenRules = listOf(
                    AwakeUiPreviewTokenRule(
                        nodeId = "tabs-fidelity.track",
                        expectedBackgroundToken = "muted",
                    ),
                ),
            )

            validateAwakeUiPreview(
                metadata = AwakeUiPreviewMetadata(
                    id = "tabs-fidelity",
                    title = "Tabs Fidelity Matrix",
                    group = "Tabs",
                    summary = "Tabs track fidelity check [${config.id}]",
                    width = (400 * config.scale.scale).toInt(),
                    height = (200 * config.scale.scale).toInt(),
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
            println("TABS MATRIX REPORT: ${aggregatedReport.summary()}")
        }
        aggregatedReport.requireClean()
    }
}
