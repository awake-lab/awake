// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonResult
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import io.github.ronjunevaldoz.awake.ui.layout.*

class RowSpacerCompositionTest {

    @Test
    fun dslRowSpacerPreservesHorizontalLayoutProgression() {
        val ui = UiContext()
        ui.beginFrame(240f, 100f, testSnapshot())

        var first: UiButtonResult? = null
        var second: UiButtonResult? = null

        ui.pushFont(BitmapFont())
        ui.column(
            modifier = Modifier.offset(10f.dp, 20f.dp).width(220f.dp).height(80f.dp),
            verticalArrangement = Arrangement.spacedBy(0f.dp)
        ) {
            row( horizontalArrangement = Arrangement.spacedBy(4f.dp), modifier = Modifier.height(30f.dp)) {
                first = buttonSlot(
                    id = "one",
                    label = "One",
                    modifier = Modifier.width(60f.px).height(30f.px)
                )
                spacer(Modifier.width(12f.dp))
                second = buttonSlot(
                    id = "two",
                    label = "Two",
                    modifier = Modifier.width(60f.px).height(30f.px)
                )
            }
        }

        val firstSlot = assertNotNull(first).slot
        val secondSlot = assertNotNull(second).slot
        assertEquals(UiBounds(10f, 20f, 60f, 30f), firstSlot)
        assertEquals(UiBounds(90f, 20f, 60f, 30f), secondSlot)
    }
}
