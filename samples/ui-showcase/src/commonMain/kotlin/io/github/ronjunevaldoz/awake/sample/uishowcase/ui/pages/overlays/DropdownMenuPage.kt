// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.modifier.width

private val MenuItems = listOf(
    ShadcnDropdownMenuItem(label = "Pinned action", enabled = false),
    ShadcnDropdownMenuSeparator,
    ShadcnDropdownMenuItem(label = "Duplicate panel"),
    ShadcnDropdownMenuItem(label = "Delete scene", destructive = true),
)

internal val DropdownMenuPage = ShowcasePage(
    id = "dropdown-menu",
    title = "Dropdown Menu",
    category = ShowcaseCategory.Overlays,
    description = "Displays a menu to the user -- such as a set of actions or functions -- triggered by a button.",
    usageCode = """shadcnDropdownMenu(id = "m", anchorSlot = trigger.slot, expanded = true, items = items)""",
    referenceExample = "registry/new-york-v4/examples/dropdown-menu-demo.tsx",
    previewHeight = 420,
    notes = listOf("The menu stays inside a real popover container, not a loose stack of buttons."),
    hero = {
        shadcnMuted("Rendered expanded so row spacing, grouping, and disabled/destructive rows stay reviewable.")
        spacer(Modifier.height(8f.dp))
        row(
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            modifier = Modifier.height(36f.dp),
        ) {
            shadcnButton(
                id = "showcase-dropdown-trigger",
                modifier = Modifier.width(124f.dp).height(36f.dp),
            ) { trigger ->
                text("Actions", centered = true)
                uiScope().shadcnDropdownMenu(
                    id = "showcase-dropdown-menu",
                    anchorSlot = trigger,
                    expanded = true,
                    items = MenuItems,
                    selectedIndex = 1,
                    width = Dimension.Fixed(340f.dp),
                    positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
                )
            }
            shadcnButton(
                id = "showcase-dropdown-secondary",
                label = "Secondary",
                modifier = Modifier.width(132f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Outline,
            )
        }
    },
)
