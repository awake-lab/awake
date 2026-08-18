// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuSub
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuSubItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.heightIn
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.width

/** Floor for a page hero so short samples keep the preview card a consistent size. */
private val HERO_MIN_HEIGHT = 160f.dp

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
    if (page.status == ShowcaseStatus.Placeholder) {
        shadcnBadge(
            id = "ui-showcase-status-${page.id}",
            label = "NOT IMPLEMENTED",
            variant = ShadcnBadgeVariant.Danger,
        )
    }
    shadcnSectionTitle(title = page.title, description = page.description)
    spacer(Modifier.height(8f.dp))
    drawUiShowcasePreviewCodeSection(page, state)
    if (page.notes.isNotEmpty()) {
        spacer(Modifier.height(12f.dp))
        shadcnSurface(
            id = "ui-showcase-notes-${page.id}",
            modifier = Modifier,
            // Static text per page -- the id already varies with the page, so a constant key
            // makes every frame after the first a sizing-cache hit.
            cacheKey = page.id,
        ) {
            shadcnSectionTitle("Notes")
            shadcnTextLines(page.notes)
        }
    }
}

/**
 * Renders a page's sample sections in catalog order: interactive hero, then the static variant
 * and state matrices. A page that declares neither matrix renders the hero alone -- no empty
 * section boxes.
 */
internal fun ColumnScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    // The hero owns the full width of the preview card and never collapses below
    // [HERO_MIN_HEIGHT]. Left to wrap its content, a one-widget hero (a lone button, a single
    // avatar row) shrank the card around it, so the catalog's preview area jumped size from
    // page to page and short heroes read as broken rather than compact.
    column(
        modifier = Modifier.fillMaxWidth().heightIn(min = HERO_MIN_HEIGHT),
    ) {
        page.hero(this, state)
    }
    page.variants?.let { renderer ->
        spacer(Modifier.height(16f.dp))
        shadcnSectionTitle("Variants")
        spacer(Modifier.height(8f.dp))
        renderer(this, state)
    }
    page.states?.let { renderer ->
        spacer(Modifier.height(16f.dp))
        shadcnSectionTitle("States")
        spacer(Modifier.height(8f.dp))
        renderer(this, state)
    }
}

private fun ColumnScope.drawUiShowcaseSidebarMenu(
    compact: Boolean,
    selectedPageId: String,
    onSelect: (ShowcasePage) -> Unit,
) {
    ShowcasePagesByCategory.forEach { (category, pages) ->
        if (compact) {
            shadcnSidebarMenu(
                id = "ui-showcase-menu-${category.name}",
                // Item labels and count are static per category; the active highlight changes
                // colors only, never measured size, so it stays out of the key.
                cacheKey = category.name,
            ) {
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
                shadcnSidebarMenuSub(
                    id = "ui-showcase-submenu-${category.name}",
                    cacheKey = category.name,
                ) {
                    pages.forEach { page ->
                        shadcnSidebarMenuSubItem(
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

private fun ColumnScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    var showCode by rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(
        modifier = Modifier.height(36f.dp),
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
    shadcnTextLines(lines = code.trimIndent().lines())
}
