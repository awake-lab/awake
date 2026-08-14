// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseMatrix
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant

internal val BadgePage = ShowcasePage(
    id = "badge",
    title = "Badge",
    category = ShowcaseCategory.Inputs,
    description = "Displays a badge or a component that looks like a badge.",
    usageCode = """shadcnBadge(id = "badge", label = "Badge", variant = ShadcnBadgeVariant.Primary)""",
    referenceExample = "registry/new-york-v4/examples/badge-demo.tsx",
    previewHeight = 260,
    notes = listOf("Pill shape with primary, secondary, outline, ghost, and destructive tokens."),
    hero = {
        shadcnBadge(id = "badge-hero", label = "Badge", variant = ShadcnBadgeVariant.Primary)
    },
    variants = {
        showcaseMatrix(ShadcnBadgeVariant.entries) { variant ->
            shadcnBadge(
                id = "badge-variant-${variant.name.lowercase()}",
                label = variant.name,
                variant = variant,
            )
        }
    },
)
