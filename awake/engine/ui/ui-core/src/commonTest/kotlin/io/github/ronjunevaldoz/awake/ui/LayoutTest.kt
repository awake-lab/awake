// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
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
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.style.Style

class LayoutTest {

    @Test
    fun rowWeightIsNotStarvedByFixedSurfaceSiblingsWithRealContent() {
        // Reproduces samples/scene3d-playground's live shell bug: fixed surface() sibling |
        // weight(1f) column | fixed surface() sibling, where the fixed surface() siblings have
        // real content of their own (not empty `{}` bodies -- every pre-existing weight() test
        // either used raw claimSlot() calls or empty-content column()/surface() siblings, which
        // this shape's own root cause never triggers). Root cause: surface() (the shared widget
        // shadcnSidebar/shadcnSurface/shadcnCard all route through) didn't suppress its own
        // children's claimSlot() recording during its content dispatch the way row()/column()
        // already do for theirs (see UiContext.withMeasuredRecordingSuppressed) -- so once a
        // fixed surface() sibling with real content forced this row into its trial-measurement
        // (hasWeightedChild/plannedSlots) path, that content's own claims leaked into this row's
        // measuredSlots/measuredWeights/fillsMainAxis trial, desyncing resolveWeightedMainAxis()'s
        // index pairing and this row's plannedSlots consumption order.
        val ui = UiContext()
        ui.beginFrame(1200f, 800f, testSnapshot())
        var sidebar: UiBounds? = null
        var viewport: UiBounds? = null
        var controls: UiBounds? = null
        val visualStyle = Style { background(io.github.ronjunevaldoz.awake.core.colors.Color(0f, 0f, 0f, 1f)) }

        ui.createColumn(x = 0f, y = 0f, width = 1200f, height = 800f).row(
            id = "shell",
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)
        ) {
            sidebar = surface(id = "sidebar", style = visualStyle, modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.FillMax)) {
                claimSlot(Dimension.Fixed(60f.px), Dimension.Fixed(20f.px))
            }
            viewport = column(id = "viewport", modifier = Modifier.weight(1f).height(Dimension.FillMax)) {
                claimSlot(Dimension.Fixed(90f.px), Dimension.Fixed(20f.px))
            }
            controls = surface(id = "controls", style = visualStyle, modifier = Modifier.width(Dimension.Fixed(220f.px)).height(Dimension.FillMax)) {
                claimSlot(Dimension.Fixed(70f.px), Dimension.Fixed(20f.px))
            }
        }
        ui.endFrame()

        assertEquals(200f, sidebar?.width)
        assertEquals(0f, sidebar?.x)
        assertEquals(220f, controls?.width, "a fixed sibling's own width must not be starved by a leaked grandchild slot")
        assertEquals(1200f - 220f, controls?.x, "controls must sit flush against the row's right edge, not overlap the viewport")
        assertEquals(1200f - 420f, viewport?.width, "the weighted middle child must get the real remaining share, not a leaked grandchild's slot")
        assertEquals(200f, viewport?.x)
    }

    @Test
    fun columnScopeAdvancesCursorByHeightPlusGap() {
        val ui = UiContext()
        val column = ui.createColumn(x = 10f, y = 20f, width = 100f, verticalArrangement = Arrangement.spacedBy(8f.px))

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
        val row = ui.createRow(x = 10f, y = 20f, height = 32f, horizontalArrangement = Arrangement.spacedBy(8f.px))

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
        var first: UiBounds? = null
        var second: UiBounds? = null

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
        var first: UiBounds? = null
        var second: UiBounds? = null

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
    fun rowVerticalAlignmentCentersMismatchedHeightChildrenWithoutPerChildAlign() {
        val ui = UiContext()
        ui.beginFrame(240f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 240f)
        var tall: UiBounds? = null
        var short: UiBounds? = null

        root.row(
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.width(Dimension.Fixed(240f.px)).height(Dimension.Fixed(60f.px))
        ) {
            tall = claimModifiedSlot(Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(60f.px)))
            short = claimModifiedSlot(Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(20f.px)))
        }

        assertEquals(0f, tall?.y, "a full-height child has no slack left to center within")
        assertEquals(20f, short?.y, "a 20px-tall child in a 60px row centers to (60 - 20) / 2 = 20")
    }

    @Test
    fun rowChildExplicitAlignOverridesContainerVerticalAlignment() {
        val ui = UiContext()
        ui.beginFrame(240f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 240f)
        var overridden: UiBounds? = null

        root.row(
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.width(Dimension.Fixed(240f.px)).height(Dimension.Fixed(60f.px))
        ) {
            overridden = claimModifiedSlot(
                Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(20f.px)).align(UiAlignment.BottomStart)
            )
        }

        assertEquals(40f, overridden?.y, "an explicit per-child .align() must win over the row's own default")
    }

    @Test
    fun columnHorizontalAlignmentCentersMismatchedWidthChildrenWithoutPerChildAlign() {
        val ui = UiContext()
        ui.beginFrame(120f, 240f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 120f)
        var wide: UiBounds? = null
        var narrow: UiBounds? = null

        root.column(
            horizontalAlignment = UiAlignment.Horizontal.Center,
            modifier = Modifier.width(Dimension.Fixed(120f.px)).height(Dimension.Fixed(80f.px))
        ) {
            wide = claimModifiedSlot(Modifier.width(Dimension.Fixed(120f.px)).height(Dimension.Fixed(20f.px)))
            narrow = claimModifiedSlot(Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(20f.px)))
        }

        assertEquals(0f, wide?.x, "a full-width child has no slack left to center within")
        assertEquals(40f, narrow?.x, "a 40px-wide child in a 120px column centers to (120 - 40) / 2 = 40")
    }

    @Test
    fun columnChildExplicitAlignOverridesContainerHorizontalAlignment() {
        val ui = UiContext()
        ui.beginFrame(120f, 240f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 120f)
        var overridden: UiBounds? = null

        root.column(
            horizontalAlignment = UiAlignment.Horizontal.Center,
            modifier = Modifier.width(Dimension.Fixed(120f.px)).height(Dimension.Fixed(80f.px))
        ) {
            overridden = claimModifiedSlot(
                Modifier.width(Dimension.Fixed(40f.px)).height(Dimension.Fixed(20f.px)).align(UiAlignment.TopEnd)
            )
        }

        assertEquals(80f, overridden?.x, "an explicit per-child .align() must win over the column's own default")
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

        assertEquals(UiBounds(40f, 40f, 40f, 20f), slot)
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

        assertEquals(UiBounds(68f, 53f, 40f, 20f), slot)
    }

    @Test
    fun columnFactoryFromSlotAppliesInsets() {
        val ui = UiContext()
        val column = ui.createColumn(UiBounds(10f, 20f, 100f, 80f), insets = UiInsets(4f.dp, 6f.dp))

        val slot = column.claimSlot(Dimension.FillMax, Dimension.Fixed(20f.px))

        assertEquals(14f, slot.x)
        assertEquals(26f, slot.y)
        assertEquals(92f, slot.width)
    }

    @Test
    fun uiScopeColumnHelperOpensNestedColumnFromClaimedSlot() {
        val ui = UiContext()
        val box = ui.createBox(x = 0f, y = 0f, width = 240f, height = 120f)
        var nestedChild: UiBounds? = null

        box.column(slot = UiBounds(20f, 30f, 100f, 60f)) {
            nestedChild = claimSlot(Dimension.FillMax, Dimension.Fixed(18f.px))
        }

        assertEquals(UiBounds(20f, 30f, 100f, 18f), nestedChild)
    }

    @Test
    fun scrollableColumnInRowPreservesRequestedFixedWidth() {
        val ui = UiContext()
        ui.beginFrame(800f, 600f, testSnapshot())

        var sidebarSlot: UiBounds? = null
        var contentSlot: UiBounds? = null

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

        renderScrollableNestedSurface { ui, content ->
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

        renderScrollableNestedSurface { ui, content ->
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

        renderScrollableNestedSurface { ui, content ->
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

        renderScrollableNestedSurface { ui, content ->
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
        var first: UiBounds? = null
        var second: UiBounds? = null

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
        var first: UiBounds? = null
        var second: UiBounds? = null

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
        var fixed: UiBounds? = null
        var weighted: UiBounds? = null

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
    fun rowWeightIsNotStarvedByNonWeightedFillMaxSibling() {
        // Reproduces the root cause behind the shadcnField*/checkout-form-grid/shadcnToggleGroup
        // workarounds this session: a plain (non-weighted) FillMax sibling must not claim the
        // row's full width as "occupied" before the weighted sibling gets its share -- see
        // resolveWeightedMainAxis() in Arrangement.kt.
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var weighted: UiBounds? = null
        var fillMax: UiBounds? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            weighted = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
            fillMax = claimSlot(Dimension.FillMax, Dimension.FillMax)
        }

        assertTrue(
            (weighted?.width ?: 0f) > 100f,
            "weighted sibling must get a reasonable, non-zero share of the row, got ${weighted?.width}"
        )
    }

    @Test
    fun rowWeightIsNotStarvedByNonWeightedFillMaxSiblingRegardlessOfOrder() {
        // Reverse-order case: the non-weighted FillMax sibling comes first. Fix must not be
        // order-dependent.
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var fillMax: UiBounds? = null
        var weighted: UiBounds? = null

        root.row(
            horizontalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(32f.px))
        ) {
            fillMax = claimSlot(Dimension.FillMax, Dimension.FillMax)
            weighted = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
        }

        assertTrue(
            (weighted?.width ?: 0f) > 100f,
            "weighted sibling must get a reasonable, non-zero share regardless of sibling order, got ${weighted?.width}"
        )
    }

    @Test
    fun rowWeightFillFalseLetsChildUseLessThanItsAllottedShare() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 200f)
        var filled: UiBounds? = null
        var unfilled: UiBounds? = null

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
        var first: UiBounds? = null
        var second: UiBounds? = null

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
        var first: UiBounds? = null
        var second: UiBounds? = null

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

    @Test
    fun rowWrapContentHeightWithWeightedWrapContentColumnsSplitsWidthEvenlyAndHugsTallestChild() {
        // Reproduces task #30: a WrapContent-height row containing several weight(1f)-tagged
        // WrapContent-height columns, each wrapping a small "label + control" shape (mimicking
        // shadcnField's label+input). Every child column is the same shape/height on purpose --
        // if the bug regresses, sibling columns after the first collapse toward/under a zero
        // width bound during trial measurement, which both corrupts their width share and risks
        // a negative-size crash in real widgets (e.g. a surface subtracting border/padding from
        // a near-zero measured width).
        val ui = UiContext()
        ui.beginFrame(300f, 200f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 300f)
        val columnSlots = mutableListOf<UiBounds>()
        var rowSlot: UiBounds? = null

        rowSlot = root.row(
            horizontalArrangement = Arrangement.spacedBy(16f.px),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)
        ) {
            // This content lambda re-runs across trial-measurement passes before the real,
            // final render -- only the last invocation's slots reflect real placement, so start
            // each pass with a clean list rather than accumulating throwaway trial results too.
            columnSlots.clear()
            repeat(3) { index ->
                columnSlots += column(
                    id = "field-$index",
                    modifier = Modifier.weight(1f).height(Dimension.WrapContent)
                ) {
                    surface(id = "label-$index", modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(16f.px))) {}
                    surface(id = "input-$index", modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(32f.px))) {}
                }
            }
        }

        val gap = 16f
        val expectedColumnWidth = (300f - gap * 2) / 3f
        columnSlots.forEach { slot ->
            assertTrue(slot.width > 0f, "weighted column width must be positive, was ${slot.width}")
            assertEquals(expectedColumnWidth, slot.width, 0.01f, "each weighted column must get an equal 1/3 share")
        }

        // Tallest (and only) child shape is 16px label + default sm gap + 32px input.
        val expectedRowHeight = 16f + UiSpacing.sm.toPx() + 32f
        assertEquals(expectedRowHeight, rowSlot.height, 0.01f, "WrapContent row height must hug the tallest child's real content height")
    }

    @Test
    fun nestedWrapContentColumnsWithNoWeightedChildrenReuseTheSizingTrialInsteadOfRetrialingWeight() {
        // Task #34 (checkout-form "laggy" report): a plain (non-weighted) WrapContent column
        // nested N levels deep -- e.g. shadcnFieldGroup > shadcnFieldSet > shadcnFieldGroup >
        // shadcnField -- used to pay for 3 full re-executions of [content] per level (the
        // resolveMeasuredColumn() sizing trial, UiScope.column()'s own unconditional
        // weight-detection trial, and the real render), compounding to 3^N leaf calls for a
        // single real leaf widget. resolveMeasuredColumn() now hands its already-computed trial
        // to column() so the redundant second trial is skipped whenever there's no weighted
        // child to justify it -- cutting this to 2^N. Regression guard: 4 levels must invoke the
        // leaf content exactly 16 times (2^4), not 81 (3^4).
        val ui = UiContext()
        ui.beginFrame(400f, 800f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 400f)
        var leafCount = 0

        root.column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
            column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                    column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                        leafCount++
                        surface(
                            id = "leaf-$leafCount",
                            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(16f.px))
                        ) {}
                    }
                }
            }
        }

        assertEquals(16, leafCount, "4 levels of plain WrapContent column nesting must cost 2^4 leaf calls, not 3^4")
    }

    @Test
    fun wrapContentHeightRowWithWeightedColumnAndNoExplicitHeightHugsRealContentNotTheSizingTrialBound() {
        // Task #34 (checkout-form "big blank gap" report): a WrapContent-height row containing a
        // weight(1f)-tagged column() with NO explicit .height(...) -- exactly
        // `row(...) { column(modifier = Modifier.weight(1f)) { ... } }`, the checkout form's real
        // Month/Year/CVV grid shape -- used to report a WrapContent row height of ~4096px (the
        // row-sizing trial's arbitrary upper-bound placeholder, see UiScope.row()) instead of the
        // real ~40px content height. RowScope.column() defaults every column's height to FillMax
        // (stretch to the row's cross axis); during the row's own WrapContent-height sizing
        // trial, that FillMax resolved against the trial's placeholder bound, and (with no
        // height-side counterpart to measuredMaxRightExcludingFill) leaked straight into the
        // row's own measured height. Regression guard: this row must hug its one real 40px-tall
        // child, not the sizing trial's placeholder.
        val ui = UiContext()
        ui.beginFrame(300f, 2000f, testSnapshot())
        val root = ui.createColumn(x = 0f, y = 0f, width = 300f)

        val rowSlot = root.row(modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
            repeat(3) { index ->
                column(id = "grid-cell-$index", modifier = Modifier.weight(1f)) {
                    surface(
                        id = "control-$index",
                        modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(40f.px))
                    ) {}
                }
            }
        }

        assertEquals(40f, rowSlot.height, 0.01f, "WrapContent row must hug its real 40px content, not the sizing trial's placeholder bound")
    }
}

private fun renderScrollableNestedSurface(renderParent: (UiContext, ColumnContent) -> Unit) {
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
