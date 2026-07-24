// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.avatarFallback
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private fun shadcnAvatarStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        foreground(shadcnTheme.tokens.foreground)
    } then style
}

fun UiScope.shadcnAvatar(
    initials: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = avatarFallback(
    initials = initials,
    modifier = modifier,
    style = shadcnAvatarStyle(theme, style)
)
