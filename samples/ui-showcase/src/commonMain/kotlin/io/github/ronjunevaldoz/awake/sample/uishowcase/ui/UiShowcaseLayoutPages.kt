// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.verticalScroll

internal fun ColumnScope.drawUiShowcaseLayoutPreview() {
    awakeShadcnSupportingText("row(...) advances a cursor along the horizontal axis; each child claims the next slot in call order.")
    spacer(UiModifier().height(8f.dp))
    row(height = 48f.dp, horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
        surface(id = "layout-row-a", width = Dimension.Fixed(80f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.primary) }) { }
        surface(id = "layout-row-b", width = Dimension.Fixed(120f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.secondary) }) { }
        surface(id = "layout-row-c", width = Dimension.Fixed(160f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.muted) }) { }
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSupportingText("column(...) advances a cursor along the vertical axis -- the default layout for every page in this catalog.")
    spacer(UiModifier().height(8f.dp))
    column(
        height = Dimension.Fixed(112f.dp),
        width = Dimension.Fixed(200f.dp),
        verticalArrangement = Arrangement.spacedBy(6f.dp)
    ) {
        surface(id = "layout-col-a", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.primary) }) { }
        surface(id = "layout-col-b", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.secondary) }) { }
        surface(id = "layout-col-c", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.muted) }) { }
    }
}

internal fun ColumnScope.drawUiShowcaseScrollPanelPreview() {
    val scrollState = context.rememberScrollState("ui-showcase-scroll-panel")
    awakeShadcnSupportingText("Scrollable containers own clipping, content measurement, and the scrollbar lane so callers do not have to reimplement any of it.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSurface(
        id = "showcase-scroll-panel-page",
        width = Dimension.Fixed(420f.dp),
        height = Dimension.Fixed(176f.dp),
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) { _ ->
        column(
            id = "scroll-container",
            modifier = UiModifier()
                .fillMaxWidth()
                .height(176f.dp) // TODO missing fillParentHeight
                .verticalScroll(scrollState),
        ) {
            repeat(10) { index ->
                awakeShadcnButton(
                    id = "showcase-scroll-row-$index",
                    label = "Inspector row ${index + 1}",
                    modifier = UiModifier().fillMaxWidth().height(32f.dp),
                    variant = if (index % 2 == 0) AwakeShadcnButtonVariant.Outline else AwakeShadcnButtonVariant.Ghost
                )
            }
        }
    }
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "The scroll thumb only appears when content actually exceeds the viewport.",
            "The widget-level preview report keeps a static clipped state around so we can catch scrollbar and clipping drift without manual scrolling."
        )
    )
}
