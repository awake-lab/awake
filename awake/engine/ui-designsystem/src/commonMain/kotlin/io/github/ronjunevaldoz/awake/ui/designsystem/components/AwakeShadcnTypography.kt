// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** [sectionTitle] with Shadcn tokens. */
fun ColumnScope.awakeShadcnSectionTitle(
    title: String,
    style: Style = Style.Empty
): UiSlot = sectionTitle(
    title = title,
    style = Style {
        val shadcnTheme = theme.asAwakeShadcnTheme()
        foreground(shadcnTheme.tokens.foreground)
        textSize(shadcnTheme.typography.title)
    } then style
)

/** Larger headline text using Shadcn tokens. */
fun ColumnScope.awakeShadcnHeadline(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asAwakeShadcnTheme()
        foreground(shadcnTheme.tokens.foreground)
        textSize(shadcnTheme.typography.headline)
    } then style
)

/** Standard body text using Shadcn tokens. */
fun ColumnScope.awakeShadcnBodyText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asAwakeShadcnTheme()
        foreground(shadcnTheme.tokens.foreground)
        textSize(shadcnTheme.typography.body)
    } then style,
    maxLines = maxLines
)

/** Muted caption/supporting text using Shadcn tokens. */
fun ColumnScope.awakeShadcnSupportingText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiSlot = supportingText(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asAwakeShadcnTheme()
        foreground(shadcnTheme.tokens.mutedForeground)
        textSize(shadcnTheme.typography.caption)
    } then style,
    maxLines = maxLines
)

/** Common section header layout (title + optional description). */
fun ColumnScope.awakeShadcnSectionHeader(
    title: ColumnScope.() -> Unit,
    description: (ColumnScope.() -> Unit)? = null
) {
    title()
    description?.invoke(this)
}

/** Convenience [awakeShadcnSectionHeader] for plain string labels. */
fun ColumnScope.awakeShadcnSectionHeader(
    title: String,
    description: String? = null
): Unit = awakeShadcnSectionHeader(
    title = { awakeShadcnSectionTitle(title) },
    description = description?.takeIf { it.isNotBlank() }?.let { text ->
        { awakeShadcnSupportingText(text) }
    }
)
