// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.lazyColumn
import io.github.ronjunevaldoz.awake.ui.layouts.lazyRow
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.horizontalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proves [io.github.ronjunevaldoz.awake.ui.layouts.lazyColumn]/[lazyRow] actually virtualize --
 * not just "renders correctly for 10 items", but that composing a 10,000-item list only ever
 * emits primitives for the handful of items that intersect the viewport (plus overscan), never
 * the full item count. Each item is a single `surface()` with an opaque fill and no border, so
 * exactly one [UiDrawPrimitive.Quad] is emitted per composed item -- a direct, countable proxy
 * for "how many items actually ran their content lambda".
 */
class LazyColumnVirtualizationTest {

    @Test
    fun tenThousandItemListOnlyEmitsPrimitivesForVisibleWindow() {
        val ui = UiContext()
        ui.beginFrame(400f, 300f, testSnapshot())
        val state = UiScrollState()

        ui.createColumn(x = 0f, y = 0f, width = 400f).lazyColumn(
            id = "list",
            itemCount = 10_000,
            itemHeight = 20f.dp,
            modifier = Modifier.height(300f.dp).verticalScroll(state),
        ) { index, _ ->
            surface(
                id = "row-$index",
                style = Style {
                    background(Color.White)
                    shape(UiShape.none)
                },
            ) {}
        }

        val primitives = ui.endFrame()
        val quadCount = primitives.filterIsInstance<UiDrawPrimitive.Quad>().count()

        // Viewport is 300px tall / 20px items = 15 fully visible + default overscan(2) each side
        // -> at most 19 composed items near the top of an unscrolled list, vs. 10,000 total.
        assertTrue(
            quadCount in 1..25,
            "expected roughly the visible window (~15-19) of 10,000 items to be composed, got $quadCount",
        )
        assertTrue(quadCount < 10_000, "virtualization defeated: composed close to every item")
    }

    @Test
    fun scrollingMovesTheComposedWindowInsteadOfComposingEverything() {
        val ui = UiContext()
        ui.beginFrame(400f, 300f, testSnapshot())
        val state = UiScrollState()
        state.update(viewportHeight = 300f, contentHeight = 200_000f)
        state.scrollTo(offsetY = 5_000f)

        ui.createColumn(x = 0f, y = 0f, width = 400f).lazyColumn(
            id = "list",
            itemCount = 10_000,
            itemHeight = 20f.dp,
            modifier = Modifier.height(300f.dp).verticalScroll(state),
        ) { index, _ ->
            surface(
                id = "row-$index",
                style = Style {
                    background(Color.White)
                    shape(UiShape.none)
                },
            ) {}
        }

        val primitives = ui.endFrame()
        val quadCount = primitives.filterIsInstance<UiDrawPrimitive.Quad>().count()
        assertTrue(quadCount in 1..25, "expected a small scrolled-in window, got $quadCount")
    }

    @Test
    fun lazyRowVirtualizesAlongTheHorizontalAxis() {
        val ui = UiContext()
        ui.beginFrame(300f, 100f, testSnapshot())
        val state = UiScrollState()

        ui.createColumn(x = 0f, y = 0f, width = 300f).lazyRow(
            id = "row-list",
            itemCount = 10_000,
            itemWidth = 20f.dp,
            modifier = Modifier.width(300f.dp).horizontalScroll(state),
        ) { index, _ ->
            surface(
                id = "col-$index",
                style = Style {
                    background(Color.White)
                    shape(UiShape.none)
                },
            ) {}
        }

        val primitives = ui.endFrame()
        val quadCount = primitives.filterIsInstance<UiDrawPrimitive.Quad>().count()
        assertTrue(quadCount in 1..25, "expected roughly the visible window, got $quadCount")
        assertTrue(quadCount < 10_000, "virtualization defeated: composed close to every item")
    }

    @Test
    fun keyIsThreadedIntoItemContentForStableNestedIds() {
        val ui = UiContext()
        ui.beginFrame(400f, 100f, testSnapshot())
        val state = UiScrollState()
        val seenKeys = mutableListOf<Any>()

        ui.createColumn(x = 0f, y = 0f, width = 400f).lazyColumn(
            id = "list",
            itemCount = 5,
            itemHeight = 20f.dp,
            modifier = Modifier.height(100f.dp).verticalScroll(state),
            key = { index -> "item-$index" },
        ) { index, key ->
            seenKeys += key
            surface(id = "row-$key") {}
        }

        ui.endFrame()
        assertEquals(listOf("item-0", "item-1", "item-2", "item-3", "item-4"), seenKeys.take(5))
    }
}
