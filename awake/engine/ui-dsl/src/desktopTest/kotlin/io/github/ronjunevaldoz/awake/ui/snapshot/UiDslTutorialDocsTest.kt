// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.propertyDropdown
import io.github.ronjunevaldoz.awake.ui.propertySlider
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toggle
import io.github.ronjunevaldoz.awake.ui.ui
import kotlin.test.Test

class UiDslTutorialDocsTest {

    @Test
    fun inspectorDslTutorial() {
        val font = UiFonts.default()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(320f, 250f)

        var orbitYaw = 0.2f
        var orbitDistance = 8f
        var showGrid = true
        var showFrustum = false

        ui.ui(x = 20f, y = 20f, width = 280f, font = font, theme = CoreUiTheme, textScale = 2f) {
            panel(
                id = "inspector",
                height = 190f.toDimension(),
                radius = UiShape.md,
                borderWidth = 1f.dp,
                style = Style {
                    contentPadding(12f.dp)
                }
            ) {
                sectionTitle("Camera")
                propertyDropdown("mode", "Mode", listOf("Orbit", "Free Fly"), 0)
                orbitYaw = propertySlider("slider-azimuth", "Azimuth", -3.14f, 3.14f, orbitYaw)
                orbitDistance = propertySlider("slider-distance", "Distance", 3f, 20f, orbitDistance)

                spacer(8f.dp)
                sectionTitle("Debug")
                showGrid = propertyCheckbox("grid", showGrid, "Show Grid", 28f.dp)
                showFrustum = propertyCheckbox("frustum", showFrustum, "Show Frustum", 28f.dp)
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
