// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputGroup
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val InputGroupPage = ShowcasePage(
    id = "input-group",
    title = "Input Group",
    category = ShowcaseCategory.Inputs,
    description = "An input paired with inline prefix or suffix affixes inside one bordered control.",
    usageCode = """shadcnInputGroup(id = "site", value = url, prefixText = "https://")""",
    referenceExample = "registry/new-york-v4/examples/input-group-demo.tsx",
    previewHeight = 340,
    hero = {
        var url by rememberStateValue("ui-showcase-input-group", "url") { "awake.dev" }
        url = shadcnInputGroup(
            id = "showcase-input-group",
            value = url,
            prefixText = "https://",
        )
        spacer(Modifier.height(12f.dp))
        var amount by rememberStateValue("ui-showcase-input-group", "amount") { "24.00" }
        amount = shadcnInputGroup(
            id = "showcase-input-group-suffix",
            value = amount,
            prefixText = "$",
            suffixText = "USD",
        )
    },
)
