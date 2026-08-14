// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseMatrix
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val ButtonPage = ShowcasePage(
    id = "button",
    title = "Button",
    category = ShowcaseCategory.Inputs,
    description = "Displays a button or a component that looks like a button.",
    usageCode = """shadcnButton(id = "btn", label = "Button", variant = ShadcnButtonVariant.Primary)""",
    referenceExample = "registry/new-york-v4/examples/button-demo.tsx",
    notes = listOf("Supports text labels, icons, and custom slot API blocks."),
    hero = {
        var clicks by rememberStateValue("button-page", "clicks") { 0 }
        shadcnButton(
            id = "button-hero",
            label = "Button",
            variant = ShadcnButtonVariant.Primary,
            onClick = { clicks += 1 },
        )
        spacer(Modifier.height(6f.dp))
        shadcnMuted("Interaction proof: $clicks clicks")
    },
    variants = {
        showcaseMatrix(ShadcnButtonVariant.entries) { variant ->
            shadcnButton(
                id = "button-variant-${variant.name.lowercase()}",
                label = variant.name,
                variant = variant,
            )
        }
    },
    states = {
        showcaseMatrix(ShadcnButtonSize.entries) { size ->
            shadcnButton(
                id = "button-size-${size.name.lowercase()}",
                label = size.name,
                size = size,
            )
        }
        spacer(Modifier.height(12f.dp))
        showcaseMatrix(listOf(true, false)) { enabled ->
            shadcnButton(
                id = "button-enabled-$enabled",
                label = if (enabled) "Enabled" else "Disabled",
                enabled = enabled,
            )
        }
    },
)
