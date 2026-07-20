// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.width
import io.github.ronjunevaldoz.awake.ui.spacer
import io.github.ronjunevaldoz.awake.ui.buttonSlot
import io.github.ronjunevaldoz.awake.ui.text

internal fun ColumnScope.drawUiShowcaseSlotApiPreview() {
    awakeShadcnSectionTitle("buttonSlot(...) content lambda")
    awakeShadcnSupportingText("The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.")
    spacer(UiModifier().height(8f.dp))
    buttonSlot(
        id = "slot-api-launch",
        modifier = UiModifier().width(180f.dp).height(40f.dp),
        style = theme.components.button
    ) {
        val labelSize = Style { textSize(theme.typography.label) }
        text(">", modifier = UiModifier().offset(x = 12f.dp).width(16f.dp), style = labelSize)
        text("Launch", modifier = UiModifier().offset(x = 32f.dp), style = labelSize)
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSectionTitle("Custom widgets, same primitives")
    awakeShadcnSupportingText("samples:hello-cube's Gauge.kt is a fully custom widget built from the same claimSlot()/emit() primitives a built-in widget uses -- no library-only capability gap.")
}
