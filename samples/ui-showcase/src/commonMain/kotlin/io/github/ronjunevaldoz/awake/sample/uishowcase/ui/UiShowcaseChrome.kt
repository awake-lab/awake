// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnTextLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width

internal fun ColumnScope.drawUiShowcaseSidebar(compact: Boolean) {
    var selectedPage by rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }

    spacer(Modifier.height(12f.dp))
    drawUiShowcaseSidebarMenu(
        compact = compact,
        selectedPageId = selectedPage,
        onSelect = { selectedPage = it.id },
    )
}

internal fun ColumnScope.drawUiShowcasePageContent(
    state: UiShowcaseRuntimeState,
    showInlineMenu: Boolean,
) {
    var selectedPage by rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    val page = showcasePageById(selectedPage)

    if (showInlineMenu) {
        drawUiShowcaseSidebarMenu(
            compact = true,
            selectedPageId = page.id,
            onSelect = { selectedPage = it.id },
        )
        spacer(Modifier.height(12f.dp))
    }

    shadcnBadge(
        id = "ui-showcase-category-${page.id}",
        label = page.category.title.uppercase(),
        variant = ShadcnBadgeVariant.Outline,
    )
    shadcnSectionHeader(title = page.title, description = page.description)
    spacer(Modifier.height(8f.dp))
    drawUiShowcasePreviewCodeSection(page, state)
    if (page.notes.isNotEmpty()) {
        spacer(Modifier.height(12f.dp))
        shadcnSurface(
            id = "ui-showcase-notes-${page.id}",
            modifier = Modifier,
        ) {
            shadcnSectionTitle("Notes")
            shadcnSupportingLines(page.notes)
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
        if (compact) {
            shadcnSidebarMenu {
                pages.forEach { page ->
                    shadcnSidebarMenuItem(
                        id = "ui-showcase-page-${page.id}",
                        label = page.title,
                        active = page.id == selectedPageId,
                        onClick = { onSelect(page) },
                    )
                }
            }
        } else {
            var expanded by rememberStateValue(
                "ui-showcase-sidebar-category",
                category.name,
            ) { true }
            shadcnCollapsible(
                id = "ui-showcase-sidebar-category-${category.name}",
                title = category.title,
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                shadcnSidebarGroup {
                    shadcnSidebarMenu {
                        pages.forEach { page ->
                            // shadcnCollapsible's header text starts at contentPadding(4dp) +
                            // "+/-" icon width(12dp) + row gap(8dp) = 24dp from the shared left
                            // edge. Nested items indent to at least that point (real shadcn/Radix
                            // Accordion content aligns under the trigger label).
                            shadcnSidebarMenuItem(
                                id = "ui-showcase-page-${page.id}",
                                label = page.title,
                                active = page.id == selectedPageId,
                                onClick = { onSelect(page) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ColumnScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    var showCode by rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(
        modifier = Modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
    ) {
        shadcnButton(
            id = "ui-showcase-preview-tab-${page.id}",
            label = "Preview",
            modifier = Modifier.width(96f.dp).height(36f.dp),
            variant = if (!showCode) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        ).also { clicked ->
            if (clicked) showCode = false
        }
        shadcnButton(
            id = "ui-showcase-code-tab-${page.id}",
            label = "Code",
            modifier = Modifier.width(88f.dp).height(36f.dp),
            variant = if (showCode) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        ).also { clicked ->
            if (clicked) showCode = true
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSurface(
        id = "ui-showcase-preview-code-${page.id}",
        modifier = Modifier,
    ) {
        if (showCode) {
            drawUiShowcaseCodeBlock(page.usageCode)
        } else {
            renderUiShowcasePagePreview(page, state)
        }
    }
}

private fun ColumnScope.drawUiShowcaseCodeBlock(code: String) {
    shadcnTextLines(
        lines = code.trimIndent().lines(),
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Clip,
        maxLines = Int.MAX_VALUE,
    )
}
