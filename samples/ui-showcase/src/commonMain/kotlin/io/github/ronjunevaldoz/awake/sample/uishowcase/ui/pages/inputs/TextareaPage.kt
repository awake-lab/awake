// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val TextareaPage = ShowcasePage(
    id = "text-area",
    title = "Text Area",
    category = ShowcaseCategory.Inputs,
    description = "Multi-line expandable text input field for longform text entry.",
    usageCode = """shadcnFieldTextarea(id = "bio", label = "Bio", value = bio, minLines = 4)""",
    referenceExample = "registry/new-york-v4/examples/textarea-demo.tsx",
    previewHeight = 460,
    notes = listOf("Scrollable multiline input with focus ring boundary."),
    hero = {
        var bio by rememberStateValue("ui-showcase-textarea", "bio") { "" }
        shadcnMuted("Multi-line expandable text input field for longform content.")
        spacer(Modifier.height(8f.dp))
        shadcnCard(
            id = "textarea-hero-card",
            modifier = Modifier.height(220f.dp),
            header = { shadcnText("Text Area Preview") },
        ) {
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
