// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val CardPage = ShowcasePage(
    id = "card",
    title = "Card",
    category = ShowcaseCategory.Layout,
    description = "Displays a card with header, content, and footer.",
    usageCode = """shadcnCard(id = "card", header = { shadcnText("Title") }) { ... }""",
    referenceExample = "registry/new-york-v4/examples/card-demo.tsx",
    previewHeight = 420,
    notes = listOf("Encapsulated content surface with card and border tokens."),
    hero = {
        shadcnCard(
            id = "showcase-card",
            modifier = Modifier.height(160f.dp),
            header = { shadcnText("Create project") },
        ) {
            shadcnMuted("Deploy your new project in one click.")
        }
    },
    variants = {
        ShadcnCardVariant.entries.forEach { variant ->
            shadcnCard(
                id = "card-variant-${variant.name.lowercase()}",
                modifier = Modifier.height(110f.dp),
                variant = variant,
                header = { shadcnText(variant.name) },
            ) {
                shadcnMuted("Card variant ${variant.name}.")
            }
            spacer(Modifier.height(12f.dp))
        }
    },
)
