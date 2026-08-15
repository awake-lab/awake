// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.avatar
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.style.Style

private fun avatarStyle(values: io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues): Style = Style {
    background(values.colors.muted)
    foreground(values.colors.foreground)
    shape(values.shapes.full)
}

fun UiScope.shadcnAvatar(
    id: String,
    initials: String,
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): UiBounds = avatar(
    id = id,
    initials = initials,
    size = size.boxSize,
    textSize = size.textSize,
    modifier = modifier,
    style = avatarStyle(themeValues),
)

fun UiScope.shadcnAvatarBadge(
    modifier: Modifier = Modifier,
    size: io.github.ronjunevaldoz.awake.ui.api.Dp = 10f.dp,
    color: io.github.ronjunevaldoz.awake.core.colors.Color? = null,
): UiBounds = surface(
    id = "avatar.badge",
    modifier = modifier.width(size).height(size),
    style = SurfaceStyle(
        background = color ?: themeValues.colors.primary,
        cornerRadius = themeValues.shapes.full,
    ),
) { _ -> }

fun UiScope.shadcnAvatarGroup(
    initials: List<String>,
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    overlap: io.github.ronjunevaldoz.awake.ui.api.Dp = 8f.dp,
): UiBounds = row(modifier = modifier) {
    initials.forEachIndexed { index, value ->
        shadcnAvatar(
            id = "avatar.$index",
            initials = value,
            size = size,
            modifier = if (index > 0) Modifier.offset((-overlap.value * index).dp, 0f.dp) else Modifier,
        )
    }
}
