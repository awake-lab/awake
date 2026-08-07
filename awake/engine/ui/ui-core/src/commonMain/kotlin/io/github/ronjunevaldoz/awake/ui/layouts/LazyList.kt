// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.onOverScrollable
import io.github.ronjunevaldoz.awake.ui.onScrollConsumed
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Virtualized, fixed-item-height list -- Awake's `LazyColumn`/`LazyRow` analog. Only items whose
 * bounds intersect the viewport (plus [overscan] items each direction) are composed/emitted this
 * frame; the rest never run [itemContent] at all.
 *
 * **Fixed height, not measure-and-cache**: unlike Compose's `LazyColumn` (which measures each
 * item's real height lazily), [itemHeight] is a single required, caller-supplied size applied to
 * every item. This was picked over a measure-and-cache-per-id design because Awake's `row()`/
 * `column()` already pay a "trial measure" pass before their real pass for `WrapContent`/weighted
 * content (see `docs/tasks/2026-08-02-trial-measure-double-execution.md`), and that cost
 * compounds badly with nested content. A fixed height means total content extent is `itemCount *
 * itemHeight` -- pure arithmetic, zero measurement of any item, on-screen or off. A per-item
 * measure-and-cache model would still have to measure every item at least once (on first
 * scroll-into-view) and would reintroduce exactly the trial-measure cost this list exists to
 * avoid for any item whose content isn't itself a single fixed-size leaf. If real variable-height
 * rows are needed later, that's a deliberate follow-up, not a default here.
 *
 * [key] mirrors Compose's `LazyColumn`'s `key` param and is threaded into [itemContent] so
 * per-item widgets can build stable nested ids (e.g. `checkbox(id = "todo-$key", ...)`) that
 * survive list reordering/filtering where [index] alone would not -- see the "Composite ids"
 * convention in `docs/reference/ui-ownership.md`. Virtualization itself introduces no new
 * state-corruption risk beyond what any non-lazy loop already has: Awake's immediate-mode
 * `WidgetState` is keyed purely by the id string a widget receives, and off-screen indices are
 * simply never composed (not recycled into a different index's slot), so nothing here reuses one
 * logical item's state for another the way a view-recycling list would.
 *
 * Requires a bounded, non-`WrapContent` height (via `Modifier.height(...)` or a `FillMax`
 * ancestor) and a [io.github.ronjunevaldoz.awake.ui.UiScrollState] via
 * `Modifier.verticalScroll(state)` -- reuses the existing scroll-offset machinery in
 * `ScrollContainers.kt`/`ScrollModifiers.kt` rather than inventing a parallel one.
 */
fun UiScope.lazyColumn(
    id: String,
    itemCount: Int,
    itemHeight: Dp,
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = defaultArrangement(),
    overscan: Int = 2,
    key: (index: Int) -> Any = { it },
    itemContent: ColumnScope.(index: Int, key: Any) -> Unit
): UiBounds {
    val state = requireNotNull(modifier.scrollState) {
        "lazyColumn '$id' requires a scrollState -- supply Modifier.verticalScroll(state)."
    }
    val sizedModifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax)
    require(sizedModifier.heightDimension != Dimension.WrapContent) {
        "lazyColumn '$id' requires a bounded height (Modifier.height(...) or a FillMax ancestor) " +
            "-- WrapContent would need every item's real extent to size itself, defeating virtualization."
    }

    val slot = claimModifiedSlot(sizedModifier)
    val itemHeightPx = itemHeight.toPx()
    val gapPx = verticalArrangement.baseSpacingPx()
    val stridePx = (itemHeightPx + gapPx).coerceAtLeast(1f)
    val contentHeightPx = if (itemCount <= 0) 0f else itemCount * itemHeightPx + (itemCount - 1) * gapPx

    state.update(
        viewportWidth = slot.width,
        viewportHeight = slot.height,
        contentWidth = slot.width,
        contentHeight = contentHeightPx
    )

    recordSemantic(role = UiSemanticRole.ScrollPanel, id = id, bounds = slot)

    if (hitTest(slot)) {
        onOverScrollable()
        val scrollDeltaY = context.inputState.scrollDeltaY
        if (state.canScrollY && scrollDeltaY != 0f) {
            state.scrollBy(deltaY = -scrollDeltaY * modifier.scrollConfig.scrollSpeed)
            onScrollConsumed()
        }
    }

    val firstVisible = (state.offsetY / stridePx).toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val lastVisible = ((state.offsetY + slot.height) / stridePx).toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val startIndex = (firstVisible - overscan).coerceAtLeast(0)
    val endIndex = (lastVisible + overscan).coerceAtMost(itemCount - 1)

    clip(slot) {
        for (index in startIndex..endIndex) {
            val itemKey = key(index)
            val itemSlot = UiBounds(
                x = slot.x,
                y = slot.y + index * stridePx - state.offsetY,
                width = slot.width,
                height = itemHeightPx
            )
            val itemScope = childColumn(
                slot = itemSlot,
                verticalArrangement = verticalArrangement,
                modifier = UiModifier(testTag = "$id.$itemKey")
            )
            itemScope.itemContent(index, itemKey)
        }
    }

    return slot
}

/** Horizontal counterpart to [lazyColumn] -- see its doc comment for the fixed-height decision,
 * keying convention, and scroll-state requirement (here via `Modifier.horizontalScroll(state)`). */
fun UiScope.lazyRow(
    id: String,
    itemCount: Int,
    itemWidth: Dp,
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = defaultArrangement(),
    overscan: Int = 2,
    key: (index: Int) -> Any = { it },
    itemContent: RowScope.(index: Int, key: Any) -> Unit
): UiBounds {
    val state = requireNotNull(modifier.scrollState) {
        "lazyRow '$id' requires a scrollState -- supply Modifier.horizontalScroll(state)."
    }
    val sizedModifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax)
    require(sizedModifier.widthDimension != Dimension.WrapContent) {
        "lazyRow '$id' requires a bounded width (Modifier.width(...) or a FillMax ancestor) " +
            "-- WrapContent would need every item's real extent to size itself, defeating virtualization."
    }

    val slot = claimModifiedSlot(sizedModifier)
    val itemWidthPx = itemWidth.toPx()
    val gapPx = horizontalArrangement.baseSpacingPx()
    val stridePx = (itemWidthPx + gapPx).coerceAtLeast(1f)
    val contentWidthPx = if (itemCount <= 0) 0f else itemCount * itemWidthPx + (itemCount - 1) * gapPx

    state.update(
        viewportWidth = slot.width,
        viewportHeight = slot.height,
        contentWidth = contentWidthPx,
        contentHeight = slot.height
    )

    recordSemantic(role = UiSemanticRole.ScrollPanel, id = id, bounds = slot)

    if (hitTest(slot)) {
        onOverScrollable()
        val scrollDeltaX = context.inputState.scrollDeltaX
        if (state.canScrollX && scrollDeltaX != 0f) {
            state.scrollBy(deltaX = -scrollDeltaX * modifier.scrollConfig.scrollSpeed)
            onScrollConsumed()
        }
    }

    val firstVisible = (state.offsetX / stridePx).toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val lastVisible = ((state.offsetX + slot.width) / stridePx).toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val startIndex = (firstVisible - overscan).coerceAtLeast(0)
    val endIndex = (lastVisible + overscan).coerceAtMost(itemCount - 1)

    clip(slot) {
        for (index in startIndex..endIndex) {
            val itemKey = key(index)
            val itemSlot = UiBounds(
                x = slot.x + index * stridePx - state.offsetX,
                y = slot.y,
                width = itemWidthPx,
                height = slot.height
            )
            val itemScope = childRow(
                slot = itemSlot,
                horizontalArrangement = horizontalArrangement,
                modifier = UiModifier(testTag = "$id.$itemKey")
            )
            itemScope.itemContent(index, itemKey)
        }
    }

    return slot
}
