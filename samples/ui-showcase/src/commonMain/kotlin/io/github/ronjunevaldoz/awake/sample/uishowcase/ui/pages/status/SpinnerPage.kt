// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSpinner
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val SpinnerPage = ShowcasePage(
    id = "spinner",
    title = "Spinner",
    category = ShowcaseCategory.Status,
    description = "An indeterminate loading indicator.",
    usageCode = """shadcnSpinner(id = "sp", modifier = Modifier.width(24f.dp).height(24f.dp))""",
    referenceExample = "registry/new-york-v4/examples/spinner-demo.tsx",
    previewHeight = 260,
    notes = listOf("Continuous rotation loading indicator."),
    hero = {
        shadcnSpinner(id = "showcase-spinner", modifier = Modifier.width(24f.dp).height(24f.dp))
    },
)
