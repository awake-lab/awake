// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewScene
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/**
 * Visual (not just bounds) proof for the "extra space between menu items" live report, requested
 * as a follow-up to [UiShowcaseSidebarGapProbeTest]'s bounds-only probe. Renders the REAL
 * production app-shell sidebar -- [drawUiShowcaseSidebar] with all category groups, exactly as
 * [UiShowcaseUi.kt]'s non-compact row branch composes it (`shadcnSidebar` at 264dp width inside a
 * padded fillMaxSize row) -- through the same rasterize-to-PNG pipeline every other preview in
 * this repo uses ([saveAwakeUiPreview]), plus a reconstruction of the pre-migration
 * (`a1aeca4b^`) sidebar for a real side-by-side. Output lands in `build/ui-previews/` like every
 * other preview PNG.
 */
class UiShowcaseSidebarRealChromePngTest {

    @Test
    fun dumpRealChromeSidebarPngs() {
        saveRealChromeSidebar(id = "debug-real-chrome-sidebar-new") { drawUiShowcaseSidebar(compact = false) }
        saveRealChromeSidebar(id = "debug-real-chrome-sidebar-old") { drawOldUiShowcaseSidebar(compact = false) }
    }

    private fun saveRealChromeSidebar(id: String, content: ColumnScope.() -> Unit) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default(cellSize = 12)
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        // Mirrors UiShowcaseUi.kt's non-compact `row { shadcnSidebar(...) { drawUiShowcaseSidebar(compact = false) } }`
        // shell: fillMaxSize + 24dp outer padding, sidebar pinned to 264dp width / FillMax height.
        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.row(
            modifier = Modifier.fillMaxSize().padding(24f.dp).width(Dimension.FillMax).height(Dimension.FillMax),
            horizontalArrangement = Arrangement.spacedBy(20f.dp),
        ) {
            shadcnSidebar(
                id = "ui-showcase-sidebar",
                style = Style { shape(16f.dp) },
                modifier = Modifier.width(264f.dp.toDimension()).height(Dimension.FillMax),
            ) {
                content()
            }
        }

        val scene = AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = id,
                title = id,
                group = "Debug",
                summary = "Throwaway real-chrome sidebar visual probe.",
                width = 1440,
                height = 900,
                reportScale = 1,
            ),
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
        saveAwakeUiPreview(scene)

        val nodes = ui.semanticNodes()
            .filter { it.role == UiSemanticRole.Button && it.id?.startsWith("ui-showcase-page-") == true }
            .sortedBy { it.bounds.y }
        println("$id gaps: " + nodes.zipWithNext { a, b -> b.bounds.y - (a.bounds.y + a.bounds.height) })
    }
}

/** Reconstruction of the pre-a1aeca4b `drawUiShowcaseSidebar`, from `git show a1aeca4b^`. */
private fun ColumnScope.drawOldUiShowcaseSidebar(compact: Boolean) {
    var selectedPage by rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    shadcnBadge("SHADCN", variant = ShadcnBadgeVariant.Primary)
    shadcnHeadline("Catalog")
    shadcnSupportingText(
        if (compact) "Choose one page at a time." else "Grouped component and pattern pages, following the shadcn-compose catalog layout.",
    )
    spacer(Modifier.height(12f.dp))
    ShowcasePagesByCategory.forEach { (category, pages) ->
        if (compact) {
            pages.forEach { page -> drawOldSidebarPageButton(page, selectedPage) { selectedPage = it.id } }
            spacer(Modifier.height(8f.dp))
        } else {
            var expanded by context.rememberStateValue("ui-showcase-sidebar-category", category.name) { true }
            shadcnCollapsible(
                id = "ui-showcase-sidebar-category-${category.name}",
                title = category.title,
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                pages.forEach { page ->
                    drawOldSidebarPageButton(page, selectedPage, startPadding = 24f.dp) { selectedPage = it.id }
                }
            }
            spacer(Modifier.height(12f.dp))
        }
    }
}

private fun ColumnScope.drawOldSidebarPageButton(
    page: ShowcasePage,
    selectedPageId: String,
    startPadding: io.github.ronjunevaldoz.awake.ui.api.Dp = 14f.dp,
    onSelect: (ShowcasePage) -> Unit,
) {
    if (
        shadcnButton(
            id = "ui-showcase-page-${page.id}",
            label = page.title,
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            style = Style { contentPadding(start = startPadding, top = 0f.dp, end = 14f.dp, bottom = 0f.dp) },
            variant = if (page.id == selectedPageId) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
            centered = false,
            verticallyCentered = true,
        )
    ) {
        onSelect(page)
    }
}
