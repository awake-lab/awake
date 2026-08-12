// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/**
 * Real-measurement probe for the "extra space between menu items" live report on the
 * shadcnSidebarGroup/Menu/MenuItem migration (commit a1aeca4b). Renders the sidebar's real
 * category (non-compact/collapsible) branch both as it exists NOW in production and as it
 * existed pre-migration (reconstructed inline from `git show a1aeca4b^`), on the same real
 * UiContext/shadcnSidebar/shadcnCollapsible plumbing, and measures the real Y gap between
 * consecutive `ui-showcase-page-*` Button semantic nodes.
 */
class UiShowcaseSidebarGapProbeTest {

    @Test
    fun measureRealMenuItemGaps() {
        val category = ShowcasePagesByCategory.entries.first { it.value.size >= 3 }
        val pages = category.value.take(3)

        val before = measureGaps(pages) { onSelect -> drawOldSidebarCategory(category.key.name, pages, onSelect) }
        val after = measureGaps(pages) { onSelect -> drawNewSidebarCategory(category.key.name, pages, onSelect) }

        val report = buildString {
            appendLine("ui-showcase sidebar category '${category.key.title}' menu-item gaps (pre-migration vs current):")
            appendLine("  BEFORE (drawUiShowcaseSidebarPageButton, direct calls): $before")
            appendLine("  AFTER  (shadcnSidebarGroup/Menu/MenuItem):              $after")
        }
        println(report)
    }

    @Test
    fun measureRealMultiCategoryGaps() {
        val before = measureMultiCategory { onSelect ->
            ShowcasePagesByCategory.entries.take(2).forEach { (cat, pages) ->
                drawOldSidebarCategory(cat.name, pages.take(2), onSelect)
            }
        }
        val after = measureMultiCategory { onSelect ->
            ShowcasePagesByCategory.entries.take(2).forEach { (cat, pages) ->
                drawNewSidebarCategory(cat.name, pages.take(2), onSelect)
            }
        }
        println("Multi-category category-to-item gaps:")
        println("  BEFORE: $before")
        println("  AFTER:  $after")
    }

    @Test
    fun measureRealGapsAfterCollapseReexpand() {
        val pages = ShowcasePagesByCategory.values.first().take(2)
        measureAfterToggle(pages) { onSelect ->
            drawNewSidebarCategory(pages.first().category.name, pages, onSelect)
        }
    }

    private fun measureAfterToggle(
        pages: List<ShowcasePage>,
        draw: ColumnScope.((ShowcasePage) -> Unit) -> Unit
    ) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        // Frame 1: mount expanded. Frame 2: force collapsed via state. Frame 3+: force
        // re-expanded -- this is the `expanded && !wasExpanded` re-measurement transition.
        val expandedFlags = listOf(true, false, true, true)
        expandedFlags.forEachIndexed { frame, forcedExpanded ->
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            ui.createUiScope(UiBounds(x = 0f, y = 0f, width = 1440f, height = 900f)).column {
                var expanded by rememberStateValue("probe-sidebar-category", pages.first().category.name) { true }
                expanded = forcedExpanded
                shadcnSidebar(
                    id = "probe-sidebar-toggle",
                    modifier = Modifier.width(264f.dp),
                ) { _ ->
                    draw { }
                }
            }
            ui.endFrame()
            if (frame == expandedFlags.lastIndex) {
                val nodes = ui.semanticNodes()
                    .filter { it.role == UiSemanticRole.Button && it.id?.startsWith("ui-showcase-page-") == true }
                    .sortedBy { it.bounds.y }
                nodes.forEach { n -> println("    id=${n.id} y=%.2f h=%.2f".format(n.bounds.y, n.bounds.height)) }
                if (nodes.size == pages.size) {
                    val gaps = nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) }
                    println("    gaps=$gaps")
                }
            }
        }
    }

    private fun measureMultiCategory(
        draw: ColumnScope.((ShowcasePage) -> Unit) -> Unit
    ): List<Float> {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        val gaps = mutableListOf<List<Float>>()
        listOf(true, false).forEach { forcedExpanded ->
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            ui.createUiScope(UiBounds(x = 0f, y = 0f, width = 1440f, height = 900f)).column {
                var expanded by rememberStateValue("probe-sidebar-category", "multi") { true }
                expanded = forcedExpanded
                shadcnSidebar(
                    id = "probe-sidebar-multi",
                    modifier = Modifier.width(264f.dp),
                ) { _ ->
                    draw { }
                }
            }
            ui.endFrame()
            val nodes = ui.semanticNodes()
                .filter { it.role == UiSemanticRole.Button && (it.id?.contains("category") == true || it.id?.startsWith("ui-showcase-page-") == true) }
                .sortedBy { it.bounds.y }
            gaps += nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) }
        }
        return gaps.flatten()
    }

    @Test
    fun measureRealCompactMenuItemGaps() {
        val category = ShowcasePagesByCategory.entries.first { it.value.size >= 3 }
        val pages = category.value.take(3)
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        ui.pushTheme(theme)
        ui.createUiScope(UiBounds(x = 0f, y = 0f, width = 1440f, height = 900f)).shadcnSidebar(
            id = "probe-sidebar-compact",
            modifier = Modifier.width(64f.dp),
            expanded = false,
        ) { _ ->
            shadcnSidebarGroup {
                shadcnSidebarMenu {
                    pages.forEach { page ->
                        shadcnSidebarMenuItem(
                            id = "ui-showcase-page-${page.id}",
                            label = page.title,
                            active = false,
                            onClick = { },
                        )
                    }
                }
            }
        }
        ui.endFrame()
        val nodes = ui.semanticNodes()
            .filter { it.role == UiSemanticRole.Button && it.id?.startsWith("ui-showcase-page-") == true }
            .sortedBy { it.bounds.y }
        val gaps = nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) }
        println("Compact sidebar gaps: $gaps")
    }

    private fun measureGaps(
        pages: List<ShowcasePage>,
        draw: ColumnScope.((ShowcasePage) -> Unit) -> Unit,
    ): List<Float> {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        var yGaps: List<Float> = emptyList()
        repeat(3) {
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            ui.createUiScope(UiBounds(x = 0f, y = 0f, width = 1440f, height = 900f)).column {
                shadcnSidebar(
                    id = "probe-sidebar",
                    modifier = Modifier.width(264f.dp),
                ) { _ ->
                    draw { }
                }
            }
            ui.endFrame()
            val nodes = ui.semanticNodes()
                .filter { it.role == UiSemanticRole.Button && it.id?.startsWith("ui-showcase-page-") == true }
                .sortedBy { it.bounds.y }
            if (nodes.size == pages.size) {
                yGaps = nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) }
            }
        }
        return yGaps
    }
}

internal fun ColumnScope.drawOldSidebarCategory(
    categoryName: String,
    pages: List<ShowcasePage>,
    onSelect: (ShowcasePage) -> Unit,
) {
    var expanded by rememberStateValue("probe-sidebar-category", categoryName) { true }
    shadcnCollapsible(
        id = "probe-sidebar-category-$categoryName",
        title = categoryName,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        pages.forEach { page ->
            oldSidebarPageButton(page, onSelect)
        }
    }
}

private fun ColumnScope.oldSidebarPageButton(
    page: ShowcasePage,
    onSelect: (ShowcasePage) -> Unit,
) {
    if (
        shadcnButton(
            id = "ui-showcase-page-${page.id}",
            label = page.title,
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            variant = ShadcnButtonVariant.Ghost,
            centered = false,
        )
    ) {
        onSelect(page)
    }
}

/** Current (post a1aeca4b) production structure. */
internal fun ColumnScope.drawNewSidebarCategory(
    categoryName: String,
    pages: List<ShowcasePage>,
    onSelect: (ShowcasePage) -> Unit,
) {
    var expanded by rememberStateValue("probe-sidebar-category", categoryName) { true }
    shadcnCollapsible(
        id = "probe-sidebar-category-$categoryName",
        title = categoryName,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        shadcnSidebarGroup {
            shadcnSidebarMenu {
                pages.forEach { page ->
                    shadcnSidebarMenuItem(
                        id = "ui-showcase-page-${page.id}",
                        label = page.title,
                        active = false,
                        onClick = { onSelect(page) },
                    )
                }
            }
        }
    }
}
