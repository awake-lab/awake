// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant

internal val EmptyPage = ShowcasePage(
    id = "empty",
    title = "Empty",
    category = ShowcaseCategory.Status,
    description = "Use to display an empty state with a title, description, and optional action.",
    usageCode = """shadcnEmpty(id = "e", title = "No projects", description = "...") { ... }""",
    referenceExample = "registry/new-york-v4/examples/empty-demo.tsx",
    previewHeight = 380,
    hero = {
        shadcnEmpty(
            id = "showcase-empty",
            title = "No projects yet",
            description = "Create your first project to see it listed here.",
        ) {
            shadcnButton(
                id = "showcase-empty-action",
                label = "New project",
                variant = ShadcnButtonVariant.Primary,
            )
        }
    },
)
