// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Real press/release interaction simulation on the live sidebar, per
 * `SurfaceClickableTest`/`UiShowcaseLayoutCostTest`'s established patterns -- a wireframe
 * bounds-overlay tool only draws layout bounds and would never catch this class of bug (state
 * not actually flipping, or flipping on the wrong widget). Renders the real
 * `drawUiShowcaseSidebar(compact = false)` tree, hit-tests the "Getting Started" category
 * header's real semantic bounds, and simulates a real two-frame press-then-release on it.
 */
class ShadcnCollapsibleToggleTest {

    private fun expandedState(ui: UiContext, category: String) =
        ui.rememberStateValue("ui-showcase-sidebar-category", category) { true }

    private fun UiContext.frame(input: Input, down: Boolean, x: Float, y: Float, body: () -> Unit) {
        input.setPointer(down, x, y)
        beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        body()
        finishFrame()
    }

    private fun UiContext.click(input: Input, at: Pair<Float, Float>, body: () -> Unit) {
        frame(input, down = true, x = at.first, y = at.second, body = body)
        frame(input, down = false, x = at.first, y = at.second, body = body)
    }

    private fun headerBounds(ui: UiContext, category: String): UiBounds {
        val id = "ui-showcase-sidebar-category-$category.header"
        val semantics = ui.finishFrame().semantics
        val node = semantics.firstOrNull { it.id == id }
        requireNotNull(node) { "no semantic node recorded for $id -- got ids: ${semantics.map { it.id }}" }
        return node.bounds
    }

    private fun UiBounds.center(): Pair<Float, Float> = (x + width / 2f) to (y + height / 2f)

    private fun drawSidebar(ui: UiContext) {
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        ui.createBox(x = 0f, y = 0f, width = 1440f, height = 900f).shadcnSidebar(
            id = "ui-showcase-sidebar",
            modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden))
                .width(264f.dp)
                .height(Dimension.FillMax),
        ) {
            drawUiShowcaseSidebar(compact = false)
        }
    }

    @Test
    fun clickingOneCategoryHeaderTogglesOnlyThatCategory() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        fun frameNoInput(body: () -> Unit) {
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            body()
            ui.endFrame()
        }

        // Warm-up frame: establish default (expanded = true for every category) and record bounds.
        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }

        val before = mapOf(
            "GettingStarted" to expandedState(ui, "GettingStarted").value,
            "Inputs" to expandedState(ui, "Inputs").value,
            "Typography" to expandedState(ui, "Typography").value,
            "Patterns" to expandedState(ui, "Patterns").value,
        )
        assertTrue(before.values.all { it }, "every category should start expanded: $before")

        val headerCenter = headerBounds(ui, "GettingStarted").center()

        ui.click(input, headerCenter) {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }

        // One extra settled frame so the flipped state is visible to a fresh read.
        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }

        assertEquals(
            false,
            expandedState(ui, "GettingStarted").value,
            "clicking the Getting Started header must collapse it",
        )
        assertEquals(true, expandedState(ui, "Inputs").value, "Inputs must be unaffected by a click on a sibling header")
        assertEquals(true, expandedState(ui, "Typography").value, "Typography must be unaffected by a click on a sibling header")
        assertEquals(true, expandedState(ui, "Patterns").value, "Patterns must be unaffected by a click on a sibling header")
    }

    @Test
    fun rapidRepeatedClicksToggleOncePerClickWithoutDoubleFiringOrSticking() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        fun frameNoInput(body: () -> Unit) {
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            body()
            ui.endFrame()
        }

        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        val headerCenter = headerBounds(ui, "GettingStarted").center()

        // click 1: expanded (true) -> collapsed (false)
        ui.click(input, headerCenter) {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        assertEquals(false, expandedState(ui, "GettingStarted").value, "first click must collapse")

        // click 2: collapsed (false) -> expanded (true)
        ui.click(input, headerCenter) {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        assertEquals(true, expandedState(ui, "GettingStarted").value, "second click must re-expand, not stick or double-fire")

        // click 3: expanded (true) -> collapsed (false)
        ui.click(input, headerCenter) {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        frameNoInput {
            ui.pushTheme(theme)
            drawSidebar(ui)
        }
        assertEquals(false, expandedState(ui, "GettingStarted").value, "third click must collapse again")
    }
}
