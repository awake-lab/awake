// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.text

internal val CollapsiblePage = ShowcasePage(
    id = "collapsible",
    title = "Collapsible",
    category = ShowcaseCategory.Layout,
    description = "An interactive component which expands and collapses a panel.",
    usageCode = """shadcnCollapsible(id = "col", title = "Header", expanded = expanded) { ... }""",
    referenceExample = "registry/new-york-v4/examples/collapsible-demo.tsx",
    previewHeight = 440,
    notes = listOf("Smooth expand/collapse animation for hidden content."),
    hero = {
        var expanded by rememberStateValue("ui-showcase-collapsible", "expanded") { true }
        expanded = shadcnCollapsible(
            id = "showcase-collapsible",
            title = "@radix-ui/primitives",
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            collapsibleRows("collapsible-hero")
        }
    },
    states = {
        // Rendered open on purpose so revealed-content spacing and separators stay reviewable
        // without live interaction.
        shadcnCollapsible(
            id = "showcase-collapsible-open",
            title = "@radix-ui/primitives",
            expanded = true,
            onExpandedChange = {},
        ) {
            collapsibleRows("collapsible-open")
        }
    },
)

private fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.collapsibleRows(idPrefix: String) {
    column(
        verticalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        shadcnMuted("Starred repositories in this workspace.")
        shadcnSeparator(
            modifier = Modifier.padding(horizontal = 0f.dp, vertical = 4f.dp),
            id = "$idPrefix.separator",
        )
        row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
            text("@radix-ui/colors", modifier = Modifier.padding(12f.dp, 0f.dp))
        }
        row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
            text("@stitches/react", modifier = Modifier.padding(12f.dp, 0f.dp))
        }
    }
}
