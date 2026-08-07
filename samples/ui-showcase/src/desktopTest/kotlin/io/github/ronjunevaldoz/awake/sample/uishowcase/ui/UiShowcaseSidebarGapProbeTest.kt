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
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.style.Style
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

        check(before.isNotEmpty() && after.isNotEmpty()) { "measured zero items -- probe broken" }
    }

    @Test
    fun measureRealMultiCategoryGaps() {
        val categories = ShowcasePagesByCategory.entries.take(3)

        println("BEFORE (pre-migration, direct drawUiShowcaseSidebarPageButton calls):")
        measureMultiCategory { onSelect ->
            categories.forEach { (cat, pages) -> drawOldSidebarCategory(cat.name, pages.take(2), onSelect) }
        }

        println("AFTER (current, shadcnSidebarGroup/Menu/MenuItem):")
        measureMultiCategory { onSelect ->
            categories.forEach { (cat, pages) -> drawNewSidebarCategory(cat.name, pages.take(2), onSelect) }
        }
    }

    /** Exercises the collapse -> re-expand transition (the specific `animatedHeight`
     * re-measurement path flagged for investigation), not just the initial-mount measurement
     * frame the other probes above use. */
    @Test
    fun measureRealGapsAfterCollapseReexpand() {
        val category = ShowcasePagesByCategory.entries.first { it.value.size >= 3 }
        val pages = category.value.take(3)

        println("BEFORE, after collapse->re-expand:")
        measureAfterToggle(pages) { onSelect -> drawOldSidebarCategory(category.key.name, pages, onSelect) }
        println("AFTER, after collapse->re-expand:")
        measureAfterToggle(pages) { onSelect -> drawNewSidebarCategory(category.key.name, pages, onSelect) }
    }

    private fun measureAfterToggle(
        pages: List<ShowcasePage>,
        draw: ColumnScope.((ShowcasePage) -> Unit) -> Unit,
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
            ui.rememberStateValue("probe-sidebar-category", pages.first().category.name) { true }.value = forcedExpanded
            ui.createBox(x = 0f, y = 0f, width = 1440f, height = 900f).run {
                shadcnSidebar(
                    id = "probe-sidebar-toggle",
                    style = Style { shape(16f.dp) },
                    modifier = Modifier.width(264f.dp.toDimension()).height(Dimension.FillMax),
                ) {
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

    private fun measureMultiCategory(draw: ColumnScope.((ShowcasePage) -> Unit) -> Unit) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        repeat(2) { frame ->
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            ui.createBox(x = 0f, y = 0f, width = 1440f, height = 900f).run {
                shadcnSidebar(
                    id = "probe-sidebar-multi",
                    style = Style { shape(16f.dp) },
                    modifier = Modifier.width(264f.dp.toDimension()).height(Dimension.FillMax),
                ) {
                    draw { }
                }
            }
            ui.endFrame()
            if (frame == 1) {
                val nodes = ui.semanticNodes()
                    .filter {
                        (it.role == UiSemanticRole.Button) &&
                            (it.id?.startsWith("ui-showcase-page-") == true || it.id?.endsWith(".header") == true)
                    }
                    .sortedBy { it.bounds.y }
                nodes.forEach { n ->
                    println("    id=${n.id} y=%.2f h=%.2f".format(n.bounds.y, n.bounds.height))
                }
            }
        }
    }

    /** Compact-mode path (no shadcnCollapsible/shadcnSidebarGroup wrapping -- just
     * shadcnSidebarMenu directly, or the flat pages.forEach in the pre-migration version). */
    @Test
    fun measureRealCompactMenuItemGaps() {
        val category = ShowcasePagesByCategory.entries.first { it.value.size >= 3 }
        val pages = category.value.take(3)

        val before = measureGaps(pages) { onSelect ->
            pages.forEach { page -> oldSidebarPageButton(page, onSelect, startPadding = 14f.dp) }
        }
        val after = measureGaps(pages) { onSelect ->
            shadcnSidebarMenu {
                pages.forEach { page ->
                    shadcnSidebarMenuItem(id = "ui-showcase-page-${page.id}", label = page.title, active = false, onClick = { onSelect(page) })
                }
            }
        }

        println("ui-showcase sidebar COMPACT-mode menu-item gaps (pre-migration vs current):")
        println("  BEFORE (direct pages.forEach): $before")
        println("  AFTER  (shadcnSidebarMenu):    $after")
        check(before.isNotEmpty() && after.isNotEmpty())
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
            ui.createBox(x = 0f, y = 0f, width = 1440f, height = 900f).run {
                shadcnSidebar(
                    id = "probe-sidebar",
                    style = Style { shape(16f.dp) },
                    modifier = Modifier.width(264f.dp.toDimension()).height(Dimension.FillMax),
                ) {
                    draw { }
                }
            }
            val primitives = ui.endFrame()
            val nodes = ui.semanticNodes()
                .filter { it.role == UiSemanticRole.Button && it.id?.startsWith("ui-showcase-page-") == true }
                .sortedBy { it.bounds.y }
            if (nodes.size == pages.size) {
                yGaps = nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) }
                println("  bounds: " + nodes.joinToString { "y=%.2f h=%.2f".format(it.bounds.y, it.bounds.height) })
            }
            check(primitives.isNotEmpty())
        }
        return yGaps
    }
}

/** Reconstruction of the pre-a1aeca4b `drawUiShowcaseSidebarPageButton` + direct `pages.forEach`
 * inside `shadcnCollapsible`, exactly as it existed at `a1aeca4b^`. */
private fun ColumnScope.drawOldSidebarCategory(
    categoryName: String,
    pages: List<ShowcasePage>,
    onSelect: (ShowcasePage) -> Unit,
) {
    var expanded by context.rememberStateValue("probe-sidebar-category", categoryName) { true }
    shadcnCollapsible(
        id = "probe-sidebar-category-$categoryName",
        title = categoryName,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        pages.forEach { page ->
            oldSidebarPageButton(page, onSelect, startPadding = 24f.dp)
        }
    }
}

private fun ColumnScope.oldSidebarPageButton(
    page: ShowcasePage,
    onSelect: (ShowcasePage) -> Unit,
    startPadding: io.github.ronjunevaldoz.awake.ui.Dp,
) {
    if (
        shadcnButton(
            id = "ui-showcase-page-${page.id}",
            label = page.title,
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            style = Style { contentPadding(start = startPadding, top = 0f.dp, end = 14f.dp, bottom = 0f.dp) },
            variant = ShadcnButtonVariant.Ghost,
            centered = false,
            verticallyCentered = true,
        )
    ) {
        onSelect(page)
    }
}

/** Current (post a1aeca4b) production structure. */
private fun ColumnScope.drawNewSidebarCategory(
    categoryName: String,
    pages: List<ShowcasePage>,
    onSelect: (ShowcasePage) -> Unit,
) {
    var expanded by context.rememberStateValue("probe-sidebar-category", categoryName) { true }
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
                        style = Style { contentPadding(start = 24f.dp, top = 0f.dp, end = 14f.dp, bottom = 0f.dp) },
                        onClick = { onSelect(page) },
                    )
                }
            }
        }
    }
}
