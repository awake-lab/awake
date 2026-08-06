// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.testSnapshot
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnButton].
 */
class ShadcnButtonFidelityTest {

    @Test
    fun shadcnButtonPrimaryFidelity() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.shadcnButton(
            id = "btn-fidelity",
            label = "FIDELITY",
            variant = ShadcnButtonVariant.Primary,
            modifier = Modifier.width(100f.dp).height(40f.dp)
        )
        val frame = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "btn-fidelity",
                    exactHeight = 40f.dp.toPx(),
                    exactWidth = 100f.dp.toPx()
                )
            ),
            tokenRules = listOf(
                AwakeUiPreviewTokenRule(
                    nodeId = "btn-fidelity",
                    expectedBackgroundToken = "primary"
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(id = "btn-fidelity", title = "Button Fidelity"),
            frame = AwakeUiPreviewFrame(
                primitives = frame.primitives,
                background = frame.background,
                font = frame.font,
                semantics = frame.semantics
            ),
            config = config
        ).requireClean()
    }

    @Test
    fun shadcnButtonOutlineFidelity() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(200f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.shadcnButton(
            id = "btn-outline",
            label = "OUTLINE",
            variant = ShadcnButtonVariant.Outline,
            modifier = Modifier.width(120f.dp).height(40f.dp)
        )
        val frame = ui.finishFrame()

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
            metadata = AwakeUiPreviewMetadata(id = "btn-outline", title = "Button Outline Fidelity"),
            frame = AwakeUiPreviewFrame(
                primitives = frame.primitives,
                background = frame.background,
                font = frame.font,
                semantics = frame.semantics
            ),
            config = config
        ).requireClean()
    }
}
