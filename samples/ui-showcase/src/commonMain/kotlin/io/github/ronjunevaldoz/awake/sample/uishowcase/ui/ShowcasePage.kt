// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal typealias ShowcaseRenderer = ColumnScope.(UiShowcaseRuntimeState) -> Unit

internal enum class ShowcaseCategory(val title: String) {
    GettingStarted("Getting Started"),
    Inputs("Inputs"),
    Layout("Layout"),
    Overlays("Overlays"),
    Status("Status"),
    Typography("Typography"),
    Blocks("Blocks"),
}

/**
 * [Placeholder] pages are registered on purpose: a component shadcn ships that Awake has not
 * built yet stays visible in the catalog instead of being silently absent. Removing a page is
 * therefore always a deliberate act, never the side effect of forgetting to add one.
 */
internal enum class ShowcaseStatus { Ready, Placeholder }

/**
 * One catalog entry. [hero] is the page's primary sample -- a port of [referenceExample] where
 * one exists. [variants] and [states] are static matrices; interaction coverage belongs in
 * `:awake:ui:designsystem` tests, not in extra showcase pages.
 *
 * [previewWidth]/[previewHeight] are the raster size the preview and layout-signature tests
 * render this page at. They live here rather than in a JVM annotation so the tests run on
 * every target, not only the reflective ones.
 */
internal class ShowcasePage(
    val id: String,
    val title: String,
    val category: ShowcaseCategory,
    val description: String,
    val usageCode: String,
    /** Path under `third_party/shadcn-ui-ref/apps/v4/` this hero is ported from, or "". */
    val referenceExample: String = "",
    val status: ShowcaseStatus = ShowcaseStatus.Ready,
    val previewWidth: Int = 720,
    val previewHeight: Int = 420,
    val notes: List<String> = emptyList(),
    val hero: ShowcaseRenderer,
    val variants: ShowcaseRenderer? = null,
    val states: ShowcaseRenderer? = null,
)

/** A component shadcn ships that Awake has not built yet. Renders as an explicit gap. */
internal fun showcasePlaceholder(
    id: String,
    title: String,
    category: ShowcaseCategory,
    description: String,
    missing: String,
    referenceExample: String = "",
): ShowcasePage = ShowcasePage(
    id = id,
    title = title,
    category = category,
    description = description,
    usageCode = "// not implemented -- $missing",
    referenceExample = referenceExample,
    status = ShowcaseStatus.Placeholder,
    previewHeight = 260,
    notes = listOf("Missing: $missing"),
    hero = { drawShowcasePlaceholder(id, title, missing, referenceExample) },
)

private fun ColumnScope.drawShowcasePlaceholder(
    id: String,
    title: String,
    missing: String,
    referenceExample: String,
) {
    shadcnEmpty(
        id = "showcase-placeholder-$id",
        title = "$title is not implemented",
        description = missing,
    )
    if (referenceExample.isNotBlank()) {
        spacer(Modifier.height(8f.dp))
        shadcnMuted("Reference: $referenceExample")
    }
}

/** Lays [items] out in wrapped rows -- the shared shape of every variant/state matrix. */
internal fun <T> ColumnScope.showcaseMatrix(
    items: List<T>,
    perRow: Int = 4,
    gap: Float = 12f,
    cell: RowScope.(T) -> Unit,
) {
    items.chunked(perRow).forEachIndexed { index, chunk ->
        if (index > 0) spacer(Modifier.height(gap.dp))
        row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
            chunk.forEach { cell(it) }
        }
    }
}
