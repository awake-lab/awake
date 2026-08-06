// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.*
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnInputOTP].
 */
class ShadcnInputOTPFidelityTest {

    @Test
    fun shadcnInputOTPSlotsFidelity() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(400f, 100f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnInputOTP(
            id = "otp",
            value = "123",
            length = 4,
            modifier = Modifier.width(300f.dp)
        )
        val frame = ui.finishFrame()

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.0", exactWidth = 36f.dp.toPx(), exactHeight = 40f.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.1", exactWidth = 36f.dp.toPx(), exactHeight = 40f.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.2", exactWidth = 36f.dp.toPx(), exactHeight = 40f.dp.toPx()),
                AwakeUiPreviewDimensionRule(nodeId = "otp.slot.3", exactWidth = 36f.dp.toPx(), exactHeight = 40f.dp.toPx())
            ),
            exactSpacingRules = listOf(
                AwakeUiPreviewExactSpacingRule(
                    label = "OTP Slots",
                    nodeIds = setOf("otp.slot.0", "otp.slot.1", "otp.slot.2", "otp.slot.3"),
                    exactGapPx = 6f.dp.toPx()
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(id = "otp-fidelity", title = "OTP Fidelity"),
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
