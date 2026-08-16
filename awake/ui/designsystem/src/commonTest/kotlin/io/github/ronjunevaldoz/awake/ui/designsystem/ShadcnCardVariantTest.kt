// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Covers `ShadcnCardVariant.Elevated`'s shadow approximation and `ShadcnCardSize`'s
 * divider-gap axis (Fix 4 in the shadcn-compose parity audit). */
class ShadcnCardVariantTest {

    @Test
    fun elevatedCardEmitsExtraShadowQuadsDefaultDoesNot() {
        fun quadCount(variant: ShadcnCardVariant): Int {
            return renderShadcnComponent(width = 200f, height = 200f, font = BitmapFont()) {
                shadcnCard(
                    id = "card",
                    variant = variant,
                    modifier = Modifier,
                ) {
                    // no body content needed
                }
            }.primitives.count { it is UiDrawPrimitive.Quad || it is UiDrawPrimitive.ShadowQuad }
        }

        assertTrue(
            quadCount(ShadcnCardVariant.Elevated) > quadCount(ShadcnCardVariant.Default),
            "Elevated should draw an explicit shadow primitive the Default variant does not",
        )
    }

    @Test
    fun compactCardSizeUsesASmallerHeaderFooterGapThanDefault() {
        fun cardHeight(size: ShadcnCardSize): Float {
            return renderShadcnComponent(width = 280f, height = 260f, font = BitmapFont()) {
                shadcnCard(
                    id = "card",
                    size = size,
                    modifier = Modifier,
                    header = { text("Title") },
                    footer = { text("Footer") },
                ) {
                    text("Body")
                }
            }.bounds("card").height
        }

        assertTrue(
            cardHeight(ShadcnCardSize.Compact) < cardHeight(ShadcnCardSize.Default),
            "Compact card should be shorter than Default given identical content",
        )
    }
}
