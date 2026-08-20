// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

private val ContextMenuItems = listOf(
    ShadcnMenuItem("Back"),
    ShadcnMenuItem("Forward", enabled = false),
    ShadcnMenuSeparator,
    ShadcnMenuItem("Reload"),
    ShadcnMenuItem("Save as..."),
)

internal val ContextMenuPage = ShowcasePage(
    id = "context-menu",
    title = "Context Menu",
    category = ShowcaseCategory.Overlays,
    description = "Displays a menu located at the pointer, triggered by a right click.",
    usageCode = """shadcnContextMenu(id = "ctx", expanded = open, items = items) { targetBounds() }""",
    referenceExample = "registry/new-york-v4/examples/context-menu-demo.tsx",
    previewHeight = 420,
    notes = listOf("Triggers on secondary pointer click over the target bounds."),
    hero = {
        var expanded by rememberStateValue("ui-showcase-context-menu", "expanded") { false }
        shadcnMuted("Right-click the panel below.")
        spacer(Modifier.height(8f.dp))
        shadcnContextMenu(
            id = "showcase-context-menu",
            expanded = expanded,
            onExpandedChange = { expanded = it },
            items = ContextMenuItems,
            target = {
                shadcnSurface(
                    id = "showcase-context-menu-target",
                    modifier = Modifier.fillMaxWidth().height(140f.dp),
                ) {
                    shadcnText("Right click here")
                }
            },
        )
    },
)
