// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToast
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.width

internal val ToastPage = ShowcasePage(
    id = "toast",
    title = "Toast",
    category = ShowcaseCategory.Status,
    description = "A succinct message that is displayed temporarily.",
    usageCode = """shadcnToast(id = "t", message = "Event has been created")""",
    referenceExample = "registry/new-york-v4/examples/sonner-demo.tsx",
    previewHeight = 320,
    notes = listOf("The toast auto-dismisses after durationMs; the return value reports visibility."),
    hero = {
        shadcnMuted("Rendered with a long duration so the surface stays capturable.")
        spacer(Modifier.height(8f.dp))
        shadcnToast(
            id = "showcase-toast",
            message = "Event has been created",
            modifier = Modifier.width(320f.dp),
            durationMs = 600_000f,
        )
    },
)
