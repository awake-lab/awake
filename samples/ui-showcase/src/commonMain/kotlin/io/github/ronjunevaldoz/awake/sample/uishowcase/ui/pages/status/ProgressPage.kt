// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val ProgressPage = ShowcasePage(
    id = "progress",
    title = "Progress",
    category = ShowcaseCategory.Status,
    description = "Displays an indicator showing the completion progress of a task.",
    usageCode = """shadcnProgress(id = "p", value = 0.65f)""",
    referenceExample = "registry/new-york-v4/examples/progress-demo.tsx",
    previewHeight = 320,
    notes = listOf("Animated progress fill bar."),
    hero = {
        shadcnProgress(
            id = "showcase-progress",
            value = 0.65f,
            modifier = Modifier.width(260f.dp).height(8f.dp),
        )
    },
    states = {
        listOf(0.25f, 0.65f, 1f).forEachIndexed { index, value ->
            shadcnProgress(
                id = "progress-state-$index",
                value = value,
                modifier = Modifier.width(260f.dp).height(8f.dp),
            )
            spacer(Modifier.height(12f.dp))
        }
    },
)
