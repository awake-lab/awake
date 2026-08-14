// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val RadioGroupPage = ShowcasePage(
    id = "radio-group",
    title = "Radio Group",
    category = ShowcaseCategory.Inputs,
    description = "A set of checkable buttons where no more than one can be checked at a time.",
    usageCode = """shadcnRadioGroup(id = "rad", options = listOf("A", "B"), selectedIndex = 0)""",
    referenceExample = "registry/new-york-v4/examples/radio-group-demo.tsx",
    previewHeight = 320,
    notes = listOf("Exclusive single-choice selection with keyboard navigation."),
    hero = {
        var selected by rememberStateValue("ui-showcase-radio-group", "selected") { 0 }
        selected = shadcnRadioGroup(
            id = "showcase-radio-group",
            options = listOf("System", "Light", "Dark"),
            selectedIndex = selected,
            onIndexChange = { selected = it },
        )
    },
)
