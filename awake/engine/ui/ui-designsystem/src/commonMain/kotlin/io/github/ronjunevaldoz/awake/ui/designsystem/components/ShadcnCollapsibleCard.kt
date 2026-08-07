// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTransparent
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

/**
 * The "card-collapsible" shape (shadcn.io's own example: a rounded card whose header is the
 * click target, tapping it height-animates a padded body in) -- composed from [shadcnCard]
 * (visual: background/border/radius, already decoupled from any single surface variant) and
 * [animatedHeight] (behavior: expand/collapse), NOT built by teaching [shadcnCollapsible] about
 * card styling. [shadcnCollapsible] stays exactly what its own doc comment says it is -- a
 * trigger + animated content, no opinion on background or border -- and this function is where
 * the two get wired together, so neither primitive knows about the other.
 *
 * Deliberately does NOT use [shadcnCard]'s own `header` slot: `shadcnCardContent` unconditionally
 * inserts a divider line between `header` and `body` (see that private function), which real
 * shadcn's card-collapsible reference doesn't have -- just spacing, no rule. The trigger row and
 * the animated body both live inside [shadcnCard]'s `body` slot instead, so this component owns
 * the whole layout and doesn't inherit a divider it never asked for.
 */
fun ColumnScope.shadcnCollapsibleCard(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    header: RowScope.() -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean {
    val shadcnTheme = theme.asShadcnTheme()
    // shadcnCard's own default style (theme.components.surface) bakes in contentPadding
    // (metrics.panelPadding) around whatever it wraps -- zeroed out here so the trigger row can
    // run edge to edge (chevron close to the card's own right border, matching the shadcn.io
    // reference) instead of sitting doubly inset by both the card's own padding AND this
    // component's own header/content padding below.
    shadcnCard(id = "$id.card", modifier = modifier.fillMaxWidth(), style = Style { contentPadding(0f.dp) }) { _ ->
        shadcnButton(
            id = "$id.header",
            modifier = Modifier.fillMaxWidth().height(40f.dp),
            variant = ShadcnButtonVariant.Ghost,
            // Ghost's own hover/active rules (Style.then appends, doesn't replace, so these
            // apply on TOP of Ghost's) paint a full-width rounded accent-color fill -- correct
            // for an actual button, wrong here: real shadcn's card-collapsible header reads as
            // part of the card, not a button island, on hover. Re-pinning background/foreground
            // to their resting values in both states neutralizes that fill without touching
            // Ghost's shape/padding/click behavior.
            style = Style {
                foreground(shadcnTheme.colors.foreground)
                contentPadding(shadcnTheme.spacing.sm, 0f.dp)
                hovered {
                    background(ShadcnTransparent)
                    foreground(shadcnTheme.colors.foreground)
                }
                active {
                    background(ShadcnTransparent)
                    foreground(shadcnTheme.colors.foreground)
                }
            },
            centered = false,
            onClick = { onExpandedChange(!expanded) },
        ) { slot ->
            row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(slot.height.dp)),
            ) {
                header()
                icon(if (expanded) ShadcnIcons.chevronUp else ShadcnIcons.chevronDown)
            }
        }
        // Same horizontal inset the trigger's own contentPadding uses, so expanded content
        // lines up with the header label instead of running edge to edge.
        animatedHeight(id = "$id.content", expanded = expanded) {
            column(modifier = Modifier.padding(shadcnTheme.spacing.sm, 0f.dp, shadcnTheme.spacing.sm, shadcnTheme.spacing.sm)) {
                content()
            }
        }
    }
    return expanded
}
