// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class PanelTest {

    @Test
    fun panelDoesNotDisturbParentLayoutCursor() {
        val ui = UiContext()
        val column = ui.createColumn(modifier = Modifier.offset((10f).dp, (20f).dp).width((200f).dp))

        val panelSlot = column.surface("p", modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px))) { }
        val next = column.claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(30f.px))

        assertEquals(10f, panelSlot.x)
        assertEquals(20f, panelSlot.y)
        // The parent column's cursor must have advanced by exactly the panel's own claimed
        // slot height + gap -- same as if `panel` were any other leaf widget occupying one row.
        assertEquals(panelSlot.y + panelSlot.height + 8f, next.y)
    }

    @Test
    fun contentLambdaScopeStartsAtPanelContentInset() {
        val ui = UiContext()
        val column = ui.createColumn(modifier = Modifier.offset((10f).dp, (20f).dp).width((200f).dp))

        var firstChildSlot: UiSlot? = null
        column.surface("p", modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px))) { slot ->
            firstChildSlot = claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(20f.px))
        }

        val childSlot = requireNotNull(firstChildSlot)
        assertEquals(10f + UiSpacing.sm.toPx(), childSlot.x, "nested content must start after the panel's content padding")
        assertEquals(20f + UiSpacing.sm.toPx(), childSlot.y, "nested content must start after the panel's content padding")
    }

    @Test
    fun zeroRadiusEmitsPlainQuad() {
        val ui = UiContext()
        val column = ui.createColumn(modifier = Modifier.offset((0f).dp, (0f).dp).width((200f).dp))
        column.surface("p", modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px)), style = Style {
            shape(UiShape.none)
        }) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.Quad>(primitives.first(), "radius = UiShape.none must emit a plain Quad, not a RoundedQuad")
    }

    @Test
    fun nonZeroRadiusEmitsRoundedQuad() {
        val ui = UiContext()
        val column = ui.createColumn(modifier = Modifier.offset((0f).dp, (0f).dp).width((200f).dp))
        column.surface("p", modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px))) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "a non-zero radius must emit a RoundedQuad")
    }

    @Test
    fun wrapContentHeightTracksChildContent() {
        val ui = UiContext()
        val font = io.github.ronjunevaldoz.awake.ui.font.BitmapFont()
        val column = ui.createColumn(modifier = Modifier.offset((0f).dp, (0f).dp).width((220f).dp), font = font)

        val panelSlot = column.surface(
            id = "wrap-height", modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.WrapContent)) {
            text("Line One")
            text("Line Two")
        }

        assertEquals(56f, panelSlot.height, "two text rows + 8px row gap + 16px panel padding should size the panel")
    }

    @Test
    fun wrapContentWidthTracksChildContent() {
        val ui = UiContext()
        val font = io.github.ronjunevaldoz.awake.ui.font.BitmapFont()
        val column = ui.createColumn(modifier = Modifier.offset((0f).dp, (0f).dp).width((220f).dp), font = font)

        val panelSlot = column.surface(
            id = "wrap-width", modifier = Modifier.width(Dimension.WrapContent).height(Dimension.WrapContent)) {
            claimSlot(Dimension.Fixed(32f.px), Dimension.Fixed(8f.px))
        }

        assertEquals(48f, panelSlot.width, "four 8px glyphs plus 16px panel padding should size the panel width")
        assertEquals(24f, panelSlot.height, "one 8px child row plus 16px panel padding should size the panel height")
    }
}
