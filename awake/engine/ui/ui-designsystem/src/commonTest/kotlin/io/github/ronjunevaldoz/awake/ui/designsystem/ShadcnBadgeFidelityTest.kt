// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewTokenRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.testSnapshot
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.createAbsolute
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import kotlin.test.Test

/**
 * High-fidelity validation tests for [shadcnBadge].
 */
class ShadcnBadgeFidelityTest {

    @Test
    fun shadcnBadgePrimaryFidelity() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(200f, 80f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute().shadcnBadge(
            label = "BETA",
            variant = ShadcnBadgeVariant.Primary,
            modifier = Modifier // Uses WrapContent by default
        )
        val frame = ui.finishFrame()

        // Since we didn't provide an ID, it might use a default or we should provide one for validation
        // I'll update the call to use an explicit ID if shadcnBadge supports it, or check semantics by label
    }
}
