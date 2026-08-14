// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val TabsPage = ShowcasePage(
    id = "tabs",
    title = "Tabs",
    category = ShowcaseCategory.Layout,
    description = "A set of layered sections of content displayed one panel at a time.",
    usageCode = """shadcnTabs(id = "tabs", tabs = listOf("Account", "Password"), selectedIndex = 0)""",
    referenceExample = "registry/new-york-v4/examples/tabs-demo.tsx",
    previewHeight = 300,
    notes = listOf("Muted track with active tab surface elevation."),
    hero = {
        var selected by rememberStateValue("ui-showcase-tabs", "selected") { 0 }
        selected = shadcnTabs(
            id = "showcase-tabs",
            tabs = listOf("Account", "Password"),
            selectedIndex = selected,
        )
    },
)
