// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MatchingDeclarationName") // ShadcnAvatarSize is a supporting type; the file's
// primary export is the shadcnAvatar* recipes below

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiTypography
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnAvatarBadgeStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnAvatarStyle
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.avatar
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width

/** Branded avatar dimensions; the structure and drawing remain Headless-owned. Text size is
 * deliberately NOT stored here -- storing it would freeze it to whichever theme happened to be
 * active the first time this enum's entries were initialized, instead of tracking the theme
 * actually active at each call site (see [resolveTextSize]). */
enum class ShadcnAvatarSize(val boxSize: Dp, val badgeSize: Dp) {
    Sm(24f.dp, 8f.dp),
    Default(32f.dp, 10f.dp),
    Lg(40f.dp, 12f.dp),
}

private fun ShadcnAvatarSize.resolveTextSize(typography: UiTypography): Sp = when (this) {
    ShadcnAvatarSize.Sm -> typography.caption
    ShadcnAvatarSize.Default -> typography.label
    ShadcnAvatarSize.Lg -> typography.body
}

fun UiScope.shadcnAvatar(
    id: String,
    initials: String,
    modifier: UiModifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): UiBounds = avatar(
    id = id,
    initials = initials,
    size = size.boxSize,
    textSize = size.resolveTextSize(themeValues.typography),
    modifier = modifier,
    style = shadcnAvatarStyle(themeValues),
)

fun UiScope.shadcnAvatarBadge(
    modifier: UiModifier = Modifier,
    size: Dp = 10f.dp,
    color: Color? = null,
    id: String = "avatar.badge",
): UiBounds = surface(
    id = id,
    modifier = modifier.width(size).height(size),
    style = shadcnAvatarBadgeStyle(themeValues, color),
) { _ -> }

fun UiScope.shadcnAvatarGroup(
    initials: List<String>,
    modifier: UiModifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    overlap: Dp = 8f.dp,
    id: String = "avatar",
): UiBounds = row(modifier = modifier) {
    initials.forEachIndexed { index, value ->
        shadcnAvatar(
            id = "$id.$index",
            initials = value,
            size = size,
            modifier = if (index > 0) Modifier.offset((-overlap.value * index).dp, 0f.dp) else Modifier,
        )
    }
}
