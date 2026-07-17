// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.supportingText

fun UiColumnDslScope.awakeShadcnSectionTitle(
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

fun UiColumnDslScope.awakeShadcnHeadline(
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

fun UiColumnDslScope.awakeShadcnBodyText(
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

fun UiColumnDslScope.awakeShadcnSupportingText(
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

fun UiColumnDslScope.awakeShadcnSectionHeader(
    title: UiColumnDslScope.() -> Unit,
    description: (UiColumnDslScope.() -> Unit)? = null
) {
    title()
    description?.invoke(this)
}

fun UiColumnDslScope.awakeShadcnSectionHeader(
    title: String,
    description: String? = null
): Unit = awakeShadcnSectionHeader(
    title = { awakeShadcnSectionTitle(title) },
    description = description?.takeIf { it.isNotBlank() }?.let { text ->
        { awakeShadcnSupportingText(text) }
    }
)
