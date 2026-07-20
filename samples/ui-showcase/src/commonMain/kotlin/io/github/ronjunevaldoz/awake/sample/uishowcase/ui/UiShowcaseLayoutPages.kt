// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnScrollSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.width

internal fun UiColumnDslScope.drawUiShowcaseLayoutPreview() {
    awakeShadcnSectionTitle("Row")
    awakeShadcnSupportingText("row(...) advances a cursor along the horizontal axis; each child claims the next slot in call order.")
    spacer(UiModifier().height(8f.dp))
    row(height = 48f.dp, gap = 8f) {
        panel(id = "layout-row-a", width = Dimension.Fixed(80f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.primary) }) { }
        panel(id = "layout-row-b", width = Dimension.Fixed(120f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.secondary) }) { }
        panel(id = "layout-row-c", width = Dimension.Fixed(160f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.muted) }) { }
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSectionTitle("Column")
    awakeShadcnSupportingText("column(...) advances a cursor along the vertical axis -- the default layout for every page in this catalog.")
    spacer(UiModifier().height(8f.dp))
    column(height = Dimension.Fixed(112f.dp), width = Dimension.Fixed(200f.dp), gap = 6f) {
        panel(id = "layout-col-a", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.primary) }) { }
        panel(id = "layout-col-b", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.secondary) }) { }
        panel(id = "layout-col-c", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.muted) }) { }
    }
}

internal fun UiColumnDslScope.drawUiShowcaseScrollPanelPreview() {
    val scrollState = context.rememberStateValue("ui-showcase-scroll-panel", "state") { UiScrollState() }

    awakeShadcnSectionTitle("Scroll Panel")
    awakeShadcnSupportingText("Scrollable containers own clipping, content measurement, and the scrollbar lane so callers do not have to reimplement any of it.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnScrollSurface(
        id = "showcase-scroll-panel-page",
        width = Dimension.Fixed(420f.dp),
        height = Dimension.Fixed(176f.dp),
        state = scrollState.value,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) { _ ->
        repeat(10) { index ->
            awakeShadcnButton(
                id = "showcase-scroll-row-$index",
                label = "Inspector row ${index + 1}",
                modifier = UiModifier().width(360f.dp).height(32f.dp),
                variant = if (index % 2 == 0) AwakeShadcnButtonVariant.Outline else AwakeShadcnButtonVariant.Ghost
            )
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
