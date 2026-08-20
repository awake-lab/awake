// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row

internal val KbdPage = ShowcasePage(
    id = "kbd",
    title = "Kbd",
    category = ShowcaseCategory.Status,
    description = "Displays a keyboard key or shortcut.",
    usageCode = """shadcnKbd(id = "kbd", label = "⌘K")""",
    referenceExample = "registry/new-york-v4/examples/kbd-demo.tsx",
    previewHeight = 260,
    notes = listOf("Subtle border box representing physical keys."),
    hero = {
        row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(24f.dp)) {
            shadcnKbd(id = "showcase-kbd-cmd", label = "⌘")
            shadcnKbd(id = "showcase-kbd-k", label = "K")
            shadcnKbd(id = "showcase-kbd-shift", label = "Shift")
        }
    },
)
