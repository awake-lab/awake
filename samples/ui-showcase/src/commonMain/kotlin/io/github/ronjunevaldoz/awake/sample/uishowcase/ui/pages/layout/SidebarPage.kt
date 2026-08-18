// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarHeaderButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.modifier.width

private val SidebarSampleItems = listOf("Overview", "Analytics", "Projects", "Settings")

internal val SidebarPage = ShowcasePage(
    id = "sidebar",
    title = "Sidebar",
    category = ShowcaseCategory.Layout,
    description = "A composable, themeable and customizable sidebar with groups, menus, and pinned chrome.",
    usageCode = """shadcnSidebar(id = "sb", header = { ... }, footer = { ... }) { shadcnSidebarMenu { ... } }""",
    referenceExample = "registry/new-york-v4/blocks/sidebar-07",
    previewHeight = 520,
    notes = listOf("Header and footer stay pinned while the menu region scrolls."),
    hero = {
        var active by rememberStateValue("ui-showcase-sidebar-page", "active") { 0 }
        shadcnSidebar(
            id = "showcase-sidebar",
            modifier = Modifier.width(264f.dp).height(360f.dp),
            header = {
                shadcnSidebarHeaderButton(
                    id = "showcase-sidebar-team",
                    title = "Acme Inc",
                    subtitle = "Enterprise",
                )
            },
            footer = {
                shadcnSidebarFooterButton(
                    id = "showcase-sidebar-user",
                    name = "shadcn",
                    email = "m@example.com",
                )
            },
        ) {
            shadcnSidebarGroup(label = "PLATFORM") {
                shadcnSidebarMenu {
                    SidebarSampleItems.forEachIndexed { index, label ->
                        shadcnSidebarMenuItem(
                            id = "showcase-sidebar-item-$index",
                            label = label,
                            active = index == active,
                            onClick = { active = index },
                        )
                    }
                }
            }
        }
    },
)
