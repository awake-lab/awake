// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import kotlin.test.Test

/** Scratch probe -- counts content-lambda executions for the benchmark's nesting shape. */
class NestedContentExecutionProbeTest {

    @Test
    fun probe() {
        val report = StringBuilder()
        for (depth in 1..5) {
            var real = 0
            var measuring = 0
            val ui = UiContext()
            ui.beginFrame(1280f, 900f, UiInputState(pointerX = -100f, pointerY = -100f))
            fun ColumnScope.nest(remaining: Int) {
                if (remaining == 0) return
                column {
                    row {
                        column {
                            if (context.isMeasuringInternal()) measuring++ else real++
                            nest(remaining - 1)
                        }
                    }
                }
            }
            ui.createAbsolute(x = 0f, y = 0f).column(id = "bench-root") { nest(depth) }
            ui.finishFrame()
            println("PROBE depth=$depth real=$real measuring=$measuring total=${real + measuring}")
        }
    }
}
