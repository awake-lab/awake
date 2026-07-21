// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.core.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.paddingTop
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [awakeShadcnRadioGroup]'s single-select): a clickable header toggles [expanded].
 * Now includes animated height transition.
 */
fun ColumnScope.awakeShadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = UiModifier(),
    onExpandedChange: (Boolean) -> Unit = {},
    content: ColumnScope.() -> Unit
): Boolean {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    val trigger = buttonSlot(
        id = "$id.header",
        modifier = modifier.fillMaxWidth()
            .height(32f.dp),
        variant = UiButtonVariant.Ghost,
        style = Style {
            foreground(shadcnTheme.tokens.foreground)
            contentPadding(4f.dp, 0f.dp)
        }
    ) { slot ->
        // We use a simple row with fixed dimensions or predictable scaling logic.
        // To avoid WrapContent issues, we ensure children don't force unmeasured constraints.
        this.row(width = Dimension.FillMax, height = Dimension.Fixed( slot.height.dp), gap = 8f) {
             text(
                 label = if (expanded) "-" else "+",
                 modifier = UiModifier().width(12f.dp).paddingTop( 8f.dp)
             )
             text(title, modifier = UiModifier().paddingTop(8f.dp))
        }
    }

    // The crash was caused by side-effects in composition via derived 'resolved' variable.
    // Now we check for the transient .clicked flag directly and only fire on actual change detection.
    if (trigger.clicked && expanded != !expanded) {
        onExpandedChange(!expanded)
    }

    // Rely on truth from parameters directly
    animatedHeight(id = "$id.content", expanded = expanded, responsiveness = 8f) {
        content()
    }

    return expanded
}
