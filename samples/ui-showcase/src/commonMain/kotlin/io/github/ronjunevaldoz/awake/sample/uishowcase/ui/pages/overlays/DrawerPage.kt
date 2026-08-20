// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDrawerPosition
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope

internal val DrawerPage = ShowcasePage(
    id = "drawer",
    title = "Drawer",
    category = ShowcaseCategory.Overlays,
    description = "A slide-over panel anchored to any viewport edge.",
    usageCode = """shadcnDrawer(id = "drw", expanded = open, position = ShadcnDrawerPosition.Bottom) { ... }""",
    referenceExample = "registry/new-york-v4/examples/drawer-demo.tsx",
    previewHeight = 420,
    notes = listOf("Anchors to any viewport edge with overlay scrim."),
    hero = {
        val popup = rememberPopupState("ui-showcase-drawer", initial = false)
        shadcnButton(
            id = "showcase-drawer-trigger",
            label = "Open drawer",
            variant = ShadcnButtonVariant.Outline,
            onClick = popup::open,
        )
        val result = uiScope().shadcnDrawer(
            id = "showcase-drawer",
            expanded = popup.expanded,
            onDismissRequest = popup::close,
            position = ShadcnDrawerPosition.Bottom,
        ) {
            shadcnText("Move goal")
            shadcnMuted("Set your daily activity target.")
            spacer(Modifier.height(8f.dp))
            shadcnButton(id = "showcase-drawer-close", label = "Close", onClick = popup::close)
        }
        if (result.dismissed) popup.close()
    },
)
