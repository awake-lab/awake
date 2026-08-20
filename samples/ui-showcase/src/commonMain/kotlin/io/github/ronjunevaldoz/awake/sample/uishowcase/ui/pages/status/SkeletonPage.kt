// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val SkeletonPage = ShowcasePage(
    id = "skeleton",
    title = "Skeleton",
    category = ShowcaseCategory.Status,
    description = "Use to show a placeholder while content is loading.",
    usageCode = """shadcnSkeleton(id = "sk", modifier = Modifier.width(260f.dp).height(20f.dp))""",
    referenceExample = "registry/new-york-v4/examples/skeleton-demo.tsx",
    previewHeight = 300,
    notes = listOf("Muted background box indicating content loading."),
    hero = {
        shadcnSkeleton(id = "showcase-skeleton-title", modifier = Modifier.width(260f.dp).height(20f.dp))
        spacer(Modifier.height(8f.dp))
        shadcnSkeleton(id = "showcase-skeleton-line-1", modifier = Modifier.width(320f.dp).height(12f.dp))
        spacer(Modifier.height(8f.dp))
        shadcnSkeleton(id = "showcase-skeleton-line-2", modifier = Modifier.width(200f.dp).height(12f.dp))
    },
)
