// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.modifier.width

internal val AlertDialogPage = ShowcasePage(
    id = "alert-dialog",
    title = "Alert Dialog",
    category = ShowcaseCategory.Overlays,
    description = "A modal dialog that interrupts the user with important content and expects a response.",
    usageCode = """shadcnAlertDialog(id = "ad", expanded = true, title = "...", message = "...")""",
    referenceExample = "registry/new-york-v4/examples/alert-dialog-demo.tsx",
    previewHeight = 520,
    notes = listOf("Rendered open on purpose so title wrapping and action widths stay reviewable."),
    hero = {
        shadcnMuted(
            "The dialog is rendered open on purpose so title wrapping, message rhythm, scrim color, " +
                "and action widths can be checked without live interaction.",
        )
        spacer(Modifier.height(8f.dp))
        shadcnButton(
            id = "showcase-alert-dialog-trigger",
            label = "Open Dialog",
            modifier = Modifier.width(156f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Outline,
        )
        uiScope().shadcnAlertDialog(
            id = "showcase-alert-dialog",
            expanded = true,
            title = "Delete this long showcase card title before publishing the updated catalog?",
            message = "This static preview exists only to validate the dialog treatment. No real deletion happens here.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
        )
    },
)
