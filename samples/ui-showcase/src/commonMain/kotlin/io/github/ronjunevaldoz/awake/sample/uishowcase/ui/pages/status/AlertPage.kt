// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val AlertPage = ShowcasePage(
    id = "alert",
    title = "Alert",
    category = ShowcaseCategory.Status,
    description = "Displays a callout for user attention.",
    usageCode = """shadcnAlert(id = "a", title = "Heads up!", description = "...")""",
    referenceExample = "registry/new-york-v4/examples/alert-demo.tsx",
    previewHeight = 380,
    notes = listOf("Default and Destructive alert callout boxes."),
    hero = {
        shadcnAlert(
            id = "showcase-alert",
            title = "Heads up!",
            description = "You can add components to your app using the CLI.",
        )
    },
    variants = {
        ShadcnAlertVariant.entries.forEach { variant ->
            shadcnAlert(
                id = "alert-variant-${variant.name.lowercase()}",
                title = variant.name,
                description = "Alert variant ${variant.name}.",
                variant = variant,
            )
            spacer(Modifier.height(12f.dp))
        }
    },
)
