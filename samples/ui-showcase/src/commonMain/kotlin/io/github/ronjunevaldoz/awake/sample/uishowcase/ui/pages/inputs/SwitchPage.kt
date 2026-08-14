// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSwitch
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val SwitchPage = ShowcasePage(
    id = "switch",
    title = "Switch",
    category = ShowcaseCategory.Inputs,
    description = "A control that allows the user to toggle between checked and not checked.",
    usageCode = """shadcnSwitch(id = "sw", checked = enabled, label = "Enable notifications")""",
    referenceExample = "registry/new-york-v4/examples/switch-demo.tsx",
    previewHeight = 280,
    notes = listOf("Smooth thumb sliding animation across track."),
    hero = {
        var checked by rememberStateValue("ui-showcase-switch", "checked") { true }
        checked = shadcnFieldSwitch(id = "showcase-switch", label = "Airplane mode", checked = checked)
    },
)
