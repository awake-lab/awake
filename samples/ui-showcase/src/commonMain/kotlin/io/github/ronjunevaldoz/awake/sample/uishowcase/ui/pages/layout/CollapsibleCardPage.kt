// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val CollapsibleCardPage = ShowcasePage(
    id = "collapsible-card",
    title = "Collapsible Card",
    category = ShowcaseCategory.Layout,
    description = "A card whose body collapses behind its own header row.",
    usageCode = """shadcnCollapsibleCard(id = "c", expanded = open, header = { _, toggle -> ... }) { ... }""",
    previewHeight = 400,
    notes = listOf("Composes the behavior primitive with the card visual instead of baking one into the other."),
    hero = {
        var expanded by rememberStateValue("ui-showcase-collapsible-card", "expanded") { true }
        expanded = shadcnCollapsibleCard(
            id = "showcase-collapsible-card",
            expanded = expanded,
            onExpandedChange = { expanded = it },
            header = { _, _ -> shadcnText("Deployment settings") },
        ) {
            shadcnMuted("Body content is mounted only while the card is expanded.")
        }
    },
)
