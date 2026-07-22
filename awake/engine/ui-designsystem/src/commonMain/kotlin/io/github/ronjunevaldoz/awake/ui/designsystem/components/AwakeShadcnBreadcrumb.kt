// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Real shadcn's `Breadcrumb`: a trail of muted links with a separator glyph between each,
 * the last item rendered as plain (non-link) current-page text. No click/navigation wiring --
 * that's caller-owned routing, same as every other Awake nav element; this only lays out the
 * trail and its visual states.
 */
fun ColumnScope.awakeShadcnBreadcrumb(
    items: List<String>,
    modifier: UiModifier = UiModifier(),
    separator: String = "/",
    height: Dp = 20f.dp
): UiSlot {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    return row(height = height, horizontalArrangement = Arrangement.spacedBy(6f.px), modifier = modifier) {
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
