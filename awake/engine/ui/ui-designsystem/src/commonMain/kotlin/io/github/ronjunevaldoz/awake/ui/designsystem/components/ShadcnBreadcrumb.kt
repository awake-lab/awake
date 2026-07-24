// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Real shadcn's `Breadcrumb`: a trail of muted links with a separator glyph between each,
 * the last item rendered as plain (non-link) current-page text. No click/navigation wiring --
 * that's caller-owned routing, same as every other Awake nav element; this only lays out the
 * trail and its visual states.
 */
fun ColumnScope.shadcnBreadcrumb(
    modifier: UiModifier = Modifier,
    height: Dp = 20f.dp,
    content: RowScope.() -> Unit
): UiSlot = row(
    horizontalArrangement = Arrangement.spacedBy(6f.dp),
    modifier = modifier.height(height.toDimension())
) {
    content()
}

/** [shadcnBreadcrumb] convenience with a plain string trail. */
fun ColumnScope.shadcnBreadcrumb(
    items: List<String>,
    modifier: UiModifier = Modifier,
    separator: String = "/",
    height: Dp = 20f.dp
): UiSlot {
    val shadcnTheme = theme.asShadcnTheme()
    return shadcnBreadcrumb(modifier = modifier, height = height) {
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
