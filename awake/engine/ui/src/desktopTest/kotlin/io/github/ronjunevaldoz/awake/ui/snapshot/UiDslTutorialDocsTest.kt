// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.DefaultUiTheme
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.ui
import kotlin.test.Test

class UiDslTutorialDocsTest {

    @Test
    fun inspectorDslTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(320f, 250f)

        var orbitYaw = 0.2f
        var orbitDistance = 8f
        var showGrid = true
        var showFrustum = false

        ui.ui(x = 20f, y = 20f, width = 280f, font = font, theme = DefaultUiTheme, textScale = 2f) {
            panel(
                id = "inspector",
                height = 190f.toDimension(),
                radius = UiShape.md,
                borderWidth = 1f.dp,
                style = Style {
                    contentPadding(12f.dp)
                }
            ) {
                inspectorSection("Camera")
                propertyRow("Mode", 28f) { slot ->
                    dropdown("mode", listOf("Orbit", "Free Fly"), 0, slot.width, slot.height)
                }
                propertySlider("Azimuth", orbitYaw, -3.14f, 3.14f) { orbitYaw = it }
                propertySlider("Distance", orbitDistance, 3f, 20f) { orbitDistance = it }

                spacer(8f)
                inspectorSection("Debug")
                showGrid = propertyCheckbox("grid", showGrid, "Show Grid", 28f)
                showFrustum = propertyCheckbox("frustum", showFrustum, "Show Frustum", 28f)
                toggle("hud", checked = true, height = 32f, label = "HUD")
            }
        }

        saveUiDslTutorialSnapshot(
            name = "ui-dsl-inspector",
            title = "UI DSL Inspector Composition",
            summary = "The facade can build a tooling-style inspector with reusable section helpers and property-row controls, without dropping back to manual absolute positioning.",
            primitives = ui.endFrame(),
            width = 320,
            height = 250,
            background = DefaultUiTheme.tokens.background
        )
    }
}

private fun UiColumnDslScope.inspectorSection(title: String) {
    text(
        title,
        style = Style {
            foreground(DefaultUiTheme.tokens.mutedForeground)
        }
    )
}

private fun UiColumnDslScope.propertySlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValue: (Float) -> Unit
) {
    propertyRow(label, height = 28f) { slot ->
        onValue(slider("slider-$label", min, max, value, slot.width, slot.height))
    }
}
