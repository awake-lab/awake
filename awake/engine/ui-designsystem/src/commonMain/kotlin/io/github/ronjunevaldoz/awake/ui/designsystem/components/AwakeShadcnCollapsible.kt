// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.animatedHeight
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.width

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [awakeShadcnRadioGroup]'s single-select): a clickable header toggles [expanded].
 * Now includes animated height transition.
 */
fun UiColumnDslScope.awakeShadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = UiModifier(),
    onExpandedChange: (Boolean) -> Unit = {},
    content: UiColumnDslScope.() -> Unit
): Boolean {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    val trigger = buttonSlot(
        id = "$id.header",
        modifier = modifier.height(32f.dp),
        variant = io.github.ronjunevaldoz.awake.ui.UiButtonVariant.Ghost,
        style = Style {
            foreground(shadcnTheme.tokens.foreground)
            contentPadding(4f.dp, 0f.dp)
        }
    ) { slot ->
        this.row(width = Dimension.FillMax, height = slot.height.px, gap = 8f) {
            text(if (expanded) "-" else "+", modifier = UiModifier().width(12f.dp))
            text(title)
        }
    }
    val resolved = if (trigger.clicked) !expanded else expanded
    if (resolved != expanded) {
        onExpandedChange(resolved)
    }

    animatedHeight(id = "$id.content", expanded = resolved, responsiveness = 8f) {
        content()
    }

    return resolved
}
