// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
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

    private fun headerBounds(ui: UiContext, category: String): UiBounds {
        val id = "ui-showcase-sidebar-category-$category.trigger"
        val semantics = ui.finishFrame().semantics
        val node = semantics.firstOrNull { it.id == id }
        requireNotNull(node) { "no semantic node recorded for $id -- got ids: ${semantics.map { it.id }}" }
        return node.bounds
    }

    private fun UiBounds.center(): Pair<Float, Float> = (x + width / 2f) to (y + height / 2f)

    private fun UiScope.drawSidebar(ui: UiContext) {
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        shadcnSidebar(
            id = "ui-showcase-sidebar",
            modifier = Modifier.verticalScroll(sidebarScroll)
                .width(264f.dp)
                .height(900f.dp),
        ) {
            drawUiShowcaseSidebar(compact = false)
        }
    }

    @Test
    fun clickingOneCategoryHeaderTogglesOnlyThatCategory() {
        val state = UiShowcaseRuntimeState()
        showcaseTestSession(width = 1440f, height = 900f, theme = state.showcaseTheme()) {
            fun sidebarFrame(x: Float = -100f, y: Float = -100f, down: Boolean = false) =
                frame(x = x, y = y, down = down) { drawSidebar(ui) }
            fun click(at: Pair<Float, Float>) {
                sidebarFrame(at.first, at.second, down = true)
                sidebarFrame(at.first, at.second, down = false)
            }

            // Warm-up frame: establish default (expanded = true for every category) and record bounds.
            sidebarFrame()

            val before = mapOf(
                "GettingStarted" to expandedState(ui, "GettingStarted").value,
                "Inputs" to expandedState(ui, "Inputs").value,
                "Typography" to expandedState(ui, "Typography").value,
                "Patterns" to expandedState(ui, "Patterns").value,
            )
            assertTrue(before.values.all { it }, "every category should start expanded: $before")

            val headerCenter = headerBounds(ui, "GettingStarted").center()

            click(headerCenter)

            // One extra settled frame so the flipped state is visible to a fresh read.
            sidebarFrame()

            assertEquals(
                false,
                expandedState(ui, "GettingStarted").value,
                "clicking the Getting Started header must collapse it",
            )
            assertEquals(true, expandedState(ui, "Inputs").value, "Inputs must be unaffected by a click on a sibling header")
            assertEquals(true, expandedState(ui, "Typography").value, "Typography must be unaffected by a click on a sibling header")
            assertEquals(true, expandedState(ui, "Patterns").value, "Patterns must be unaffected by a click on a sibling header")
        }
    }

    @Test
    fun rapidRepeatedClicksToggleOncePerClickWithoutDoubleFiringOrSticking() {
        val state = UiShowcaseRuntimeState()
        showcaseTestSession(width = 1440f, height = 900f, theme = state.showcaseTheme()) {
            fun sidebarFrame(x: Float = -100f, y: Float = -100f, down: Boolean = false) =
                frame(x = x, y = y, down = down) { drawSidebar(ui) }
            fun click(at: Pair<Float, Float>) {
                sidebarFrame(at.first, at.second, down = true)
                sidebarFrame(at.first, at.second, down = false)
            }

            sidebarFrame()
            val headerCenter = headerBounds(ui, "GettingStarted").center()

            // click 1: expanded (true) -> collapsed (false)
            click(headerCenter)
            sidebarFrame()
            assertEquals(false, expandedState(ui, "GettingStarted").value, "first click must collapse")

            // click 2: collapsed (false) -> expanded (true)
            click(headerCenter)
            sidebarFrame()
            assertEquals(true, expandedState(ui, "GettingStarted").value, "second click must re-expand, not stick or double-fire")

            // click 3: expanded (true) -> collapsed (false)
            click(headerCenter)
            sidebarFrame()
            assertEquals(false, expandedState(ui, "GettingStarted").value, "third click must collapse again")
        }
    }
}
