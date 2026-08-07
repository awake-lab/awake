// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Covers `ShadcnCardVariant.Elevated`'s shadow approximation and `ShadcnCardSize`'s
 * divider-gap axis (Fix 4 in the shadcn-compose parity audit). */
class ShadcnCardVariantTest {

    @Test
    fun elevatedCardEmitsExtraShadowQuadsDefaultDoesNot() {
        fun quadCount(variant: ShadcnCardVariant): Int {
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            ui.pushTheme(ShadcnTheme)
            ui.beginFrame(200f, 200f, testSnapshot())
            ui.createColumn(x = 10f, y = 10f, width = 120f)
                .shadcnCard(id = "card", variant = variant, modifier = Modifier.height(Dimension.WrapContent)) {
                    // no body content needed
                }
            return ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Quad>().size
        }

        assertTrue(
            quadCount(ShadcnCardVariant.Elevated) > quadCount(ShadcnCardVariant.Default),
            "Elevated should draw the two extra shadow strips the Default variant doesn't",
        )
    }

    @Test
    fun compactCardSizeUsesASmallerHeaderFooterGapThanDefault() {
        fun cardHeight(size: ShadcnCardSize): Float {
            val ui = UiContext()
            ui.pushFont(BitmapFont())
            ui.pushTheme(ShadcnTheme)
            ui.beginFrame(280f, 260f, testSnapshot())
            ui.createColumn(x = 10f, y = 10f, width = 200f).shadcnCard(
                id = "card",
                size = size,
                modifier = Modifier.height(Dimension.WrapContent),
                header = { text("Title") },
                footer = { text("Footer") },
            ) {
                text("Body")
            }
            return assertNotNull(ui.finishFrame().semantics.firstOrNull { it.id == "card" }).bounds.height
        }

        assertTrue(
            cardHeight(ShadcnCardSize.Compact) < cardHeight(ShadcnCardSize.Default),
            "Compact card should be shorter than Default given identical content",
        )
    }
}
