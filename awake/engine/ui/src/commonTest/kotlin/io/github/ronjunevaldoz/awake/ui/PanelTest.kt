// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PanelTest {

    @Test
    fun panelDoesNotDisturbParentLayoutCursor() {
        val ui = UiContext()
        val column = ui.column(x = 10f, y = 20f, width = 200f)

        val panelSlot = column.panel("p", Dimension.Fixed(180f.px), Dimension.Fixed(100f.px)) { }
        val next = column.claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(30f.px))

        assertEquals(10f, panelSlot.x)
        assertEquals(20f, panelSlot.y)
        // The parent column's cursor must have advanced by exactly the panel's own claimed
        // slot height + gap -- same as if `panel` were any other leaf widget occupying one row.
        assertEquals(panelSlot.y + panelSlot.height + 8f, next.y)
    }

    @Test
    fun contentLambdaScopeStartsAtPanelSlotOrigin() {
        val ui = UiContext()
        val column = ui.column(x = 10f, y = 20f, width = 200f)

        var firstChildSlot: UiSlot? = null
        column.panel("p", Dimension.Fixed(180f.px), Dimension.Fixed(100f.px)) { slot ->
            firstChildSlot = claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(20f.px))
        }

        assertEquals(10f, firstChildSlot!!.x, "nested content must start at the panel slot's own x")
        assertEquals(20f, firstChildSlot!!.y, "nested content must start at the panel slot's own y")
    }

    @Test
    fun zeroRadiusEmitsPlainQuad() {
        val ui = UiContext()
        val column = ui.column(x = 0f, y = 0f, width = 200f)
        column.panel("p", Dimension.Fixed(180f.px), Dimension.Fixed(100f.px), radius = UiShape.none) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.Quad>(primitives.first(), "radius = UiShape.none must emit a plain Quad, not a RoundedQuad")
    }

    @Test
    fun nonZeroRadiusEmitsRoundedQuad() {
        val ui = UiContext()
        val column = ui.column(x = 0f, y = 0f, width = 200f)
        column.panel("p", Dimension.Fixed(180f.px), Dimension.Fixed(100f.px), radius = UiShape.md) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "a non-zero radius must emit a RoundedQuad")
    }
}
