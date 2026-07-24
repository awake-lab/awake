// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** [sectionTitle] with Shadcn tokens. */
fun ColumnScope.shadcnSectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiSlot = sectionTitle(
    title = title,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.tokens.foreground)
        }
        textSize(shadcnTheme.typography.title)
    } then style
)

/** Larger headline text using Shadcn tokens. */
fun ColumnScope.shadcnHeadline(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.tokens.foreground)
        }
        textSize(shadcnTheme.typography.headline)
    } then style
)

/**Standard body text using Shadcn tokens. */
fun ColumnScope.shadcnBodyText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.tokens.foreground)
        }
        textSize(shadcnTheme.typography.body)
    } then style,
    maxLines = maxLines
)

/** Muted caption/supporting text using Shadcn tokens. */
fun ColumnScope.shadcnSupportingText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiSlot = supportingText(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.tokens.mutedForeground)
        }
        textSize(shadcnTheme.typography.caption)
    } then style,
    maxLines = maxLines
)

/** Generic shadcn text component with support for muted state and shimmer modifier. */
fun UiScope.shadcnText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (muted) {
            foreground(shadcnTheme.tokens.mutedForeground)
        } else if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.tokens.foreground)
        }
        textSize(shadcnTheme.typography.body)
    } then style,
    maxLines = maxLines
)

/** Common section header layout (title + optional description). */
fun ColumnScope.shadcnSectionHeader(
    title: ColumnScope.() -> Unit,
    description: (ColumnScope.() -> Unit)? = null
) {
    title()
    description?.invoke(this)
}

/** Convenience [shadcnSectionHeader] for plain string labels. */
fun ColumnScope.shadcnSectionHeader(
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier
): Unit = shadcnSectionHeader(
    title = { shadcnSectionTitle(title, modifier = modifier) },
    description = description?.takeIf { it.isNotBlank() }?.let { text ->
        { shadcnSupportingText(text) }
    }
)
