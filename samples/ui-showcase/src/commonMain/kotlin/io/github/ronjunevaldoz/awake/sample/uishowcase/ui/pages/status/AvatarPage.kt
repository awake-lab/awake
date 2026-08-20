// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcaseMatrix
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val AvatarPage = ShowcasePage(
    id = "avatar",
    title = "Avatar",
    category = ShowcaseCategory.Status,
    description = "An image element with a fallback for representing the user.",
    usageCode = """shadcnAvatar(id = "av", initials = "JD")""",
    referenceExample = "registry/new-york-v4/examples/avatar-demo.tsx",
    previewHeight = 360,
    notes = listOf("Rounded avatar with initial text fallback."),
    hero = {
        row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
            shadcnAvatar(id = "showcase-avatar-rj", initials = "RJ")
            shadcnAvatar(id = "showcase-avatar-ak", initials = "AK")
            shadcnAvatar(id = "showcase-avatar-ms", initials = "MS")
        }
        spacer(Modifier.height(12f.dp))
        shadcnMuted("Grouped, overlapping avatars share one recipe.")
        shadcnAvatarGroup(id = "showcase-avatar-group", initials = listOf("RJ", "AK", "MS", "TL"))
    },
    variants = {
        showcaseMatrix(ShadcnAvatarSize.entries) { size ->
            shadcnAvatar(id = "avatar-size-${size.name.lowercase()}", initials = "AW", size = size)
        }
    },
)
