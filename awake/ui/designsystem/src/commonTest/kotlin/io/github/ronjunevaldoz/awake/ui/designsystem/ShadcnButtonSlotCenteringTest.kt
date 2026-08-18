// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Slot-composed button content must render centered in the button's box -- the reference
 * button is `inline-flex items-center justify-center`. Regression for the studio icon rails:
 * slot content went through a TopStart-aligned box and every icon glyph sat in the button's
 * top-left corner.
 */
class ShadcnButtonSlotCenteringTest {

    @Test
    fun slotContentCentersInsideAnIconButton() {
        val frame = renderShadcnComponent(width = 200f, height = 100f) {
            column {
                shadcnButton(
                    id = "icon-btn",
                    modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp),
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                ) {
                    text(label = "x", semanticId = "icon-btn-glyph")
                }
            }
        }
        val button = frame.bounds("icon-btn")
        val glyph = frame.bounds("icon-btn-glyph")

        assertTrue(
            abs((glyph.x + glyph.width / 2f) - (button.x + button.width / 2f)) <= 1.5f,
            "slot content must center horizontally in the button",
        )
        assertTrue(
            abs((glyph.y + glyph.height / 2f) - (button.y + button.height / 2f)) <= 1.5f,
            "slot content must center vertically in the button",
        )
    }
}
