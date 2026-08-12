// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewScene
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/** Dumps the real production Sidebar as a PNG using the test rasterizer, matching its
 * "Chrome" look (logo, badges, real categories) -- useful for crop-validating that new
 * design-system Sidebar internal spacing matches the prior hand-rolled version. */
class UiShowcaseSidebarRealChromePngTest {

    @Test
    fun dumpRealChromeSidebarPngs() {
        saveRealChromeSidebar("debug-sidebar-real-expanded") { drawOldUiShowcaseSidebar(compact = false) }
        saveRealChromeSidebar("debug-sidebar-real-compact") { drawOldUiShowcaseSidebar(compact = true) }
    }

    private fun saveRealChromeSidebar(id: String, draw: ColumnScope.() -> Unit) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(300f, 800f, input.updateSnapshot().toUiInputState())
        ui.pushTheme(theme)
        ui.createUiScope(UiBounds(x = 12f, y = 12f, width = 276f, height = 776f)).shadcnSidebar(
            id = "real-sidebar",
            modifier = Modifier.width(276f.dp),
        ) { _ ->
            draw()
        }

        val output = ui.finishFrame()
        val scene = AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = id,
                title = id,
                group = "Debug",
                summary = "Full-chrome Sidebar render for spacing validation.",
                width = 300,
                height = 800,
                reportScale = 2,
            ),
            primitives = output.primitives,
            background = theme.colors.background,
            font = ui.currentFont,
            semantics = output.semantics,
        )
        saveAwakeUiPreview(scene)
    }
}

internal fun ColumnScope.drawOldUiShowcaseSidebar(compact: Boolean) {
    shadcnSidebarGroup {
        shadcnBadge("ALPHA", "Alpha", variant = ShadcnBadgeVariant.Primary)
        shadcnHeadline("Awake Engine")
        shadcnSupportingText("v0.1.0")
    }

    ShowcasePagesByCategory.entries.forEach { (category, pages) ->
        var expanded by rememberStateValue("probe-sidebar-category", category.name) { true }
        shadcnCollapsible(
            id = "category-${category.name}",
            title = category.title,
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            shadcnSidebarMenu {
                pages.forEach { page ->
                    drawOldSidebarPageButton(page, category.name, 36f.dp) { }
                }
            }
        }
    }
}

private fun ColumnScope.drawOldSidebarPageButton(
    page: ShowcasePage,
    categoryName: String,
    height: io.github.ronjunevaldoz.awake.ui.api.Dp,
    onSelect: (ShowcasePage) -> Unit,
) {
    shadcnSidebarMenuItem(
        id = "page-${page.id}",
        label = page.title,
        active = false,
        onClick = { onSelect(page) },
    )
}
