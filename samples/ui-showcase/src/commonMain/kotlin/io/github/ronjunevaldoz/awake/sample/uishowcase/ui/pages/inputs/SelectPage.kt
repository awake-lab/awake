// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val SelectPage = ShowcasePage(
    id = "select",
    title = "Select",
    category = ShowcaseCategory.Inputs,
    description = "Displays a list of options for the user to pick from, triggered by a button.",
    usageCode = """shadcnSelect(id = "sel", options = options, selectedIndex = 0)""",
    referenceExample = "registry/new-york-v4/examples/select-demo.tsx",
    previewHeight = 340,
    notes = listOf("Floating item popover with hover highlight."),
    hero = {
        var selected by rememberStateValue("ui-showcase-select", "selected") { 0 }
        selected = shadcnFieldDropdown(
            id = "showcase-select",
            label = "Framework",
            options = listOf("Compose", "SwiftUI", "Flutter"),
            selectedIndex = selected,
        ) ?: selected
    },
)
