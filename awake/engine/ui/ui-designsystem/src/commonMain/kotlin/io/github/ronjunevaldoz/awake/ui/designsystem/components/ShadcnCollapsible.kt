// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [shadcnRadioGroup]'s single-select): a clickable header toggles [expanded].
 * Now includes animated height transition.
 */
fun ColumnScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    header: RowScope.() -> Unit,
    content: ColumnScope.() -> Unit
): Boolean {
    val shadcnTheme = theme.asShadcnTheme()
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
        this.row(
            horizontalArrangement = Arrangement.spacedBy(8f.dp),
            verticalAlignment = UiAlignment.Vertical.Center
        , modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(slot.height.dp))) {
             text(
                 label = if (expanded) "-" else "+",
                 modifier = Modifier.width(12f.dp),
                 centered = true,
                 verticallyCentered = true
             )
             header()
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

/** [shadcnCollapsible] convenience with a plain string title. */
fun ColumnScope.shadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    content: ColumnScope.() -> Unit
): Boolean = shadcnCollapsible(
    id = id,
    expanded = expanded,
    modifier = modifier,
    onExpandedChange = onExpandedChange,
    header = { text(title, verticallyCentered = true) },
    content = content
)
