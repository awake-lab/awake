// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseMatrix
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val CheckboxPage = ShowcasePage(
    id = "checkbox",
    title = "Checkbox",
    category = ShowcaseCategory.Inputs,
    description = "A control that allows the user to toggle between checked and not checked.",
    usageCode = """shadcnCheckbox(id = "agree", checked = true, label = "Accept terms")""",
    referenceExample = "registry/new-york-v4/examples/checkbox-demo.tsx",
    previewHeight = 280,
    notes = listOf("Supports checked, unchecked, indeterminate, and disabled state tokens."),
    hero = {
        var checked by rememberStateValue("ui-showcase-checkbox", "checked") { true }
        checked = shadcnCheckbox(id = "showcase-checkbox", checked = checked, label = "Accept terms")
    },
    states = {
        showcaseMatrix(listOf("Checked" to true, "Unchecked" to false)) { (label, checked) ->
            shadcnCheckbox(
                id = "checkbox-state-${label.lowercase()}",
                checked = checked,
                label = label,
            )
        }
    },
)
