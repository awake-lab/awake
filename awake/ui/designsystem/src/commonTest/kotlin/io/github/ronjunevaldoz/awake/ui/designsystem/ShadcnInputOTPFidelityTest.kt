// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewExactSpacingRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnInputOTP].
 */
class ShadcnInputOTPFidelityTest {

    @Test
    fun shadcnInputOTPSlotsFidelity() = runTest {
        val font = BitmapFont()
        val frameOutput = renderShadcnComponent(width = 400f, height = 100f, font = font) {
            shadcnInputOTP(
                id = "otp",
                value = "123",
                length = 4,
                modifier = Modifier.width(300f.dp),
            )
        }

        // shadcn's InputOTPSlot is h-9 w-9 (36x36, square).
        val slotWidth = 36f
        val slotHeight = 36f
        val slotSpacing = 6f

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.0", exactWidth = slotWidth, exactHeight = slotHeight),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.1", exactWidth = slotWidth, exactHeight = slotHeight),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.2", exactWidth = slotWidth, exactHeight = slotHeight),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.3", exactWidth = slotWidth, exactHeight = slotHeight),
            ),
            exactSpacingRules = listOf(
                AwakeUiPreviewExactSpacingRule(
                    label = "OTP Slots",
                    nodeIds = setOf("otp.slot.0", "otp.slot.1", "otp.slot.2", "otp.slot.3"),
                    exactGapPx = slotSpacing,
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
                background = ShadcnTheme.colors.background,
                font = font,
                semantics = frameOutput.semantics,
            ),
            config = config,
        ).requireClean()
    }
}
