// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.width
import kotlin.test.Test

/** Builds a one-off [UiInputState] for a test frame -- [Input] is a per-session instance
 * now (no longer a global object), so tests construct their own throwaway one instead of
 * writing into shared static state. */
private fun testSnapshot(x: Float = -100f, y: Float = -100f, down: Boolean = false): UiInputState {
    val input = Input()
    input.setPointer(down, x, y)
    return input.updateSnapshot().toUiInputState()
}

class UiDslTutorialDocsTest {

    @Test
    fun inspectorDslTutorial() {
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(320f, 250f, testSnapshot())

        var orbitYaw = 0.2f
        var orbitDistance = 8f
        var showGrid = true
        var showFrustum = false

        ui.column(slot = UiSlot(20f, 20f, 280f, 210f), font = font, theme = CoreUiTheme, textScale = 2f) {
            surface(
                id = "inspector",
                height = 190f.toDimension(),
                radius = UiShape.md,
                borderWidth = 1f.dp,
                style = Style {
                    contentPadding(12f.dp)
                }
            ) {
                text("Camera")
                row(height = 28f.dp) { slot ->
                    dropdown(
                        id = "mode",
                        options = listOf("Orbit", "Free Fly"),
                        selectedIndex = 0,
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                }
                row(height = 28f.dp) { slot ->
                    orbitYaw = slider(
                        id = "slider-azimuth",
                        min = -3.14f,
                        max = 3.14f,
                        value = orbitYaw,
                        label = "Azimuth",
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                }
                row(height = 28f.dp) { slot ->
                    orbitDistance = slider(
                        id = "slider-distance",
                        min = 3f,
                        max = 20f,
                        value = orbitDistance,
                        label = "Distance",
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                }
                spacer(UiModifier().height(8f.dp))
                text("Debug")
                showGrid = toggle("grid", checked = showGrid, modifier = UiModifier().height(28f.dp), label = "Show Grid")
                showFrustum = toggle("frustum", checked = showFrustum, modifier = UiModifier().height(28f.dp), label = "Show Frustum")
                toggle("hud", checked = true, modifier = UiModifier().height(32f.px), label = "HUD")
            }
        }

        saveUiDslTutorialSnapshot(
            name = "ui-dsl-inspector",
            title = "UI DSL Inspector Composition",
            summary = "The facade can build a tooling-style inspector with reusable section helpers and property-row controls, without dropping back to manual absolute positioning.",
            primitives = ui.endFrame(),
            width = 320,
            height = 250,
            background = CoreUiTheme.tokens.background,
            font = font
        )
    }
}
