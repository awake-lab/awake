// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val PopoverPage = ShowcasePage(
    id = "popover",
    title = "Popover",
    category = ShowcaseCategory.Overlays,
    description = "Displays rich content in a portal, triggered by a button.",
    usageCode = """shadcnPopover(id = "p", anchorSlot = trigger.slot, expanded = true) { ... }""",
    referenceExample = "registry/new-york-v4/examples/popover-demo.tsx",
    previewHeight = 400,
    notes = listOf("Freeform content, unlike the fixed row list a dropdown menu renders."),
    hero = {
        shadcnMuted("Rendered expanded so anchored placement and panel chrome stay reviewable.")
        spacer(Modifier.height(8f.dp))
        row(
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            modifier = Modifier.height(36f.dp),
        ) {
            spacer(Modifier.width(100f.dp))
            shadcnButton(
                id = "showcase-popover-trigger",
                modifier = Modifier.width(120f.dp).height(36f.dp),
            ) { trigger ->
                text("Share", centered = true)
                uiScope().shadcnPopover(
                    id = "showcase-popover",
                    anchorSlot = trigger,
                    expanded = true,
                    width = Dimension.Fixed(280f.dp),
                ) {
                    shadcnText("Share scene")
                    shadcnMuted("Anyone with the link can view this scene until you revoke it.")
                }
            }
            shadcnButton(
                id = "showcase-popover-secondary",
                label = "Reference",
                modifier = Modifier.width(120f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Secondary,
            )
        }
    },
)
