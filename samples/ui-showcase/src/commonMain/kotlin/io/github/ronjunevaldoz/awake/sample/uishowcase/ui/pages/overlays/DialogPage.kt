// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope

internal val DialogPage = ShowcasePage(
    id = "dialog",
    title = "Dialog",
    category = ShowcaseCategory.Overlays,
    description = "A window overlaid on either the primary window or another dialog window.",
    usageCode = """shadcnDialog(id = "dlg", expanded = open) { ... }""",
    referenceExample = "registry/new-york-v4/examples/dialog-demo.tsx",
    previewHeight = 420,
    notes = listOf("Centered popup window with dismiss actions."),
    hero = {
        val popup = rememberPopupState("ui-showcase-dialog", initial = false)
        shadcnButton(
            id = "showcase-dialog-trigger",
            label = "Open dialog",
            variant = ShadcnButtonVariant.Outline,
            onClick = popup::open,
        )
        val result = uiScope().shadcnDialog(id = "showcase-dialog", expanded = popup.expanded) {
            shadcnText("This dialog is interactive.")
            spacer(Modifier.height(8f.dp))
            shadcnButton(id = "showcase-dialog-close", label = "Close", onClick = popup::close)
        }
        if (result.dismissed) popup.close()
    },
)
