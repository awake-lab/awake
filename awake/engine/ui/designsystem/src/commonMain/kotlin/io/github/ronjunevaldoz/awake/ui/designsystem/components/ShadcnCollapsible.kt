// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
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
import io.github.ronjunevaldoz.awake.ui.unstyled.components.collapsible
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [shadcnRadioGroup]'s single-select): a clickable trigger toggles [expanded].
 * Includes animated height transition.
 *
 * Primary Slot API form -- [trigger] receives `(isOpen, toggle)` and renders the *entire*
 * trigger, matching shadcn-compose's `trigger: @Composable (isOpen, toggle) -> Unit`
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
    content: ColumnScope.() -> Unit,
): Boolean = collapsible(
    id = id,
    expanded = expanded,
    modifier = modifier,
    onExpandedChange = onExpandedChange,
    trigger = trigger,
    content = content,
)

/**
 * [shadcnCollapsible] convenience with the previous fixed trigger: caller-supplied [header]
 * content on the left, a chevron affordance on the right (down when collapsed, up when
 * expanded, matching real shadcn's `ChevronsUpDown`-style trigger). Kept for callers that only
 * need to customize the header's inner content, not swap the trigger widget itself.
 *
 * [bordered] wraps the trigger *and* content together in one [shadcnSurface] panel -- real
 * shadcn's own reference `Collapsible` (e.g. a "Product details" panel) is a single bordered
 * box containing both, not a bordered card with a borderless collapsible nested inside it.
 * Getting that by wrapping a *caller's* `shadcnCard { shadcnCollapsible { ... } }` produces two
 * stacked visual boxes instead of one -- an outer static card border plus an inner trigger
 * that has no border of its own, reading as "nested cards". [bordered] fixes that at the
 * source: one component that is both the border and the collapse behavior. Defaults to
 * `false` so existing borderless callers (sidebar nav category groups, the plain showcase
 * demo) keep their current look untouched.
 */
private fun ColumnScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    bordered: Boolean = false,
    header: RowScope.() -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean {
    val shadcnTheme = theme.asShadcnTheme()
    fun ColumnScope.triggerAndContent(triggerModifier: UiModifier): Boolean = shadcnCollapsible(
        id = id,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        trigger = { isOpen, toggle ->
            shadcnButton(
                id = "$id.header",
                modifier = triggerModifier.fillMaxWidth().height(40f.dp),
                variant = ShadcnButtonVariant.Ghost,
                style = Style {
                    foreground(shadcnTheme.colors.foreground)
                    contentPadding(shadcnTheme.spacing.sm, 0f.dp)
                },
                centered = false,
                onClick = { toggle() },
            ) { slot ->
                row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = UiAlignment.Vertical.Center,
                    modifier = Modifier.width(Dimension.FillMax)
                        .height(Dimension.Fixed(slot.height.dp)),
                ) {
                    header()
                    icon(if (isOpen) ShadcnIcons.chevronUp else ShadcnIcons.chevronDown)
                }
            }
        },
        content = content,
    )
    if (!bordered) return triggerAndContent(modifier)
    var result = expanded
    shadcnCard(id = "$id.panel", modifier = modifier.fillMaxWidth()) {
        result = triggerAndContent(Modifier)
    }
    return result
}

/** [shadcnCollapsible] convenience with a plain string title. */
fun ColumnScope.shadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    bordered: Boolean = false,
    content: ColumnScope.() -> Unit,
): Boolean = shadcnCollapsible(
    id = id,
    expanded = expanded,
    modifier = modifier,
    onExpandedChange = onExpandedChange,
    bordered = bordered,
    header = { text(title, verticallyCentered = true) },
    content = content,
)
