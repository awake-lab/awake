// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.benchmark

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput

/**
 * Frame cost of nested row/column layout, parameterised by nesting depth.
 *
 * Depth is the parameter because the cost is depth-driven, not per-node: every row/column runs a
 * trial execution of its content to decide its measurement branch, and that compounds with each
 * level (see docs/tasks/2026-08-02-trial-measure-double-execution.md, which measured the per-depth
 * doubling directly). A single fixed-shape benchmark would report one number and hide the curve,
 * so a fix that flattens the curve would look the same as one that shaves a constant.
 *
 * Deliberately widget-free -- no theme, no text, no design system. This measures ui-core's layout
 * pass alone, so a regression here cannot be blamed on a recipe, and a recipe regression cannot
 * hide in here. Timing lives here; trial COUNTS stay in ui-core's tests, where they are exact and
 * noise-free (the same shell measured 12.9/16.0/17.3 ms across three runs at a fixed 1,724 trials).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
open class LayoutNestingBenchmark {

    @Param("2", "4", "6", "8")
    var depth: Int = 0

    private lateinit var ui: UiContext

    @Setup
    fun setUp() {
        ui = UiContext()
    }

    @Benchmark
    fun nestedColumnsAndRows(): Int {
        ui.beginFrame(UiFrameInput(viewportWidth = FRAME_WIDTH, viewportHeight = FRAME_HEIGHT, input = UiInputState(pointerX = -100f, pointerY = -100f)))
        ui.createAbsolute(x = 0f, y = 0f).column(id = "bench-root") {
            nest(depth)
        }
        return ui.finishFrame().primitives.size
    }

    /** Alternating column/row so both measurement paths are exercised at every level. */
    private fun ColumnScope.nest(remaining: Int) {
        if (remaining == 0) return
        column {
            row {
                column {
                    nest(remaining - 1)
                }
            }
        }
    }

    private companion object {
        const val FRAME_WIDTH = 1280f
        const val FRAME_HEIGHT = 900f
    }
}
