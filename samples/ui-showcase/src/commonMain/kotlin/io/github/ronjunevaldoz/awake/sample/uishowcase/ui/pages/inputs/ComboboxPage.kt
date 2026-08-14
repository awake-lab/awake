// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCombobox
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val ComboboxPage = ShowcasePage(
    id = "combobox",
    title = "Combobox",
    category = ShowcaseCategory.Inputs,
    description = "Autocomplete input and command palette with a list of suggestions.",
    usageCode = """shadcnCombobox(id = "framework", options = frameworks, selectedIndex = selected)""",
    referenceExample = "registry/new-york-v4/examples/combobox-demo.tsx",
    previewHeight = 340,
    notes = listOf("Typing filters the option list; the trigger keeps the current selection."),
    hero = {
        var selected: Int? by rememberStateValue("ui-showcase-combobox", "selected") { null }
        selected = shadcnCombobox(
            id = "showcase-combobox",
            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
            selectedIndex = selected,
        ) ?: selected
    },
)
