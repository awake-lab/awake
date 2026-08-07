// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.CanvasScope
import io.github.ronjunevaldoz.awake.ui.UiFillRule
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.uiPath

internal fun ColumnScope.drawUiShowcaseLayoutPreview() {
    shadcnSupportingText("row(...) advances a cursor along the horizontal axis; each child claims the next slot in call order.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.height(48f.dp.toDimension())
    ) {
        surface(
            id = "layout-row-a",
            style = Style { background(theme.colors.primary) },
            modifier = Modifier.width(Dimension.Fixed(80f.dp)).height(Dimension.FillMax)
        ) { }
        surface(
            id = "layout-row-b",
            style = Style { background(theme.colors.secondary) },
            modifier = Modifier.width(Dimension.Fixed(120f.dp)).height(Dimension.FillMax)
        ) { }
        surface(
            id = "layout-row-c",
            style = Style { background(theme.colors.muted) },
            modifier = Modifier.width(Dimension.Fixed(160f.dp)).height(Dimension.FillMax)
        ) { }
    }
    spacer(Modifier.height(16f.dp))
    shadcnSupportingText("column(...) advances a cursor along the vertical axis -- the default layout for every page in this catalog.")
    spacer(Modifier.height(8f.dp))
    column(
        verticalArrangement = Arrangement.spacedBy(6f.dp),
        modifier = Modifier.width(Dimension.Fixed(200f.dp)).height(Dimension.Fixed(112f.dp))
    ) {
        surface(
            id = "layout-col-a",
            style = Style { background(theme.colors.primary) },
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(28f.dp))
        ) { }
        surface(
            id = "layout-col-b",
            style = Style { background(theme.colors.secondary) },
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(28f.dp))
        ) { }
        surface(
            id = "layout-col-c",
            style = Style { background(theme.colors.muted) },
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(28f.dp))
        ) { }
    }
}

internal fun ColumnScope.drawUiShowcaseScrollPanelPreview() {
    val scrollState = context.rememberScrollState("ui-showcase-scroll-panel")
    shadcnSupportingText("Scrollable containers own clipping, content measurement, and the scrollbar lane so callers do not have to reimplement any of it.")
    spacer(Modifier.height(8f.dp))
    shadcnSurface(
        id = "showcase-scroll-panel-page",
        style = Style { shape(14f.dp) },
        modifier = Modifier.width(Dimension.Fixed(420f.dp))
            .height(Dimension.WrapContent)
    ) { _ ->
        column(
            id = "scroll-container",
            modifier = Modifier
                .fillMaxWidth()
                .height(176f.dp) // TODO missing fillParentHeight
                .verticalScroll(scrollState),
        ) {
            repeat(10) { index ->
                shadcnButton(
                    id = "showcase-scroll-row-$index",
                    label = "Inspector row ${index + 1}",
                    modifier = Modifier.fillMaxWidth().height(32f.dp),
                    variant = if (index % 2 == 0) ShadcnButtonVariant.Outline else ShadcnButtonVariant.Ghost
                )
            }
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(
        listOf(
            "The scroll thumb only appears when content actually exceeds the viewport.",
            "The widget-level preview report keeps a static clipped state around so we can catch scrollbar and clipping drift without manual scrolling."
        )
    )
}

internal fun ColumnScope.drawUiShowcaseCanvasPreview() {
    shadcnSupportingText("Canvas keeps custom drawing local to a bounded slot, so layout still owns structure while drawing owns paint.")
    spacer(Modifier.height(8f.dp))
    shadcnSurface(
        id = "showcase-canvas-page",
        style = Style { shape(16f.dp) },
        modifier = Modifier.width(Dimension.Fixed(420f.dp))
            .height(Dimension.Fixed(220f.dp))
    ) { slot ->
        recordSemantic(
            role = UiSemanticRole.Panel,
            id = "showcase-canvas-root",
            bounds = slot
        )
        canvas(slot) {
            drawShowcaseCanvasScene()
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(
        listOf(
            "The header glow, clipped badge, and nested chart all come from the same CanvasScope without opening a separate renderer API.",
            "This is the intended authoring path for custom HUDs, diagnostics, vector ornaments, and design-system art."
        )
    )
}

private fun CanvasScope.drawShowcaseCanvasScene() {
    val tokens = context.currentTheme.colors
    val headerGradient = UiLinearGradient.horizontal(
        start = tokens.primary.withAlpha(0.16f),
        end = tokens.accent.withAlpha(0.22f)
    )
    val badgeGradient = UiLinearGradient.vertical(
        top = tokens.accent.withAlpha(0.92f),
        bottom = tokens.primary.withAlpha(0.78f)
    )
    drawGradientRect(
        x = 0f,
        y = 0f,
        width = bounds.width,
        height = 56f,
        gradient = headerGradient
    )
    // Header title/subtitle need a baseline gap of roughly one glyph height (~28px at this
    // preview's 2x density body size) to not overlap -- the old 16px gap (18 -> 34) was
    // smaller than the glyph height itself, so the subtitle painted right through the title.
    drawText(
        text = "Canvas",
        x = 20f,
        y = 8f,
        color = tokens.foreground
    )
    drawText(
        text = "Local drawing inside a surfaced slot",
        x = 20f,
        y = 48f,
        color = tokens.mutedForeground
    )

    drawRoundRect(
        x = 18f,
        y = 76f,
        width = 148f,
        height = 108f,
        color = tokens.background.withAlpha(0.94f),
        radius = 16f.dp,
        borderWidth = 1f.dp,
        borderColor = tokens.border
    )
    // "Shapes" label moved closer to the box's top edge (94 -> 88) and every shape below it
    // shifted down by 12px so the label's own glyph height no longer overlaps the circle.
    drawText("Shapes", x = 34f, y = 88f, color = tokens.foreground)
    drawLine(
        startX = 34f,
        startY = 164f,
        endX = 126f,
        endY = 124f,
        color = tokens.primary
    )
    drawCircle(
        x = 38f,
        y = 122f,
        diameter = 22f,
        color = tokens.primary.withAlpha(0.85f)
    )
    drawShape(
        shape = UiShapeSpec.CutCorner(10f.dp),
        x = 84f,
        y = 118f,
        width = 46f,
        height = 30f,
        color = tokens.secondary.withAlpha(0.88f),
        borderWidth = 1f.dp,
        borderColor = tokens.border
    )
    fillPath(
        path = uiPath(fillRule = UiFillRule.NonZero) {
            moveTo(42f, 178f)
            lineTo(66f, 150f)
            lineTo(92f, 178f)
            close()
        },
        color = tokens.accent.withAlpha(0.82f)
    )

    clipShape(
        shape = UiShapeSpec.Circle,
        x = 300f,
        y = 82f,
        width = 82f,
        height = 82f
    ) {
        drawGradientRect(
            x = 0f,
            y = 0f,
            width = 82f,
            height = 82f,
            gradient = badgeGradient
        )
        // Same too-tight baseline gap as the header title/subtitle above (18px, smaller than
        // the ~28px glyph height at this density) -- widened so "ART" doesn't paint through "HUD".
        drawText("HUD", x = 21f, y = 20f, color = Color.White)
        drawText("ART", x = 23f, y = 48f, color = Color.White)
    }

    nested(
        x = 188f,
        y = 82f,
        width = 92f,
        height = 92f
    ) {
        drawRoundRect(
            x = 0f,
            y = 0f,
            width = bounds.width,
            height = bounds.height,
            color = tokens.muted.withAlpha(0.32f),
            radius = 14f.dp,
            borderWidth = 1f.dp,
            borderColor = tokens.border.withAlpha(0.8f)
        )
        drawText("Chart", x = 14f, y = 16f, color = tokens.foreground)
        drawLine(14f, 68f, 32f, 54f, color = tokens.secondary)
        drawLine(32f, 54f, 50f, 60f, color = tokens.secondary)
        drawLine(50f, 60f, 68f, 34f, color = tokens.primary)
        drawLine(68f, 34f, 78f, 28f, color = tokens.accent)
    }
}
