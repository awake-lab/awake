// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.textLines
import io.github.ronjunevaldoz.awake.ui.width

internal fun UiColumnDslScope.drawUiShowcaseSidebar(compact: Boolean) {
    val selectedPage = context.rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    awakeShadcnBadge("SHADCN", variant = AwakeShadcnBadgeVariant.Primary)
    awakeShadcnHeadline("Catalog")
    awakeShadcnSupportingText(
        if (compact) {
            "Choose one page at a time."
        } else {
            "Grouped component and pattern pages, following the shadcn-compose catalog layout."
        }
    )
    spacer(UiModifier().height(12f.dp))
    drawUiShowcaseSidebarMenu(
        compact = compact,
        selectedPageId = selectedPage.value,
        onSelect = { selectedPage.value = it.id }
    )
}

internal fun UiColumnDslScope.drawUiShowcasePageContent(
    state: UiShowcaseRuntimeState,
    showInlineMenu: Boolean,
) {
    val selectedPage = context.rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    val page = showcasePageById(selectedPage.value)

    if (showInlineMenu) {
        drawUiShowcaseSidebarMenu(
            compact = true,
            selectedPageId = page.id,
            onSelect = { selectedPage.value = it.id }
        )
        spacer(UiModifier().height(12f.dp))
    }

    awakeShadcnBadge(page.category.title.uppercase(), variant = AwakeShadcnBadgeVariant.Outline)
    awakeShadcnSectionHeader(
        title = { awakeShadcnSectionTitle(page.title) },
        description = { awakeShadcnBodyText(page.description) }
    )
    spacer(UiModifier().height(8f.dp))
    drawUiShowcasePreviewCodeSection(page, state)
    spacer(UiModifier().height(12f.dp))
    awakeShadcnSurface(
        id = "ui-showcase-usage-${page.id}",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionTitle("Usage")
        drawUiShowcaseCodeBlock(page.usageCode)
    }
    if (page.notes.isNotEmpty()) {
        spacer(UiModifier().height(12f.dp))
        awakeShadcnSurface(
            id = "ui-showcase-notes-${page.id}",
            height = Dimension.WrapContent,
            variant = AwakeShadcnSurfaceVariant.Card,
            style = Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Notes")
            supportingLines(page.notes)
        }
    }
}

internal fun UiColumnDslScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    page.renderPreview(this, state)
}

private fun UiColumnDslScope.drawUiShowcaseSidebarMenu(
    compact: Boolean,
    selectedPageId: String,
    onSelect: (ShowcasePage) -> Unit,
) {
    ShowcasePagesByCategory.forEach { (category, pages) ->
        if (!compact) {
            awakeShadcnSectionTitle(category.title)
            spacer(UiModifier().height(4f.dp))
        }
        pages.forEach { page ->
            if (
                awakeShadcnButton(
                    id = "ui-showcase-page-${page.id}",
                    label = page.title,
                    modifier = UiModifier()
                        .fillMaxWidth()
                        .height(36f.dp),
                    style = Style {
                        contentPadding(start = 14f.dp, top = 0f.dp, end = 14f.dp, bottom = 0f.dp)
                    },
                    variant = if (page.id == selectedPageId) {
                        AwakeShadcnButtonVariant.Primary
                    } else {
                        AwakeShadcnButtonVariant.Ghost
                    },
                    centered = false,
                    verticallyCentered = true
                )
            ) {
                onSelect(page)
            }
        }
        spacer(UiModifier().height(if (compact) 8f.dp else 12f.dp))
    }
}

private fun UiColumnDslScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    val showCode = context.rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(height = 36f.dp, gap = 8f) {
        awakeShadcnButton(
            id = "ui-showcase-preview-tab-${page.id}",
            label = "Preview",
            modifier = UiModifier().width(96f.dp).height(36f.dp),
            variant = if (!showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = false
        }
        awakeShadcnButton(
            id = "ui-showcase-code-tab-${page.id}",
            label = "Code",
            modifier = UiModifier().width(88f.dp).height(36f.dp),
            variant = if (showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = true
        }
    }
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSurface(
        id = "ui-showcase-preview-code-${page.id}",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) {
        if (showCode.value) {
            drawUiShowcaseCodeBlock(page.usageCode)
        } else {
            renderUiShowcasePagePreview(page, state)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseCodeBlock(code: String) {
    textLines(
        lines = code.trimIndent().lines(),
        style = Style {
            foreground(theme.tokens.foreground)
            textSize(theme.typography.label)
        },
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Clip,
        maxLines = Int.MAX_VALUE
    )
}
