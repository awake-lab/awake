// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb

internal val BreadcrumbPage = ShowcasePage(
    id = "breadcrumb",
    title = "Breadcrumb",
    category = ShowcaseCategory.Layout,
    description = "Displays the path to the current resource using a hierarchy of links.",
    usageCode = """shadcnBreadcrumb(id = "bc", items = listOf("Docs", "Components", "Button"))""",
    referenceExample = "registry/new-york-v4/examples/breadcrumb-demo.tsx",
    previewHeight = 240,
    notes = listOf("Compact path trail for deep page hierarchies."),
    hero = {
        shadcnBreadcrumb(
            id = "showcase-breadcrumb",
            items = listOf("Docs", "Components", "Button"),
        )
    },
)
