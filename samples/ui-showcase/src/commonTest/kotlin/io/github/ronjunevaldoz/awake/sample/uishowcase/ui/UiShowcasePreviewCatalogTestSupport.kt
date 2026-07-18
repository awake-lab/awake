// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.ui

internal val UiShowcasePreviewEntries: List<AwakeUiPreviewEntry> = listOf(
    UiShowcaseOverviewPreview,
    UiShowcaseReferencePreview,
    UiShowcaseThemePreview,
    UiShowcaseFontsPreview,
    UiShowcaseLayoutPreview,
    UiShowcaseSlotApisPreview,
    UiShowcaseButtonsPreview,
    UiShowcasePopupsPreview,
    UiShowcaseStatePreview
)

@AwakeUiPreview(
    id = "ui-showcase-overview",
    title = "Overview",
    group = "Getting Started",
    summary = "Docs-style shell overview for the Awake shadcn showcase.",
    width = 900,
    height = 560
)
internal object UiShowcaseOverviewPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "introduction")
}

@AwakeUiPreview(
    id = "ui-showcase-reference",
    title = "Reference Comparison",
    group = "Getting Started",
    summary = "Side-by-side visual cues we are matching against the shadcn reference.",
    width = 900,
    height = 620
)
internal object UiShowcaseReferencePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "reference")
}

@AwakeUiPreview(
    id = "ui-showcase-theming",
    title = "Theme Controls",
    group = "Getting Started",
    summary = "Preset, base color, accent, and dark-mode controls sharing the same Awake theme factory.",
    width = 900,
    height = 660
)
internal object UiShowcaseThemePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "theming")
}

@AwakeUiPreview(
    id = "ui-showcase-fonts",
    title = "Bitmap And True Font",
    group = "Foundations",
    summary = "Direct specimen comparison between the bitmap default and the new TTF-derived runtime font.",
    width = 920,
    height = 620
)
internal object UiShowcaseFontsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "fonts")
}

@AwakeUiPreview(
    id = "ui-showcase-layout",
    title = "Layout Primitives",
    group = "Foundations",
    summary = "row/column/spacer, the modifier-first layout primitives every other page is built from.",
    width = 900,
    height = 460
)
internal object UiShowcaseLayoutPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "layout")
}

@AwakeUiPreview(
    id = "ui-showcase-slot-apis",
    title = "Slot APIs",
    group = "Patterns",
    summary = "buttonSlot(...)'s content-lambda form composing arbitrary content inside a widget's own claimed slot.",
    width = 900,
    height = 360
)
internal object UiShowcaseSlotApisPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "slot-apis")
}

@AwakeUiPreview(
    id = "ui-showcase-buttons",
    title = "Buttons And Badges",
    group = "Foundations",
    summary = "Core action and status components rendered with the current Awake shadcn recipe.",
    width = 900,
    height = 560
)
internal object UiShowcaseButtonsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "buttons")
}

@AwakeUiPreview(
    id = "ui-showcase-popups",
    title = "Popup Components",
    group = "Overlays",
    summary = "Dropdown and dialog proofs rendered through the shared DSL surface.",
    width = 900,
    height = 620
)
internal object UiShowcasePopupsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "popups")
}

@AwakeUiPreview(
    id = "ui-showcase-state",
    title = "State Container",
    group = "Patterns",
    summary = "Reducer-backed counter preview showing Awake's small MVI path in the sample catalog.",
    width = 900,
    height = 560
)
internal object UiShowcaseStatePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "state")
}

private fun renderUiShowcasePagePreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    pageId: String,
): AwakeUiPreviewFrame {
    val state = UiShowcaseRuntimeState()
    val theme = state.showcaseTheme()
    val font = UiFonts.default()
    val page = ShowcasePages.firstOrNull { it.id == pageId } ?: error("Unknown showcase page: $pageId")
    val ui = UiContext()

    Input.setPointer(down = false, x = -100f, y = -100f)
    ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat())
    ui.ui(
        x = 24f,
        y = 24f,
        width = (metadata.width - 48).toFloat(),
        font = font,
        theme = theme,
        gap = 10f
    ) {
        awakeShadcnSurface(
            id = "ui-showcase-preview-${page.id}",
            width = Dimension.FillMax,
            height = Dimension.WrapContent,
            variant = AwakeShadcnSurfaceVariant.Card,
            style = Style { shape(16f.dp) }
        ) {
            awakeShadcnBadge(page.category.title.uppercase(), variant = AwakeShadcnBadgeVariant.Outline)
            awakeShadcnSectionHeader(
                title = page.title,
                description = page.description
            )
            spacer(UiModifier().height(10f.dp))
            renderUiShowcasePagePreview(page, state)
        }
    }

    return AwakeUiPreviewFrame(
        primitives = ui.endFrame(),
        background = theme.tokens.background,
        font = font,
        semantics = ui.semanticNodes()
    )
}
