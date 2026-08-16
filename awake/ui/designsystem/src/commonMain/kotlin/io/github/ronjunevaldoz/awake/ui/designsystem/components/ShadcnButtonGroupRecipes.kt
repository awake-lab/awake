// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw

/**
 * shadcn's `ButtonGroup`: buttons joined into one control, sharing a single border and outer
 * radius.
 *
 * Ported from `registry/new-york-v4/ui/button-group.tsx` in the pinned shadcn checkout, which
 * achieves the join by stripping the adjacent side from each child --
 * `[&>*:not(:first-child)]:rounded-l-none [&>*:not(:first-child)]:border-l-0` horizontally, the
 * top equivalents vertically. CSS sibling selectors are not available here, so the GROUP owns the
 * border and the radius and the children are drawn borderless inside it: same visual result, and
 * the children stay ordinary [shadcnButton]s rather than needing a group-aware variant.
 *
 * `w-fit items-stretch` in the source is why this wraps its content rather than filling: a button
 * group is as wide as its buttons.
 */
fun UiScope.shadcnButtonGroup(
    id: String,
    modifier: Modifier = Modifier,
    content: RowScope.() -> Unit,
): UiBounds = groupSurface(id, modifier) {
    row(
        horizontalArrangement = Arrangement.spacedBy(0f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.fillMaxHeight(),
    ) { content() }
}

/**
 * The `orientation="vertical"` variant: same joined control, stacked.
 *
 * A separate function rather than a parameter because each direction's children are scoped to
 * their own layout ([RowScope] vs [ColumnScope]) -- a single entry point would have to hand the
 * caller an untyped scope and lose that.
 */
fun UiScope.shadcnButtonGroupColumn(
    id: String,
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = groupSurface(id, modifier) {
    column(
        verticalArrangement = Arrangement.spacedBy(0f.dp),
    ) { content() }
}

private fun UiScope.groupSurface(
    id: String,
    modifier: Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = Style {
        background(themeValues.colors.card)
        foreground(themeValues.colors.cardForeground)
        border(1f.dp, themeValues.colors.border)
        shape(themeValues.shapes.md)
        // No padding: the children ARE the control's edges, which is what makes the group read
        // as one button rather than a card with buttons in it.
        contentPadding(0f.dp)
    },
) { content() }

/**
 * The hairline between two members of a group -- shadcn's `ButtonGroupSeparator`, which defaults
 * to `orientation="vertical"` and `self-stretch` because it divides a horizontal row.
 */
fun RowScope.shadcnButtonGroupSeparator(
    id: String = "separator",
    modifier: Modifier = Modifier,
): UiBounds = shadcnSeparator(
    id = id,
    modifier = modifier.fillMaxHeight(),
    orientation = UiSeparatorOrientation.Vertical,
)

/** The vertical group's divider: a horizontal rule between stacked members. */
fun ColumnScope.shadcnButtonGroupSeparator(
    id: String = "separator",
    modifier: Modifier = Modifier,
): UiBounds = shadcnSeparator(
    id = id,
    modifier = modifier.fillMaxWidth(),
    orientation = UiSeparatorOrientation.Horizontal,
)

/**
 * A non-interactive label inside a group -- shadcn's `ButtonGroupText` (`bg-muted`, bordered,
 * `px-4 text-sm font-medium`). Used for a unit suffix, a count, or a mode readout that sits in
 * the same control as the buttons that change it.
 */
fun UiScope.shadcnButtonGroupText(
    id: String,
    text: String,
    modifier: Modifier = Modifier,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = Style {
        background(themeValues.colors.muted)
        foreground(themeValues.colors.mutedForeground)
        shape(0f.dp)
        // `px-4` in the source; no vertical inset, since the row's own height governs.
        contentPadding(Tw.Spacing.s4, 0f.dp, Tw.Spacing.s4, 0f.dp)
    },
) {
    shadcnText(text, tone = ShadcnTextTone.Muted)
}
