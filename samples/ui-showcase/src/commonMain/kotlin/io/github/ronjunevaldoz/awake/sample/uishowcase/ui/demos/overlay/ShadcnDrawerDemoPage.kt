// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.overlay

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDrawerPosition
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

internal fun ColumnScope.drawShadcnDrawerDemoPreview() {
    val bottomDrawerState = context.rememberPopupState("showcase-drawer-bottom")

    shadcnBadge("DRAWER", variant = ShadcnBadgeVariant.Outline)
    shadcnSupportingText("Slide-over modal drawer panel anchored to viewport edge.")
    spacer(Modifier.height(8f.dp))

    row(horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(io.github.ronjunevaldoz.awake.ui.layout.Dimension.Fixed(36f.dp))) {
        if (
            shadcnButton(
                id = "showcase-drawer-open-bottom",
                label = "Open Bottom Drawer",
                modifier = Modifier.width(160f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Primary
            )
        ) {
            bottomDrawerState.open()
        }
    }

    shadcnDrawer(
        id = "showcase-drawer-bottom-panel",
        expanded = bottomDrawerState.expanded,
        position = ShadcnDrawerPosition.Bottom,
        onDismissRequest = { bottomDrawerState.close() },
        header = { text("Bottom Drawer Panel") }
    ) {
        shadcnBodyText("This drawer panel slides over from the bottom edge.")
        spacer(Modifier.height(10f.dp))
        if (
            shadcnButton(
                id = "showcase-drawer-close-bottom",
                label = "Dismiss Drawer",
                modifier = Modifier.width(120f.dp).height(32f.dp),
                variant = ShadcnButtonVariant.Outline
            )
        ) {
            bottomDrawerState.close()
        }
    }
}
