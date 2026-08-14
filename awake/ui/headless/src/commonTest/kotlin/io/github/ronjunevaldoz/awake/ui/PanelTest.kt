// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PanelTest {

    @Test
    fun panelDoesNotDisturbParentLayoutCursor() {
        val ui = UiContext()
        val column = ui.createColumn(x = 10f, y = 20f, width = 200f)

        val panelSlot = column.surface(
            "p",
            modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px)),
        ) { }
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
        val column = ui.createColumn(x = 10f, y = 20f, width = 200f)

        var firstChildSlot: UiBounds? = null
        column.surface(
            "p",
            modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px)),
        ) { slot ->
            firstChildSlot = claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(20f.px))
        }

        val childSlot = requireNotNull(firstChildSlot)
        assertEquals(
            10f + UiSpacing.sm.toPx(),
            childSlot.x,
            "nested content must start after the panel's content padding",
        )
        assertEquals(
            20f + UiSpacing.sm.toPx(),
            childSlot.y,
            "nested content must start after the panel's content padding",
        )
    }

    @Test
    fun zeroRadiusEmitsPlainQuad() {
        val ui = UiContext()
        val column = ui.createColumn(x = 0f, y = 0f, width = 200f)
        column.surface(
            "p",
            modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px)),
            style = Style {
                shape(UiShape.none)
            },
        ) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.Quad>(
            primitives.first(),
            "radius = UiShape.none must emit a plain Quad, not a RoundedQuad",
        )
    }

    @Test
    fun nonZeroRadiusEmitsRoundedQuad() {
        val ui = UiContext()
        val column = ui.createColumn(x = 0f, y = 0f, width = 200f)
        column.surface(
            "p",
            modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(100f.px)),
        ) { }
        val primitives = ui.endFrame()
        assertTrue(primitives.isNotEmpty())
        assertIs<UiDrawPrimitive.RoundedQuad>(
            primitives.first(),
            "a non-zero radius must emit a RoundedQuad",
        )
    }

    @Test
    fun wrapContentHeightTracksChildContent() {
        val ui = UiContext()
        val font = io.github.ronjunevaldoz.awake.ui.font.BitmapFont()
        ui.pushFont(font)
        val column = ui.createColumn(x = 0f, y = 0f, width = 220f)

        val panelSlot = column.surface(
            id = "wrap-height",
            modifier = Modifier.width(Dimension.Fixed(180f.px)).height(Dimension.WrapContent),
        ) {
            text("Line One")
            text("Line Two")
        }

        assertEquals(
            56f,
            panelSlot.height,
            "two text rows + 8px row gap + 16px panel padding should size the panel",
        )
    }

    @Test
    fun wrapContentWidthTracksChildContent() {
        val ui = UiContext()
        val font = io.github.ronjunevaldoz.awake.ui.font.BitmapFont()
        ui.pushFont(font)
        val column = ui.createColumn(x = 0f, y = 0f, width = 220f)

        val panelSlot = column.surface(
            id = "wrap-width",
            modifier = Modifier.width(Dimension.WrapContent).height(Dimension.WrapContent),
        ) {
            claimSlot(Dimension.Fixed(32f.px), Dimension.Fixed(8f.px))
        }

        assertEquals(
            48f,
            panelSlot.width,
            "four 8px glyphs plus 16px panel padding should size the panel width",
        )
        assertEquals(
            24f,
            panelSlot.height,
            "one 8px child row plus 16px panel padding should size the panel height",
        )
    }
}
