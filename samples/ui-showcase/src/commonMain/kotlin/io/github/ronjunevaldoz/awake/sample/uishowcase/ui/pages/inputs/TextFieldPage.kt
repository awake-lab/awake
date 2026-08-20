// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val TextFieldPage = ShowcasePage(
    id = "text-input",
    title = "Text Field",
    category = ShowcaseCategory.Inputs,
    description = "Single-line keyboard-driven text input control with label and focus ring.",
    usageCode = """shadcnFieldTextField(id = "email", label = "Email", value = email)""",
    referenceExample = "registry/new-york-v4/examples/input-demo.tsx",
    previewHeight = 520,
    notes = listOf("Supports live keyboard typing, arrow navigation, and clear actions."),
    hero = {
        var name by rememberStateValue("ui-showcase-text-field", "name") { "" }
        var email by rememberStateValue("ui-showcase-text-field", "email") { "" }
        var bio by rememberStateValue("ui-showcase-text-field", "bio") { "" }
        shadcnMuted("Single-line and multi-line keyboard-driven text input controls with focus ring bounds.")
        spacer(Modifier.height(8f.dp))
        shadcnCard(
            id = "text-field-hero-card",
            modifier = Modifier.height(260f.dp),
            header = { shadcnText("Text Input & Area Interactive Preview") },
        ) {
            name = shadcnFieldTextField(
                id = "showcase-name",
                label = "Full Name",
                value = name,
                placeholder = "Jane Doe",
            )
            email = shadcnFieldTextField(
                id = "showcase-email",
                label = "Email Address",
                value = email,
                placeholder = "jane@example.com",
            )
            bio = shadcnFieldTextarea(
                id = "showcase-bio",
                label = "Biography",
                value = bio,
                placeholder = "Tell us about your background...",
                minLines = 4,
            )
        }
    },
)
