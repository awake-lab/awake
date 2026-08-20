// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.headless.HeadlessCanvasGradient
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.canvas
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val CanvasPage = ShowcasePage(
    id = "canvas",
    title = "Canvas",
    category = ShowcaseCategory.Layout,
    description = "Escape hatch for custom drawing: gradients, clipping, and nested local coordinate spaces.",
    usageCode = """canvas(id = "chart", modifier = Modifier.width(360f.dp)) { drawRect(...) }""",
    previewWidth = 520,
    previewHeight = 400,
    notes = listOf("Custom drawing stays an explicit advanced API; ordinary components stay on Headless slots."),
    hero = {
        shadcnMuted("Nested blocks carry their own local origin, so children draw in parent-relative coordinates.")
        shadcnSurface(
            id = "showcase-canvas-page",
            modifier = Modifier.width(420f.dp).height(220f.dp),
        ) {
            canvas(id = "showcase-canvas-root", modifier = Modifier.width(360f.dp).height(190f.dp)) {
                val colors = primitive.theme.colors
                drawGradientRect(
                    x = 0f,
                    y = 0f,
                    width = bounds.width,
                    height = 56f,
                    gradient = HeadlessCanvasGradient(
                        colors.primary.withAlpha(0.16f),
                        colors.accent.withAlpha(0.22f),
                    ),
                )
                clipCircle(x = 20f, y = 18f, diameter = 48f) {
                    drawRect(20f, 18f, 48f, 48f, colors.accent)
                }
                nested(x = 188f, y = 82f, width = 92f, height = 92f) {
                    drawRoundRect(0f, 0f, 92f, 92f, colors.muted, radius = primitive.theme.shapes.md)
                    drawLine(12f, 70f, 78f, 22f, colors.primary, strokeWidth = 2f.dp)
                }
            }
        }
    },
)
