// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [shadcnRadioGroup]'s single-select): a clickable trigger toggles [expanded].
 * Includes animated height transition.
 *
 * Primary Slot API form -- [trigger] receives `(isOpen, toggle)` and renders the *entire*
 * trigger, matching real shadcn-compose's `trigger: @Composable (isOpen, toggle) -> Unit`
 * shape (not just a header label plugged into a fixed internal button, which is what this
 * function used to do). This lets a caller render any real interactive widget as the
 * trigger -- a `shadcnButton(variant = Outline)`, an icon-only button, anything -- by calling
 * `toggle()` from its own `onClick`, instead of being stuck inside a hardcoded Ghost-variant
 * `buttonSlot`. See the `header`/`title` overloads below for the previous fixed-trigger
 * behavior, now expressed as convenience wrappers over this primary form.
 */
fun ColumnScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(isOpen: Boolean, toggle: () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit
): Boolean {
    trigger(expanded) { onExpandedChange(!expanded) }

    animatedHeight(id = "$id.content", expanded = expanded, responsiveness = 8f) {
        content()
    }

    return expanded
}

/**
 * [shadcnCollapsible] convenience with the previous fixed trigger: a Ghost-variant button
 * showing a "-"/"+" icon plus caller-supplied [header] content. Kept for callers that only
 * need to customize the header's inner content, not swap the trigger widget itself.
 */
private fun ColumnScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    header: RowScope.() -> Unit,
    content: ColumnScope.() -> Unit
): Boolean = shadcnCollapsible(
    id = id,
    expanded = expanded,
    modifier = modifier,
    onExpandedChange = onExpandedChange,
    trigger = { isOpen, toggle ->
        val shadcnTheme = theme.asShadcnTheme()
        shadcnButton(
            id = "$id.header",
            modifier = modifier.fillMaxWidth()
                .height(32f.dp),
            variant = ShadcnButtonVariant.Ghost,
            style = Style {
                foreground(shadcnTheme.colors.foreground)
                contentPadding(4f.dp, 0f.dp)
            },
            onClick = { toggle() }
        ) { slot ->
            // We use a simple row with fixed dimensions or predictable scaling logic.
            // To avoid WrapContent issues, we ensure children don't force unmeasured constraints.
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center
            , modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(slot.height.dp))) {
                 text(
                     label = if (isOpen) "-" else "+",
                     modifier = Modifier.width(12f.dp),
                     centered = true,
                     verticallyCentered = true
                 )
                 header()
            }
        }
    },
    content = content
)

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
