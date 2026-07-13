// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutTest {

    @Test
    fun columnScopeAdvancesCursorByHeightPlusGap() {
        val ui = UiContext()
        val column = ui.column(x = 10f, y = 20f, width = 100f, gap = 8f)

        val first = column.claimSlot(100f, 32f)
        assertEquals(10f, first.x)
        assertEquals(20f, first.y)
        assertEquals(32f, first.height)

        val second = column.claimSlot(100f, 28f)
        assertEquals(10f, second.x)
        assertEquals(20f + 32f + 8f, second.y, "second slot must start after the first row's height + gap")

        val third = column.claimSlot(100f, 40f)
        assertEquals(second.y + 28f + 8f, third.y)
    }

    @Test
    fun columnScopeFallsBackToConfiguredWidthWhenZero() {
        val ui = UiContext()
        val column = ui.column(x = 0f, y = 0f, width = 200f)
        val slot = column.claimSlot(0f, 32f)
        assertEquals(200f, slot.width, "a zero/unset width hint should fall back to the column's own configured width")
    }

    @Test
    fun absoluteScopeReturnsSameRectRegardlessOfRequestedSize() {
        val ui = UiContext()
        val scope = ui.absolute(x = 15f, y = 25f)

        val first = scope.claimSlot(50f, 50f)
        assertEquals(15f, first.x)
        assertEquals(25f, first.y)

        val second = scope.claimSlot(999f, 999f)
        assertEquals(15f, second.x, "AbsoluteScope must always return the exact x/y it was constructed with")
        assertEquals(25f, second.y)
    }

    @Test
    fun rowScopeAdvancesCursorByWidthPlusGap() {
        val ui = UiContext()
        val row = ui.row(x = 10f, y = 20f, height = 32f, gap = 8f)

        val first = row.claimSlot(50f, 32f)
        assertEquals(10f, first.x)
        assertEquals(20f, first.y)

        val second = row.claimSlot(60f, 32f)
        assertEquals(10f + 50f + 8f, second.x, "second slot must start after the first column's width + gap")

        val third = row.claimSlot(40f, 32f)
        assertEquals(second.x + 60f + 8f, third.x)
    }

    @Test
    fun rowScopeFallsBackToConfiguredHeightWhenZero() {
        val ui = UiContext()
        val row = ui.row(x = 0f, y = 0f, height = 40f)
        val slot = row.claimSlot(50f, 0f)
        assertEquals(40f, slot.height, "a zero/unset height hint should fall back to the row's own configured height")
    }

    @Test
    fun boxScopeReturnsSameRectRegardlessOfRequestedSize() {
        val ui = UiContext()
        val box = ui.box(x = 5f, y = 5f, width = 100f, height = 50f)

        val first = box.claimSlot(999f, 999f)
        assertEquals(5f, first.x)
        assertEquals(5f, first.y)
        assertEquals(100f, first.width, "BoxScope must always return its own configured width, ignoring the requested size")
        assertEquals(50f, first.height)

        val second = box.claimSlot(1f, 1f)
        assertEquals(first, second, "every claimSlot call on a BoxScope must return the identical rect")
    }
}
