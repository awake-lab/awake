// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.clickable
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.resolveClickable
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text as drawText
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
): UiBounds = row(
    horizontalArrangement = Arrangement.spacedBy(6f.dp),
    modifier = modifier.height(height.toDimension())
) {
    content()
}.toBounds()

/** [shadcnBreadcrumb] convenience with a plain string trail. */
fun ColumnScope.shadcnBreadcrumb(
    items: List<String>,
    modifier: UiModifier = Modifier,
    separator: String = "/",
    height: Dp = 20f.dp
): UiBounds {
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

/**
 * A clickable, muted crumb -- every entry except the current page. Real shadcn uses an actual
 * icon-capable link component; Awake has no icon system yet (see [shadcnBreadcrumbSeparator]),
 * so this is text-only, wired the same way every other Awake nav element wires clicks:
 * [io.github.ronjunevaldoz.awake.ui.modifier.clickable] + [resolveClickable] on top of the
 * generic [text] widget, no bespoke click plumbing.
 */
fun RowScope.shadcnBreadcrumbLink(
    text: String,
    onClick: () -> Unit,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    val id = "breadcrumb-link:$text"
    val resolvedModifier = modifier.clickable(onClick = onClick)
    val bounds = drawText(
        label = text,
        modifier = resolvedModifier,
        style = Style {
            foreground(shadcnTheme.tokens.mutedForeground)
            textSize(shadcnTheme.typography.caption)
        } then style
    )
    resolveClickable(id = id, slot = bounds, modifier = resolvedModifier)
    return bounds
}

/** The current, non-clickable page -- always the last entry in a [shadcnBreadcrumb]. */
fun RowScope.shadcnBreadcrumbPage(
    text: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return drawText(
        label = text,
        modifier = modifier,
        style = Style {
            foreground(shadcnTheme.tokens.foreground)
            textSize(shadcnTheme.typography.caption)
        } then style
    )
}

/** A "/" divider between crumbs; insert manually where wanted -- matches the separator glyph
 * the [shadcnBreadcrumb] string-list overload already uses. */
fun RowScope.shadcnBreadcrumbSeparator(
    modifier: UiModifier = Modifier,
    glyph: String = "/"
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return drawText(
        label = glyph,
        modifier = modifier,
        style = Style {
            foreground(shadcnTheme.tokens.mutedForeground)
            textSize(shadcnTheme.typography.caption)
        }
    )
}

/** A collapsed run of hidden crumbs, e.g. `Home / ... / Settings`. Purely presentational --
 * expanding it is caller-driven, same as every other Awake widget: no hidden state here. */
fun RowScope.shadcnBreadcrumbEllipsis(modifier: UiModifier = Modifier): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return drawText(
        label = "...",
        modifier = modifier,
        style = Style {
            foreground(shadcnTheme.tokens.mutedForeground)
            textSize(shadcnTheme.typography.caption)
        }
    )
}
