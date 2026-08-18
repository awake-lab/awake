// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val SurfacePage = ShowcasePage(
    id = "surface",
    title = "Surface",
    category = ShowcaseCategory.Layout,
    description = "The neutral themed container every other shadcn recipe is built on.",
    usageCode = """shadcnSurface(id = "panel") { shadcnText("Content") }""",
    previewHeight = 380,
    notes = listOf("Card, dialog, popover, and sidebar all resolve their chrome through this primitive."),
    hero = {
        shadcnSurface(id = "showcase-surface-default", modifier = Modifier.fillMaxWidth()) {
            shadcnText("Default surface")
            shadcnMuted("Background, border, and radius come from the active theme.")
        }
        spacer(Modifier.height(12f.dp))
        shadcnSurface(
            id = "showcase-surface-muted",
            modifier = Modifier.fillMaxWidth(),
            variant = ShadcnSurfaceVariant.Muted,
        ) {
            shadcnText("Muted surface")
            shadcnMuted("Used for inset panels and preview wells.")
        }
    },
)
