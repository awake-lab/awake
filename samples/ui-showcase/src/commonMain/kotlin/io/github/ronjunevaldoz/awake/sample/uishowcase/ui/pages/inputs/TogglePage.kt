// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseMatrix
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val TogglePage = ShowcasePage(
    id = "toggle",
    title = "Toggle",
    category = ShowcaseCategory.Inputs,
    description = "A two-state button that can be either on or off.",
    usageCode = """shadcnToggle(id = "bold", checked = bold, label = "Bold")""",
    referenceExample = "registry/new-york-v4/examples/toggle-demo.tsx",
    previewHeight = 280,
    hero = {
        var checked by rememberStateValue("ui-showcase-toggle", "checked") { false }
        checked = shadcnToggle(id = "showcase-toggle", checked = checked, label = "Bold")
    },
    states = {
        showcaseMatrix(listOf("On" to true, "Off" to false)) { (label, checked) ->
            shadcnToggle(
                id = "toggle-state-${label.lowercase()}",
                checked = checked,
                label = label,
            )
        }
    },
)
