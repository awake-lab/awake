// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewExactSpacingRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnInputOTP].
 */
class ShadcnInputOTPFidelityTest {

    @Test
    fun shadcnInputOTPSlotsFidelity() = runTest {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(400f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute(slot = ui.frameBounds()).shadcnInputOTP(
            id = "otp",
            value = "123",
            length = 4,
            modifier = Modifier.width(300f.dp),
        )
        val frameOutput = ui.finishFrame()

        // shadcn's InputOTPSlot is h-9 w-9 (36x36, square).
        val slotWidth = 36f
        val slotHeight = 36f
        val slotSpacing = 6f

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.0", exactWidth = slotWidth.dp.toPx(), exactHeight = slotHeight.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.1", exactWidth = slotWidth.dp.toPx(), exactHeight = slotHeight.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.2", exactWidth = slotWidth.dp.toPx(), exactHeight = slotHeight.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.3", exactWidth = slotWidth.dp.toPx(), exactHeight = slotHeight.dp.toPx()),
            ),
            exactSpacingRules = listOf(
                AwakeUiPreviewExactSpacingRule(
                    label = "OTP Slots",
                    nodeIds = setOf("otp.slot.0", "otp.slot.1", "otp.slot.2", "otp.slot.3"),
                    exactGapPx = slotSpacing.dp.toPx(),
                ),
            ),
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "otp-fidelity",
                title = "OTP Fidelity",
                group = "Input",
                summary = "OTP slots fidelity check",
                width = 400,
                height = 100,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics,
            ),
            config = config,
        ).requireClean()
    }
}
