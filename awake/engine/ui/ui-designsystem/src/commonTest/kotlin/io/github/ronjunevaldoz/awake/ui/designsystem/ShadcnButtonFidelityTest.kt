// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnButton].
 */
class ShadcnButtonFidelityTest {

    @Test
    fun shadcnButtonPrimaryFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val btnWidth = 120f
        val btnHeight = 40f

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnButton(
            id = "btn-fidelity",
            label = "FIDELITY",
            variant = ShadcnButtonVariant.Primary,
            modifier = Modifier.width(btnWidth.dp).height(btnHeight.dp)
        )
        val frameOutput = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "btn-fidelity",
                    exactHeight = btnHeight.dp.toPx(),
                    exactWidth = btnWidth.dp.toPx()
                )
            ),
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "btn-fidelity",
                    expectedBackgroundToken = "primary"
                )
            )
        )

        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "btn-fidelity",
                title = "Button Fidelity",
                group = "Button",
                summary = "Primary fidelity check",
                width = 240,
                height = 100
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

    @Test
    fun shadcnButtonOutlineFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val btnWidth = 120f
        val btnHeight = 40f

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnButton(
            id = "btn-outline",
            label = "OUTLINE",
            variant = ShadcnButtonVariant.Outline,
            modifier = Modifier.width(btnWidth.dp).height(btnHeight.dp)
        )
        val frameOutput = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "btn-outline",
                    expectedBackgroundToken = "background",
                    expectedBorderToken = "border"
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "btn-outline",
                title = "Button Outline Fidelity",
                group = "Button",
                summary = "Outline fidelity check",
                width = 200,
                height = 100
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics
            ),
            config = config
        ).requireClean()
    }
}
