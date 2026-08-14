// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.UiLayoutDiagnostics
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * How a middle child is sized, across container type x parent sizing x child sizing.
 *
 * Every cell is the same shape -- a fixed 48px header, the child under test, a fixed 48px footer
 * -- so the expected height is arithmetic, not judgement: a weighted or filling child must take
 * what the two fixed siblings leave.
 *
 * Written because four hand-picked sidebar tests all passed while the real render put the footer
 * on top of the menu. They passed on lenient thresholds ("more than 48px"), which a fixed number
 * cannot do. Expected values here are exact for that reason -- a matrix multiplies whatever
 * assertion quality it is given, so a matrix of thresholds would have been four times more green
 * and four times more wrong.
 */
class LayoutSizingMatrixTest {

    private enum class Container { Column, Surface }

    private enum class ChildSizing { Fixed, FillMax, Weight, WeightWithContent }

    private data class Cell(
        val container: Container,
        val parentHeight: Dimension,
        val child: ChildSizing,
    ) {
        val label: String
            get() {
                val parent = when (parentHeight) {
                    is Dimension.Fixed -> "Fixed"
                    Dimension.FillMax -> "FillMax"
                    Dimension.WrapContent -> "Wrap"
                }
                return "${container.name}/$parent/${child.name}"
            }
    }

    private fun childModifier(child: ChildSizing): UiModifier = when (child) {
        ChildSizing.Fixed -> Modifier.width(Dimension.FillMax).height(CHILD_FIXED.px)
        ChildSizing.FillMax -> Modifier.width(Dimension.FillMax).fillMaxHeight()
        ChildSizing.Weight, ChildSizing.WeightWithContent ->
            Modifier.width(Dimension.FillMax).weight(1f)
    }

    private fun measure(cell: Cell): Float {
        // This suite exists to RECORD the fallback, so it opts out of the throw that now guards
        // it for everyone else. Without this the matrix reports the first broken cell instead of
        // the whole table, which is the one thing it is for.
        UiLayoutDiagnostics.allowUnplannedWeight = true
        val ui = UiContext()
        ui.beginFrame(FRAME, FRAME, testSnapshot())
        var child: UiBounds? = null
        val body: ColumnScope.(UiBounds) -> Unit = {
            column(id = "head", modifier = Modifier.width(Dimension.FillMax).height(FIXED.px)) { }
            child = column(id = "body", modifier = childModifier(cell.child)) {
                // Isolates "weight ignored" from "empty child measures zero": the known-good
                // weight fixture in RowColumnWeightCacheTest always has a sized child.
                if (cell.child == ChildSizing.WeightWithContent) {
                    column(id = "inner", modifier = Modifier.width(Dimension.FillMax).height(24f.px)) { }
                }
            }
            column(id = "foot", modifier = Modifier.width(Dimension.FillMax).height(FIXED.px)) { }
        }
        val parentModifier = Modifier.width(Dimension.FillMax).let { base ->
            when (val h = cell.parentHeight) {
                is Dimension.Fixed -> base.height(h.dp)
                Dimension.FillMax -> base.fillMaxHeight()
                Dimension.WrapContent -> base
            }
        }
        val root = ui.createBox(x = 0f, y = 0f, width = FRAME, height = FRAME)
        // Zero gap and zero content padding: both are real features, but they are not what this
        // matrix measures, and leaving them in makes every expected value carry a container-
        // specific correction. Isolate the division itself.
        val noGap = Arrangement.spacedBy(0f.px)
        when (cell.container) {
            Container.Column -> root.column(
                id = "parent",
                verticalArrangement = noGap,
                modifier = parentModifier,
                content = body,
            )
            Container.Surface -> root.surface(
                id = "parent",
                verticalArrangement = noGap,
                style = Style { contentPadding(0f.dp) },
                modifier = parentModifier,
                content = body,
            )
        }
        ui.endFrame()
        UiLayoutDiagnostics.allowUnplannedWeight = false
        return child?.height ?: -1f
    }

    @Test
    fun everyContainerDividesItsHeightTheSameWay() {
        val parentHeights = listOf(Dimension.Fixed(PARENT_FIXED.px), Dimension.FillMax)
        val cells = Container.entries.flatMap { container ->
            parentHeights.flatMap { parentHeight ->
                ChildSizing.entries.map { Cell(container, parentHeight, it) }
            }
        }

        val rows = cells.map { cell ->
            val actual = measure(cell)
            // Parent resolves to PARENT_FIXED either way here: the frame is FRAME tall and a
            // FillMax parent fills it, so both columns of the matrix share one expectation.
            val parentSize = if (cell.parentHeight == Dimension.FillMax) FRAME else PARENT_FIXED
            val expected = when (cell.child) {
                ChildSizing.Fixed -> CHILD_FIXED
                // FillMax and weight are NOT the same request, and this row is where that gets
                // pinned. A filling child takes what is left where it stands -- it has only seen
                // the header, and in a single-pass engine the footer does not exist yet -- so it
                // claims parent - header and the footer lands past the edge. Compose behaves the
                // same way: fillMaxHeight() fills the incoming constraint, it does not reserve
                // room for later siblings. "Take the remainder" is weight(1f), below.
                ChildSizing.FillMax -> parentSize - FIXED
                // Weight is resolved by the parent AFTER every sibling is measured, so this one
                // really is the slack both fixed siblings left.
                ChildSizing.Weight, ChildSizing.WeightWithContent -> parentSize - FIXED * 2f
            }
            Triple(cell.label, actual, expected)
        }

        val failures = rows.filter { (_, actual, expected) -> abs(actual - expected) > TOLERANCE }
        val table = rows.joinToString("\n") { (label, actual, expected) ->
            val mark = if (abs(actual - expected) > TOLERANCE) "FAIL" else "ok  "
            "  $mark $label: actual=$actual expected=$expected"
        }

        assertTrue(
            failures.isEmpty(),
            "${failures.size} of ${rows.size} sizing cells disagree with their arithmetic:\n$table",
        )
    }

    private companion object {
        const val FRAME = 400f
        const val PARENT_FIXED = 400f
        const val FIXED = 48f
        const val CHILD_FIXED = 64f
        const val TOLERANCE = 1f
    }
}
