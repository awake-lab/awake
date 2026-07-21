// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutTest {

    @Test
    fun columnScopeAdvancesCursorByHeightPlusGap() {
        val ui = UiContext()
        val column = ui.createColumn(x = 10f, y = 20f, width = 100f, gap = 8f)

        val first = column.claimSlot(Dimension.Fixed(100f.px), Dimension.Fixed(32f.px))
        assertEquals(10f, first.x)
        assertEquals(20f, first.y)
        assertEquals(32f, first.height)

        val second = column.claimSlot(Dimension.Fixed(100f.px), Dimension.Fixed(28f.px))
        assertEquals(10f, second.x)
        assertEquals(20f + 32f + 8f, second.y, "second slot must start after the first row's height + gap")

        val third = column.claimSlot(Dimension.Fixed(100f.px), Dimension.Fixed(40f.px))
        assertEquals(second.y + 28f + 8f, third.y)
    }

    @Test
    fun columnScopeFillMaxUsesConfiguredWidth() {
        val ui = UiContext()
        val column = ui.createColumn(x = 0f, y = 0f, width = 200f)
        val slot = column.claimSlot(Dimension.FillMax, Dimension.Fixed(32f.px))
        assertEquals(200f, slot.width, "FillMax must resolve to the column's own configured width")
    }

    @Test
    fun absoluteScopeReturnsSameRectRegardlessOfRequestedSize() {
        val ui = UiContext()
        val scope = ui.createAbsolute(x = 15f, y = 25f)

        val first = scope.claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(50f.px))
        assertEquals(15f, first.x)
        assertEquals(25f, first.y)

        val second = scope.claimSlot(Dimension.Fixed(999f.px), Dimension.Fixed(999f.px))
        assertEquals(15f, second.x, "AbsoluteScope must always return the exact x/y it was constructed with")
        assertEquals(25f, second.y)
    }

    @Test
    fun absoluteScopeFillMaxResolvesToZeroInsteadOfThrowing() {
        // Regression test: AbsoluteScope has no configured width/height to fill, but its
        // original claimSlot(width: Float, ...) never rejected 0f either -- it passed it
        // straight through (harmless when a caller doesn't need a real slot size, e.g.
        // text()'s own default slot for a standalone, non-centered HUD label). FillMax must
        // resolve the same way (0f), not throw, or every UiScope.text() call on an
        // AbsoluteScope -- exactly DemoCatalog's debug HUD -- crashes at runtime.
        val ui = UiContext()
        val scope = ui.createAbsolute(x = 15f, y = 25f)
        val slot = scope.claimSlot(Dimension.FillMax, Dimension.FillMax)
        assertEquals(0f, slot.width)
        assertEquals(0f, slot.height)
    }

    @Test
    fun rowScopeAdvancesCursorByWidthPlusGap() {
        val ui = UiContext()
        val row = ui.createRow(x = 10f, y = 20f, height = 32f, gap = 8f)

        val first = row.claimSlot(Dimension.Fixed(50f.px), Dimension.Fixed(32f.px))
        assertEquals(10f, first.x)
        assertEquals(20f, first.y)

        val second = row.claimSlot(Dimension.Fixed(60f.px), Dimension.Fixed(32f.px))
        assertEquals(10f + 50f + 8f, second.x, "second slot must start after the first column's width + gap")

        val third = row.claimSlot(Dimension.Fixed(40f.px), Dimension.Fixed(32f.px))
        assertEquals(second.x + 60f + 8f, third.x)
    }

    @Test
    fun rowScopeFillMaxUsesConfiguredHeight() {
        val ui = UiContext()
        val row = ui.createRow(x = 0f, y = 0f, height = 40f)
        val slot = row.claimSlot(Dimension.Fixed(50f.px), Dimension.FillMax)
        assertEquals(40f, slot.height, "FillMax must resolve to the row's own configured height")
    }

    @Test
    fun boxScopeReturnsSameRectRegardlessOfRequestedSize() {
        val ui = UiContext()
        val box = ui.createBox(x = 5f, y = 5f, width = 100f, height = 50f)

        val first = box.claimSlot(Dimension.Fixed(999f.px), Dimension.Fixed(999f.px))
        assertEquals(5f, first.x)
        assertEquals(5f, first.y)
        assertEquals(100f, first.width, "BoxScope must always return its own configured width, ignoring the requested size")
        assertEquals(50f, first.height)

        val second = box.claimSlot(Dimension.Fixed(1f.px), Dimension.Fixed(1f.px))
        assertEquals(first, second, "every claimSlot call on a BoxScope must return the identical rect")
    }

    @Test
    fun claimModifiedSlotCentersContentInsideBox() {
        val ui = UiContext()
        val box = ui.createBox(x = 10f, y = 20f, width = 100f, height = 60f, contentAlignment = UiAlignment.Center)

        val slot = box.claimModifiedSlot(
            defaultWidth = Dimension.Fixed(40f.px),
            defaultHeight = Dimension.Fixed(20f.px)
        )

        assertEquals(UiSlot(40f, 40f, 40f, 20f), slot)
    }

    @Test
    fun claimModifiedSlotAppliesPaddingAndOffsetAfterAlignment() {
        val ui = UiContext()
        val box = ui.createBox(x = 0f, y = 0f, width = 120f, height = 80f, contentAlignment = UiAlignment.BottomEnd)

        val slot = box.claimModifiedSlot(
            defaultWidth = Dimension.Fixed(40f.px),
            defaultHeight = Dimension.Fixed(20f.px),
            modifier = UiModifier()
                .padding(start = 8f.dp, top = 6f.dp, end = 10f.dp, bottom = 4f.dp)
                .offset(x = (-2f).dp, y = (-3f).dp)
        )

        assertEquals(UiSlot(68f, 53f, 40f, 20f), slot)
    }

    @Test
    fun columnFactoryFromSlotAppliesInsets() {
        val ui = UiContext()
        val column = ui.createColumn(UiSlot(10f, 20f, 100f, 80f), insets = UiInsets(4f.dp, 6f.dp))

        val slot = column.claimSlot(Dimension.FillMax, Dimension.Fixed(20f.px))

        assertEquals(14f, slot.x)
        assertEquals(26f, slot.y)
        assertEquals(92f, slot.width)
    }
}
