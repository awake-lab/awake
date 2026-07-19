// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px

/**
 * Real shadcn's `Breadcrumb`: a trail of muted links with a separator glyph between each,
 * the last item rendered as plain (non-link) current-page text. No click/navigation wiring --
 * that's caller-owned routing, same as every other Awake nav element; this only lays out the
 * trail and its visual states.
 */
fun UiColumnDslScope.awakeShadcnBreadcrumb(
    items: List<String>,
    modifier: UiModifier = UiModifier(),
    separator: String = "/",
    height: Dp = 20f.dp
): UiSlot {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    return row(height = height, gap = 6f, modifier = modifier) {
        items.forEachIndexed { index, label ->
            val isCurrent = index == items.lastIndex
            text(
                label = label,
                style = Style {
                    foreground(if (isCurrent) shadcnTheme.tokens.foreground else shadcnTheme.tokens.mutedForeground)
                    textSize(shadcnTheme.typography.caption)
                }
            )
            if (!isCurrent) {
                text(
                    label = separator,
                    style = Style {
                        foreground(shadcnTheme.tokens.mutedForeground)
                        textSize(shadcnTheme.typography.caption)
                    }
                )
            }
        }
    }
}

/**
 * Real shadcn's `Collapsible` (and `Accordion`, a group of these with only one open at a
 * time -- caller-composed by tracking which id is expanded, same as
 * [awakeShadcnRadioGroup]'s single-select): a clickable header toggles [expanded]; the
 * caller's [content] block is only invoked while expanded, so hidden content costs nothing
 * to lay out. No animation -- shadcn's own is a CSS height transition, and this engine has
 * no per-widget animated-layout primitive to drive that with yet.
 */
fun UiColumnDslScope.awakeShadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = UiModifier(),
    content: UiColumnDslScope.() -> Unit
): Boolean {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    // Plain ASCII "-" not the unicode minus sign -- this engine's bitmap font is ASCII-only
    // (confirmed: U+2212 rendered as a missing-glyph "?" placeholder).
    val headerLabel = if (expanded) "$title  -" else "$title  +"
    val clicked = awakeShadcnButton(
        id = "$id.header",
        label = headerLabel,
        modifier = modifier.height(32f.px),
        variant = AwakeShadcnButtonVariant.Ghost,
        style = Style { foreground(shadcnTheme.tokens.foreground) },
        centered = false
    )
    val resolved = if (clicked) !expanded else expanded
    if (resolved) {
        content()
    }
    return resolved
}
