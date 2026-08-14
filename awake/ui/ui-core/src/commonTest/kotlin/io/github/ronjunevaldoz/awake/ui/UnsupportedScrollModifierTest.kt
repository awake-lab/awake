// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Scrolling is dispatched on `UiModifier.scrollState` and only `column()` reads it. `row()` and
 * `box()` took the same modifier and dropped it: no scroll, no error, nothing naming why.
 *
 * A silently ignored modifier is the expensive kind of bug -- it looks like a broken container
 * rather than an unsupported call. These pin that it now fails at the call site, and that the one
 * container which does implement scrolling still works.
 */
class UnsupportedScrollModifierTest {

    private fun frame(body: UiPrimitiveScope.() -> Unit) {
        val ui = UiContext()
        ui.beginFrame(400f, 400f, testSnapshot())
        ui.createBox(x = 0f, y = 0f, width = 400f, height = 400f).body()
        ui.endFrame()
    }

    @Test
    fun rowRejectsAScrollModifierItCannotHonour() {
        val failure = assertFailsWith<IllegalStateException> {
            frame {
                row(modifier = Modifier.width(200f.px).height(100f.px).verticalScroll(UiScrollState())) { }
            }
        }
        assertTrue(
            failure.message.orEmpty().contains("row()"),
            "the error must name the container that cannot scroll, got: ${failure.message}",
        )
    }

    @Test
    fun boxRejectsAScrollModifierItCannotHonour() {
        val failure = assertFailsWith<IllegalStateException> {
            frame {
                box(modifier = Modifier.width(200f.px).height(100f.px).verticalScroll(UiScrollState())) { }
            }
        }
        assertTrue(
            failure.message.orEmpty().contains("box()"),
            "the error must name the container that cannot scroll, got: ${failure.message}",
        )
    }

    @Test
    fun columnStillScrolls() {
        // The guard must not have been pasted onto the one container that implements scrolling.
        val state = UiScrollState()
        frame {
            column(
                id = "scroller",
                modifier = Modifier.width(200f.px).height(100f.px).verticalScroll(state),
            ) {
                column(id = "tall", modifier = Modifier.width(Dimension.FillMax).height(500f.px)) { }
            }
        }
        assertTrue(state.canScrollY, "column() must still scroll: contentHeight=${state.contentHeight}")
    }
}
