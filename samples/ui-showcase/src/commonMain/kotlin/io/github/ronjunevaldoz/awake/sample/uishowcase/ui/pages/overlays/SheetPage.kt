// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnSheetSide
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSheet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope

internal val SheetPage = ShowcasePage(
    id = "sheet",
    title = "Sheet",
    category = ShowcaseCategory.Overlays,
    description = "Extends the Dialog component to display content that complements the main content of the screen.",
    usageCode = """shadcnSheet(id = "s", expanded = open, side = ShadcnSheetSide.Right) { ... }""",
    referenceExample = "registry/new-york-v4/examples/sheet-demo.tsx",
    previewHeight = 420,
    notes = listOf("Same popup machinery as Drawer; Sheet keeps shadcn's side-anchored sizing defaults."),
    hero = {
        val popup = rememberPopupState("ui-showcase-sheet", initial = false)
        shadcnButton(
            id = "showcase-sheet-trigger",
            label = "Open sheet",
            variant = ShadcnButtonVariant.Outline,
            onClick = popup::open,
        )
        val result = uiScope().shadcnSheet(
            id = "showcase-sheet",
            expanded = popup.expanded,
            onDismissRequest = popup::close,
            side = ShadcnSheetSide.Right,
        ) {
            shadcnText("Edit profile")
            shadcnMuted("Make changes to your profile here. Click save when you're done.")
            spacer(Modifier.height(8f.dp))
            shadcnButton(id = "showcase-sheet-close", label = "Close", onClick = popup::close)
        }
        if (result.dismissed) popup.close()
    },
)
