// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
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
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.headlessRoot().column {
            shadcnButton(
                id = "icon-btn",
                modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp),
                variant = ShadcnButtonVariant.Ghost,
                size = ShadcnButtonSize.Icon,
            ) {
                text(label = "x", semanticId = "icon-btn-glyph")
            }
        }
        val semantics = ui.finishFrame().semantics
        val button = requireSemanticNode(semantics, id = "icon-btn", role = UiSemanticRole.Button)
        val glyph = semantics.first { it.id == "icon-btn-glyph" }

        val buttonCenterX = button.bounds.x + button.bounds.width / 2f
        val buttonCenterY = button.bounds.y + button.bounds.height / 2f
        val glyphCenterX = glyph.bounds.x + glyph.bounds.width / 2f
        val glyphCenterY = glyph.bounds.y + glyph.bounds.height / 2f

        assertTrue(
            abs(glyphCenterX - buttonCenterX) <= 1.5f,
            "slot content must center horizontally: button center $buttonCenterX, glyph center $glyphCenterX",
        )
        assertTrue(
            abs(glyphCenterY - buttonCenterY) <= 1.5f,
            "slot content must center vertically: button center $buttonCenterY, glyph center $glyphCenterY",
        )
    }
}
