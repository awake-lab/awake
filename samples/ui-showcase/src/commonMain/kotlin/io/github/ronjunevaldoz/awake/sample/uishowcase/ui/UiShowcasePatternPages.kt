// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseSlotApiPreview() {
    shadcnSupportingText("The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.")
    spacer(Modifier.height(8f.dp))
    buttonSlot(
        id = "slot-api-launch",
        modifier = Modifier.width(180f.dp).height(40f.dp),
        style = theme.components.button
    ) {
        val labelSize = Style { textSize(theme.typography.label) }
        text(">", modifier = Modifier.offset(x = 12f.dp).width(16f.dp), style = labelSize)
        text("Launch", modifier = Modifier.offset(x = 32f.dp), style = labelSize)
    }
    spacer(Modifier.height(16f.dp))
    shadcnSupportingText("samples:hello-cube's Gauge.kt is a fully custom widget built from the same claimSlot()/emit() primitives a built-in widget uses -- no library-only capability gap.")
}
