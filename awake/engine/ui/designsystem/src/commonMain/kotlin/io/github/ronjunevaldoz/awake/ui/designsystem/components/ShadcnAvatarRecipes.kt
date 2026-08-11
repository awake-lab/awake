// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.avatar
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width

private fun avatarStyle(values: io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues): SurfaceStyle =
    SurfaceStyle(
        background = values.colors.muted,
        foreground = values.colors.foreground,
        cornerRadius = values.shapes.full,
    )

fun ColumnScope.shadcnAvatar(
    id: String,
    initials: String,
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): Unit = avatar(
    id = id,
    initials = initials,
    size = size.boxSize,
    textSize = size.textSize,
    modifier = modifier,
    style = avatarStyle(themeValues),
)

fun RowScope.shadcnAvatar(
    id: String,
    initials: String,
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): Unit = avatar(
    id = id,
    initials = initials,
    size = size.boxSize,
    textSize = size.textSize,
    modifier = modifier,
    style = avatarStyle(themeValues),
)

fun RowScope.shadcnAvatarGroup(
    initials: List<String>,
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    overlap: io.github.ronjunevaldoz.awake.ui.api.Dp = 8f.dp,
): UiBounds = row(modifier = modifier) {
    initials.forEachIndexed { index, value ->
        avatar(
            id = "avatar.$index",
            initials = value,
            size = size.boxSize,
            textSize = size.textSize,
            style = SurfaceStyle(
                background = themeValues.colors.muted,
                foreground = themeValues.colors.foreground,
                cornerRadius = themeValues.shapes.full,
                border = SurfaceBorder(2f.dp, themeValues.colors.background),
            ),
            modifier = Modifier.offset(x = overlap * (-index.toFloat())),
        )
    }
}

fun BoxScope.shadcnAvatarBadge(
    modifier: Modifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): UiBounds = surface(
    id = "avatar.badge",
    modifier = modifier.width(size.badgeSize).height(size.badgeSize),
    style = SurfaceStyle(
        background = themeValues.colors.primary,
        border = SurfaceBorder(2f.dp, themeValues.colors.background),
        cornerRadius = themeValues.shapes.full,
    ),
) { }
