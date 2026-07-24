// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.textLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseSidebar(compact: Boolean) {
    val selectedPage = context.rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    shadcnBadge("SHADCN", variant = ShadcnBadgeVariant.Primary)
    shadcnHeadline("Catalog")
    shadcnSupportingText(
        if (compact) {
            "Choose one page at a time."
        } else {
            "Grouped component and pattern pages, following the shadcn-compose catalog layout."
        }
    )
    spacer(Modifier.height(12f.dp))
    drawUiShowcaseSidebarMenu(
        compact = compact,
        selectedPageId = selectedPage.value,
        onSelect = { selectedPage.value = it.id }
    )
}

internal fun ColumnScope.drawUiShowcasePageContent(
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
        spacer(Modifier.height(12f.dp))
    }

    shadcnBadge(page.category.title.uppercase(), variant = ShadcnBadgeVariant.Outline)
    shadcnSectionHeader(
        title = { shadcnSectionTitle(page.title) },
        description = { shadcnBodyText(page.description) }
    )
    spacer(Modifier.height(8f.dp))
    drawUiShowcasePreviewCodeSection(page, state)
    if (page.notes.isNotEmpty()) {
        spacer(Modifier.height(12f.dp))
        shadcnSurface(
            id = "ui-showcase-notes-${page.id}",
            variant = ShadcnSurfaceVariant.Card,
            style = Style { shape(14f.dp) }
        , modifier = Modifier.copy(heightDimension = Dimension.WrapContent)) {
            shadcnSectionTitle("Notes")
            supportingLines(page.notes)
        }
    }
}

internal fun ColumnScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    page.renderPreview(this, state)
}

private fun ColumnScope.drawUiShowcaseSidebarMenu(
    compact: Boolean,
    selectedPageId: String,
    onSelect: (ShowcasePage) -> Unit,
) {
    ShowcasePagesByCategory.forEach { (category, pages) ->
        if (!compact) {
            shadcnSectionTitle(category.title)
            spacer(Modifier.height(4f.dp))
        }
        pages.forEach { page ->
            if (
                shadcnButton(
                    id = "ui-showcase-page-${page.id}",
                    label = page.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36f.dp),
                    style = Style {
                        contentPadding(start = 14f.dp, top = 0f.dp, end = 14f.dp, bottom = 0f.dp)
                    },
                    variant = if (page.id == selectedPageId) {
                        ShadcnButtonVariant.Primary
                    } else {
                        ShadcnButtonVariant.Ghost
                    },
                    centered = false,
                    verticallyCentered = true
                )
            ) {
                onSelect(page)
            }
        }
        spacer(Modifier.height(if (compact) 8f.dp else 12f.dp))
    }
}

private fun ColumnScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    val showCode = context.rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(
        modifier = Modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(8f.dp)
    ) {
        shadcnButton(
            id = "ui-showcase-preview-tab-${page.id}",
            label = "Preview",
            modifier = Modifier.width(96f.dp).height(36f.dp),
            variant = if (!showCode.value) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = false
        }
        shadcnButton(
            id = "ui-showcase-code-tab-${page.id}",
            label = "Code",
            modifier = Modifier.width(88f.dp).height(36f.dp),
            variant = if (showCode.value) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = true
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSurface(
        id = "ui-showcase-preview-code-${page.id}",
        variant = ShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    , modifier = Modifier.copy(heightDimension = Dimension.WrapContent)) {
        if (showCode.value) {
            drawUiShowcaseCodeBlock(page.usageCode)
        } else {
            renderUiShowcasePagePreview(page, state)
        }
    }
}

private fun ColumnScope.drawUiShowcaseCodeBlock(code: String) {
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
