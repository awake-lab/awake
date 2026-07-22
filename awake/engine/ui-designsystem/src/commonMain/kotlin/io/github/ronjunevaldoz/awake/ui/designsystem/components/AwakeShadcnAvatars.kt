// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.avatarFallback

private fun awakeShadcnAvatarStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        foreground(shadcnTheme.tokens.foreground)
    } then style
}

fun UiScope.awakeShadcnAvatar(
    initials: String,
    modifier: UiModifier = UiModifier(),
    diameter: Dp = 40f.dp,
    style: Style = Style.Empty
): Unit = avatarFallback(
    initials = initials,
    modifier = modifier,
    diameter = diameter,
    style = awakeShadcnAvatarStyle(theme, style)
)
