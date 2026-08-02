// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiFallbackTheme
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.uiPath
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import kotlin.test.Test
import io.github.ronjunevaldoz.awake.ui.style.*

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

        ui.pushFont(font)
        ui.pushTheme(UiFallbackTheme)
        ui.pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp).height(210f.dp)) {
            surface(
                id = "inspector",
                style = Style {
                    borderWidth(1f.dp)
                    contentPadding(12f.dp)
                }
            , modifier = Modifier.height(190f.toDimension())) {
                text("Camera")
                row( modifier = Modifier.height(28f.dp)) { slot ->
                    dropdown(
                        id = "mode",
                        options = listOf("Orbit", "Free Fly"),
                        selectedIndex = 0,
                        modifier = Modifier.width(slot.width.px).height(slot.height.px)
                    )
                }
                row( modifier = Modifier.height(28f.dp)) { slot ->
                    orbitYaw = slider(
                        id = "slider-azimuth",
                        min = -3.14f,
                        max = 3.14f,
                        value = orbitYaw,
                        label = "Azimuth",
                        modifier = Modifier.width(slot.width.px).height(slot.height.px)
                    )
                }
                row( modifier = Modifier.height(28f.dp)) { slot ->
                    orbitDistance = slider(
                        id = "slider-distance",
                        min = 3f,
                        max = 20f,
                        value = orbitDistance,
                        label = "Distance",
                        modifier = Modifier.width(slot.width.px).height(slot.height.px)
                    )
                }
                spacer(Modifier.height(8f.dp))
                text("Debug")
                showGrid = toggle("grid", checked = showGrid, modifier = Modifier.height(28f.dp), label = "Show Grid")
                showFrustum = toggle("frustum", checked = showFrustum, modifier = Modifier.height(28f.dp), label = "Show Frustum")
                toggle("hud", checked = true, modifier = Modifier.height(32f.px), label = "HUD")
            }
        }

        saveUiDslTutorialSnapshot(
            name = "ui-dsl-inspector",
            title = "UI DSL Inspector Composition",
            summary = "The facade can build a tooling-style inspector with reusable section helpers and property-row controls, without dropping back to manual absolute positioning.",
            primitives = ui.endFrame(),
            width = 320,
            height = 250,
            background = UiFallbackTheme.colors.background,
            font = font
        )
    }

    @Test
    fun canvasDslTutorial() {
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(360f, 240f, testSnapshot())

        ui.pushFont(font)
        ui.pushTheme(UiFallbackTheme)
        ui.pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(320f.dp).height(200f.dp)) {
            surface(
                id = "canvas-proof",
                style = Style {
                    borderWidth(1f.dp)
                    contentPadding(12f.dp)
                }
            , modifier = Modifier.height(180f.toDimension())) { slot ->
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
            background = UiFallbackTheme.colors.background,
            font = font
        )
    }
}
