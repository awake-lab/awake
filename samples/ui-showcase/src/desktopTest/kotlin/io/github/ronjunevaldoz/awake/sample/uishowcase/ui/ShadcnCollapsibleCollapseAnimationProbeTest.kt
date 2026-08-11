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
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/**
 * Real repro of the user's "collapsing a category, animation is dropping up" report -- same
 * live `drawUiShowcaseSidebar` tree wrapped in the same `verticalScroll` container as
 * production (so the collapsible's `animatedHeight` runs through the scroll panel's per-frame
 * `measureColumnContent` trial pass, not just a bare column like a unit-level probe would).
 * Samples the "Getting Started" header's own semantic Y and the *next* category header's Y
 * every frame across a real click-to-collapse, looking for a one-frame jump/overshoot instead
 * of a smooth monotonic slide.
 */
class ShadcnCollapsibleCollapseAnimationProbeTest {

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
    fun collapsingGettingStartedSlidesInputsHeaderUpSmoothly() {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        fun frameNoInput() {
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            drawSidebar(ui)
        }

        frameNoInput()
        val gettingStartedHeader = headerBounds(ui, "GettingStarted")
        val headerCenter = gettingStartedHeader.center()

        // Press-then-release the "Getting Started" header to trigger a real collapse, same
        // two-frame press/release idiom as ShadcnCollapsibleToggleTest, then sample every frame
        // afterward while the collapse animation eases.
        val inputsHeaderYs = mutableListOf<Float>()
        input.setPointer(true, headerCenter.first, headerCenter.second)
        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        ui.pushTheme(theme)
        drawSidebar(ui)
        ui.finishFrame()

        input.setPointer(false, headerCenter.first, headerCenter.second)
        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        ui.pushTheme(theme)
        drawSidebar(ui)
        ui.finishFrame()

        repeat(40) {
            input.setPointer(false, -100f, -100f)
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            drawSidebar(ui)
            val semantics = ui.finishFrame().semantics
            val inputsHeader = semantics.firstOrNull { it.id == "ui-showcase-sidebar-category-Inputs.header" }
            requireNotNull(inputsHeader) { "Inputs header missing from semantics: ${semantics.map { it.id }}" }
            inputsHeaderYs.add(inputsHeader.bounds.y)
        }

        println("Inputs header Y across collapse: $inputsHeaderYs")

        var prevY = inputsHeaderYs.first()
        for ((i, y) in inputsHeaderYs.withIndex()) {
            check(y <= prevY + 0.01f) {
                "Inputs header Y INCREASED (moved back down) at frame $i: $prevY -> $y -- non-monotonic slide in $inputsHeaderYs"
            }
            prevY = y
        }
    }
}
