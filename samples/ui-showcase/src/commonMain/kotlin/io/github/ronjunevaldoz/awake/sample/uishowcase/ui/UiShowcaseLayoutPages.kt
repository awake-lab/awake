package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.headless.HeadlessCanvasGradient
import io.github.ronjunevaldoz.awake.ui.headless.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width

internal fun ColumnScope.drawUiShowcaseLayoutPreview() {
    shadcnBadge(id = "showcase-badge-layout", label = "LAYOUT", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Rows, columns, and slots are owned by Headless; the design system only supplies component visuals.")
    row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(40f.dp)) {
        shadcnButton(id = "layout-row-a", label = "A", modifier = Modifier.height(40f.dp), variant = ShadcnButtonVariant.Primary)
        shadcnButton(id = "layout-row-b", label = "B", modifier = Modifier.height(40f.dp), variant = ShadcnButtonVariant.Secondary)
        shadcnButton(id = "layout-row-c", label = "C", modifier = Modifier.height(40f.dp), variant = ShadcnButtonVariant.Outline)
    }
}

internal fun ColumnScope.drawUiShowcaseScrollPanelPreview() {
    shadcnSupportingText("Scrolling is a Headless modifier backed by a runtime-neutral state handle.")
    spacer(Modifier.height(8f.dp))
    column(
        modifier = Modifier.fillMaxWidth().height(176f.dp),
        verticalArrangement = Arrangement.spacedBy(4f.dp),
    ) {
        repeat(10) { index ->
            shadcnButton(
                id = "showcase-scroll-row-$index",
                label = "Inspector row ${index + 1}",
                modifier = Modifier.fillMaxWidth().height(32f.dp),
                variant = if (index % 2 == 0) ShadcnButtonVariant.Outline else ShadcnButtonVariant.Ghost,
            )
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(listOf("The scroll thumb appears only when content exceeds the viewport."))
}

internal fun ColumnScope.drawUiShowcaseCanvasPreview() {
    shadcnBadge(id = "showcase-badge-canvas", label = "CANVAS", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Custom drawing remains an explicit advanced API; ordinary showcase components stay on Headless slots.")
    spacer(Modifier.height(8f.dp))
    shadcnSurface(
        id = "showcase-canvas-page",
        modifier = Modifier.width(420f.dp).height(220f.dp),
    ) {
        canvas(id = "showcase-canvas-root", modifier = Modifier.width(360f.dp).height(190f.dp)) {
            val colors = themeValues.colors
            drawGradientRect(
                x = 0f,
                y = 0f,
                width = bounds.width,
                height = 56f,
                gradient = HeadlessCanvasGradient(colors.primary.withAlpha(0.16f), colors.accent.withAlpha(0.22f)),
            )
            clipCircle(x = 20f, y = 18f, diameter = 48f) {
                drawRect(20f, 18f, 48f, 48f, colors.accent)
            }
            nested(x = 188f, y = 82f, width = 92f, height = 92f) {
                drawRoundRect(0f, 0f, 92f, 92f, colors.muted, radius = themeValues.shapes.md)
                drawLine(12f, 70f, 78f, 22f, colors.primary, strokeWidth = 2f.dp)
            }
        }
    }
}
