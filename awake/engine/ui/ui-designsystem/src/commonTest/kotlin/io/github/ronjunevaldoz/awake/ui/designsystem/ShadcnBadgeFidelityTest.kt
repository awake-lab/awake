// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnBadge].
 */
class ShadcnBadgeFidelityTest {

    @Test
    fun shadcnBadgePrimaryFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute(slot = ui.frameBounds()).shadcnBadge(
            label = "BETA",
            variant = ShadcnBadgeVariant.Primary,
            modifier = Modifier.testTag("badge-fidelity"),
        )
        val frameOutput = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "badge-fidelity",
                    expectedBackgroundToken = "primary",
                    expectedForegroundToken = "primary-foreground",
                ),
            ),
        )

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
