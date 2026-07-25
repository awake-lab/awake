// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.testTag
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
    fun rowSpaceBetweenDistributesChildrenAcrossRemainingWidth() {
        val ui = UiContext()
        ui.beginFrame(240f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 240f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.SpaceBetween
        , modifier = Modifier.width(Dimension.Fixed(240f.px)).height(Dimension.Fixed(32f.px))) {
            first = claimSlot(Dimension.Fixed(40f.px), Dimension.FillMax)
            second = claimSlot(Dimension.Fixed(60f.px), Dimension.FillMax)
        }

        assertEquals(0f, first?.x)
        assertEquals(180f, second?.x)
    }

    @Test
    fun columnSpaceEvenlyAddsLeadingAndBetweenSpace() {
        val ui = UiContext()
        ui.beginFrame(120f, 200f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 120f, height = 200f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.column(
            verticalArrangement = Arrangement.SpaceEvenly
        , modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(200f.px))) {
            first = claimSlot(Dimension.FillMax, Dimension.Fixed(20f.px))
            second = claimSlot(Dimension.FillMax, Dimension.Fixed(20f.px))
        }

        assertEquals(160f / 3f, first?.y ?: -1f, 0.001f)
        assertEquals((160f / 3f) * 2f + 20f, second?.y ?: -1f, 0.001f)
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
            Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(20f.px))
        )

        assertEquals(UiSlot(40f, 40f, 40f, 20f), slot)
    }

    @Test
    fun claimModifiedSlotAppliesPaddingAndOffsetAfterAlignment() {
        val ui = UiContext()
        val box = ui.createBox(x = 0f, y = 0f, width = 120f, height = 80f, contentAlignment = UiAlignment.BottomEnd)

        val slot = box.claimModifiedSlot(
            Modifier
                .width(Dimension.Fixed(40f.px))
                .height(Dimension.Fixed(20f.px))
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

    @Test
    fun uiScopeColumnHelperOpensNestedColumnFromClaimedSlot() {
        val ui = UiContext()
        val box = ui.createBox(x = 0f, y = 0f, width = 240f, height = 120f)
        var nestedChild: UiSlot? = null

        box.column(slot = UiSlot(20f, 30f, 100f, 60f)) {
            nestedChild = claimSlot(Dimension.FillMax, Dimension.Fixed(18f.px))
        }

        assertEquals(UiSlot(20f, 30f, 100f, 18f), nestedChild)
    }

    @Test
    fun scrollableColumnInRowPreservesRequestedFixedWidth() {
        val ui = UiContext()
        ui.beginFrame(800f, 600f, testSnapshot())

        var sidebarSlot: UiSlot? = null
        var contentSlot: UiSlot? = null

        ui.createBox(x = 0f, y = 0f, width = 800f, height = 600f).row(
            horizontalArrangement = Arrangement.spacedBy(20f.px)
        , modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) {
            sidebarSlot = column(
                id = "sidebar",
                modifier = (Modifier.verticalScroll(UiScrollState(), UiScrollConfig.Hidden)).width(264f.toDimension()).height(
                    Dimension.FillMax)) { }
            contentSlot = column(
                id = "content", modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) { }
        }

        assertEquals(264f, sidebarSlot?.width)
        assertEquals(284f, contentSlot?.x)
        assertEquals(516f, contentSlot?.width)
    }

    @Test
    fun scrollableColumnInRowDetectsOverflowThroughNestedWrapContentSurface() {
        val scrollState = UiScrollState()

        renderScrollableNestedSurface(scrollState) { ui, content ->
            ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).row(
                horizontalArrangement = Arrangement.spacedBy(16f.px)
            , modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) {
                surface(
                    id = "sidebar", modifier = Modifier.width(Dimension.Fixed(220f.px)).height(
                        Dimension.FillMax)) { }
                column(
                    id = "content-viewport",
                    modifier = (Modifier.verticalScroll(scrollState)).width(Dimension.FillMax).height(
                        Dimension.FillMax)) {
                    content()
                }
            }
        }

        assertTrue(scrollState.canScrollY, "a row-hosted scrollable column should detect overflow through a nested WrapContent surface")
        assertTrue(scrollState.contentHeight > scrollState.viewportHeight)
    }

    @Test
    fun scrollableColumnInColumnDetectsOverflowThroughNestedWrapContentSurface() {
        val scrollState = UiScrollState()

        renderScrollableNestedSurface(scrollState) { ui, content ->
            ui.createColumn(x = 0f, y = 0f, width = 920f, height = 620f).column(
                id = "content-viewport",
                modifier = (Modifier.verticalScroll(scrollState)).width(Dimension.FillMax).height(
                    Dimension.FillMax)) {
                content()
            }
        }

        assertTrue(scrollState.canScrollY, "a column-hosted scrollable column should detect overflow through a nested WrapContent surface")
        assertTrue(scrollState.contentHeight > scrollState.viewportHeight)
    }

    @Test
    fun scrollableColumnInBoxDetectsOverflowThroughNestedWrapContentSurface() {
        val scrollState = UiScrollState()

        renderScrollableNestedSurface(scrollState) { ui, content ->
            ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).column(
                id = "content-viewport",
                modifier = (Modifier.verticalScroll(scrollState)).width(Dimension.FillMax).height(
                    Dimension.FillMax)) {
                content()
            }
        }

        assertTrue(scrollState.canScrollY, "a box-hosted scrollable column should detect overflow through a nested WrapContent surface")
        assertTrue(scrollState.contentHeight > scrollState.viewportHeight)
    }

    @Test
    fun scrollableColumnInAbsoluteDetectsOverflowThroughNestedWrapContentSurface() {
        val scrollState = UiScrollState()

        renderScrollableNestedSurface(scrollState) { ui, content ->
            ui.createAbsolute(x = 0f, y = 0f).column(
                id = "content-viewport",
                modifier = (Modifier.verticalScroll(scrollState)).width(Dimension.Fixed(920f.px)).height(
                    Dimension.Fixed(620f.px))) {
                content()
            }
        }

        assertTrue(scrollState.canScrollY, "an absolute-hosted scrollable column should detect overflow through a nested WrapContent surface")
        assertTrue(scrollState.contentHeight > scrollState.viewportHeight)
    }

    @Test
    fun scrollableColumnFillMaxInUnboundedParentFailsLoudlyWithParentName() {
        val ui = UiContext()
        ui.beginFrame(920f, 620f, testSnapshot())

        val error = assertFailsWith<IllegalStateException> {
            ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).surface(
                id = "surface-semantic",
                modifier = (Modifier.testTag("preview-root")).width(Dimension.FillMax).height(
                    Dimension.WrapContent)) {
                column(
                    id = "content-viewport",
                    modifier = (Modifier.verticalScroll(UiScrollState())).width(Dimension.FillMax).height(
                        Dimension.FillMax)) {
                    repeat(20) { index ->
                        surface(
                            id = "content-row-$index", modifier = Modifier.width(Dimension.FillMax).height(
                                Dimension.Fixed(36f.px))) { }
                    }
                }
            }
        }

        assertTrue(error.message.orEmpty().contains("content-viewport"))
        assertTrue(error.message.orEmpty().contains("preview-root"))
        assertTrue(!error.message.orEmpty().contains("surface-semantic"))
    }

    @Test
    fun rowWeightSplitsRemainingSpaceEvenlyForEqualWeights() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            first = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
            second = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
        }

        assertEquals(100f, first?.width)
        assertEquals(0f, first?.x)
        assertEquals(100f, second?.width)
        assertEquals(100f, second?.x)
    }

    @Test
    fun rowWeightSplitsRemainingSpaceProportionallyForUnequalWeights() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            first = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
            second = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(3f))
        }

        assertEquals(50f, first?.width)
        assertEquals(150f, second?.width)
        assertEquals(50f, second?.x)
    }

    @Test
    fun rowWeightReservesFixedSiblingSpaceFirst() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var fixed: UiSlot? = null
        var weighted: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            fixed = claimSlot(Dimension.Fixed(50f.px), Dimension.FillMax)
            weighted = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
        }

        assertEquals(50f, fixed?.width)
        assertEquals(150f, weighted?.width, "weighted child must only get the space left after the fixed sibling")
        assertEquals(50f, weighted?.x)
    }

    @Test
    fun rowWeightFillFalseLetsChildUseLessThanItsAllottedShare() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var filled: UiSlot? = null
        var unfilled: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            filled = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f, fill = true))
            unfilled = claimSlot(Dimension.Fixed(30f.px), Dimension.FillMax, LayoutWeight(1f, fill = false))
        }

        assertEquals(100f, filled?.width, "fill=true still claims its full 100px share of the 200px row")
        assertEquals(30f, unfilled?.width, "fill=false must cap the child at its own requested size, not the full 100px share")
        assertEquals(100f, unfilled?.x)
    }

    @Test
    fun columnWeightSplitsRemainingSpaceEvenlyForEqualWeights() {
        val ui = UiContext()
        ui.beginFrame(80f, 200f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 80f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.column(
            verticalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(200f.px))
        ) {
            first = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
            second = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
        }

        assertEquals(100f, first?.height)
        assertEquals(0f, first?.y)
        assertEquals(100f, second?.height)
        assertEquals(100f, second?.y)
    }

    @Test
    fun rowModifierWeightWiresThroughClaimModifiedSlot() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var first: UiSlot? = null
        var second: UiSlot? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            first = claimModifiedSlot(Modifier.weight(1f).height(Dimension.FillMax))
            second = claimModifiedSlot(Modifier.weight(1f).height(Dimension.FillMax))
        }

        assertEquals(100f, first?.width)
        assertEquals(100f, second?.width)
        assertEquals(100f, second?.x)
    }
}

private fun renderScrollableNestedSurface(
    scrollState: UiScrollState,
    renderParent: (UiContext, ColumnContent) -> Unit
) {
    val ui = UiContext()
    ui.beginFrame(920f, 620f, testSnapshot())

    val content: ColumnContent = {
        surface(
            id = "content-card", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
            repeat(20) { index ->
                surface(
                    id = "content-row-$index", modifier = Modifier.width(Dimension.FillMax).height(
                        Dimension.Fixed(36f.px))) { }
            }
        }
    }

    renderParent(ui, content)
    ui.endFrame()
}

private typealias ColumnContent = io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope.() -> Unit
