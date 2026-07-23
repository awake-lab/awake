// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.uiPath
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

    @Test
    fun canvasDslTutorial() {
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(360f, 240f, testSnapshot())

        ui.column(slot = UiSlot(20f, 20f, 320f, 200f), font = font, theme = CoreUiTheme, textScale = 2f) {
            surface(
                id = "canvas-proof",
                height = 180f.toDimension(),
                radius = UiShape.md,
                borderWidth = 1f.dp,
                style = Style {
                    contentPadding(12f.dp)
                }
            ) { slot ->
                canvas(slot) {
                    val headerGradient = UiLinearGradient.horizontal(
                        start = Color(0.12f, 0.38f, 0.95f, 1f),
                        end = Color(0.42f, 0.17f, 0.96f, 1f)
                    )
                    drawGradientRect(0f, 0f, bounds.width, 54f, headerGradient)
                    drawGradientBorder(
                        0f,
                        0f,
                        bounds.width,
                        bounds.height,
                        UiLinearGradient.vertical(
                            top = Color(1f, 1f, 1f, 0.18f),
                            bottom = Color(1f, 1f, 1f, 0.05f)
                        )
                    )
                    drawText(
                        text = "Canvas DSL",
                        x = 14f,
                        y = 34f,
                        color = Color.White
                    )
                    drawRoundRect(
                        x = 14f,
                        y = 72f,
                        width = 108f,
                        height = 72f,
                        color = Color(0.96f, 0.97f, 0.99f, 1f),
                        radius = 12f.dp
                    )
                    fillPath(
                        uiPath {
                            moveTo(34f, 124f)
                            lineTo(68f, 90f)
                            lineTo(102f, 124f)
                            close()
                        },
                        color = Color(0.18f, 0.48f, 0.89f, 1f)
                    )
                    clipShape(UiShapeSpec.Circle, 178f, 76f, 92f, 92f) {
                        drawGradientRect(
                            178f,
                            76f,
                            92f,
                            92f,
                            UiLinearGradient.vertical(
                                top = Color(0.98f, 0.73f, 0.20f, 1f),
                                bottom = Color(0.91f, 0.31f, 0.16f, 1f)
                            )
                        )
                        nested(192f, 90f, 64f, 64f) {
                            drawCircle(0f, 0f, 64f, Color(1f, 1f, 1f, 0.22f))
                            drawLine(8f, 48f, 56f, 16f, Color.White)
                        }
                    }
                }
            }
        }

        saveUiDslTutorialSnapshot(
            name = "ui-dsl-canvas",
            title = "UI DSL Canvas Composition",
            summary = "Canvas exposes a local-coordinate drawing DSL for gradients, paths, clipping, and nested composition without falling back to raw primitive emission.",
            primitives = ui.endFrame(),
            width = 360,
            height = 240,
            background = CoreUiTheme.tokens.background,
            font = font
        )
    }
}
