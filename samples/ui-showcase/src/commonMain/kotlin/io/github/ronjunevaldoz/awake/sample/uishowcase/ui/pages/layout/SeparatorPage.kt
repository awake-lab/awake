// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val SeparatorPage = ShowcasePage(
    id = "separator",
    title = "Separator",
    category = ShowcaseCategory.Layout,
    description = "Visually or semantically separates content.",
    usageCode = """shadcnSeparator(orientation = UiSeparatorOrientation.Vertical)""",
    referenceExample = "registry/new-york-v4/examples/separator-demo.tsx",
    previewHeight = 300,
    hero = {
        shadcnText("Awake UI")
        shadcnMuted("An immediate-mode component library.")
        spacer(Modifier.height(12f.dp))
        shadcnSeparator()
        spacer(Modifier.height(12f.dp))
        row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(20f.dp)) {
            shadcnKbd(id = "showcase-separator-kbd", label = "⌘K")
            shadcnSeparator(
                modifier = Modifier.width(1f.dp),
                orientation = UiSeparatorOrientation.Vertical,
            )
            shadcnMuted("Open command menu")
        }
    },
)
