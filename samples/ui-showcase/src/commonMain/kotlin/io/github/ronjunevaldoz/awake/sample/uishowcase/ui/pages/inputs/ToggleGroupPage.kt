// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val ToggleGroupPage = ShowcasePage(
    id = "toggle-group",
    title = "Toggle Group",
    category = ShowcaseCategory.Inputs,
    description = "A set of two-state buttons that can be toggled on or off.",
    usageCode = """shadcnToggleGroup(id = "align", options = listOf("Left", "Center"), selectedIndex = 0)""",
    referenceExample = "registry/new-york-v4/examples/toggle-group-demo.tsx",
    previewHeight = 280,
    notes = listOf("Single-select and multi-select forms share one recipe."),
    hero = {
        var selected by rememberStateValue("ui-showcase-toggle-group", "selected") { 0 }
        shadcnToggleGroup(
            id = "showcase-toggle-group",
            options = listOf("Left", "Center", "Right"),
            selectedIndex = selected,
            onIndexChange = { selected = it },
        )
    },
    states = {
        var picked by rememberStateValue("ui-showcase-toggle-group", "multi") { setOf(0, 2) }
        shadcnToggleGroup(
            id = "showcase-toggle-group-multi",
            options = listOf("Bold", "Italic", "Underline"),
            selectedIndices = picked,
            onSelectedIndicesChange = { picked = it },
        )
    },
)
