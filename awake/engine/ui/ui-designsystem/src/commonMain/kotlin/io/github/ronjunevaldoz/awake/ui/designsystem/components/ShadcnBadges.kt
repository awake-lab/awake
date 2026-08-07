// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

/** Real shadcn's `Badge`: an inline status pill -- defaults to Secondary, but
 * [ShadcnBadgeVariant] covers all semantic variants (Primary, Secondary, Outline,
 * Destructive). */
fun UiScope.shadcnBadge(
    label: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty,
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.badge(theme.asShadcnTheme(), variant) then ShadcnStyles.badgeContent(theme.asShadcnTheme()) then style,
    centered = true,
)

/** Real shadcn's `Kbd`: an inline key-cap label, same "measure text, draw a box, draw the
 * label" mechanics as [shadcnBadge] with a different (sm-radius, muted) style. */
fun UiScope.shadcnKbd(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.kbd(theme.asShadcnTheme()) then style,
    centered = true,
)
