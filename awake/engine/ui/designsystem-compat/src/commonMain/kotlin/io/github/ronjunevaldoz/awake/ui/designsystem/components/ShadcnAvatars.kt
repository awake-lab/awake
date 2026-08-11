// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.avatarFallback
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope
import io.github.ronjunevaldoz.awake.ui.headless.avatar as headlessAvatar

/** Mirrors shadcn-compose's `ShadcnAvatarSize` (`Sm`/`Default`/`Lg`): drives both the
 * avatar's box diameter AND its [shadcnAvatarBadge]/initials text size together, so a caller
 * picking `Lg` gets bigger initials automatically instead of a bigger circle with the same
 * tiny text. [boxSize] matches shadcn's own `size-6`/`size-8`/`size-10` (24/32/40px). */
enum class ShadcnAvatarSize(val boxSize: Dp, val textSize: Sp, val badgeSize: Dp) {
    Sm(24f.dp, ShadcnTheme.typography.caption, 8f.dp),
    Default(32f.dp, ShadcnTheme.typography.label, 10f.dp),
    Lg(40f.dp, ShadcnTheme.typography.body, 12f.dp),
}

private fun shadcnAvatarStyle(theme: UiTheme, size: ShadcnAvatarSize, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        foreground(shadcnTheme.colors.foreground)
        textSize(size.textSize)
    } then style
}

/** Only sets a diameter when [modifier] doesn't already claim one -- an explicit
 * `Modifier.width()`/`.height()`/`.size()` from the caller always wins, matching the
 * `ColumnScope`/`RowScope` `surface()` overloads' "modifier wins" convention. */
private fun UiModifier.withAvatarSize(size: ShadcnAvatarSize): UiModifier =
    if (widthDimension != null || heightDimension != null) {
        this
    } else {
        this.size(
            size.boxSize,
            size.boxSize,
        )
    }

/** Structure only (the circle) -- content is caller-supplied, e.g. a custom icon avatar
 * instead of initials text. See [avatarFallback]. */
@Deprecated(
    message = "Use a Headless-native avatar recipe. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnAvatar(
    modifier: UiModifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    style: Style = Style.Empty,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Unit = avatarFallback(
    modifier = modifier.withAvatarSize(size),
    style = shadcnAvatarStyle(theme, size, style),
    content = content,
)

/** Headless-native initials avatar recipe. */
fun HeadlessUiScope.shadcnAvatar(
    id: String,
    initials: String,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
): Unit = headlessAvatar(
    id = id,
    initials = initials,
    size = size.boxSize,
    textSize = size.textSize,
    style = SurfaceStyle(
        background = themeValues.colors.muted,
        foreground = themeValues.colors.foreground,
        cornerRadius = themeValues.shapes.full,
    ),
)

/** [shadcnAvatar] convenience that renders plain initials text as the content. */
@Deprecated(
    message = "Use a Headless-native avatar recipe. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnAvatar(
    initials: String,
    modifier: UiModifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    style: Style = Style.Empty,
): Unit = avatarFallback(
    initials = initials,
    modifier = modifier.withAvatarSize(size),
    style = shadcnAvatarStyle(theme, size, style),
)

/**
 * A small status dot anchored to an [shadcnAvatar]'s corner (e.g. an online indicator).
 * Reuses [avatarFallback]'s existing circle primitive (background + border + Circle shape)
 * instead of a new draw call -- an empty-content avatar IS a filled ring, which is exactly
 * what a badge dot is. Defaults to the bottom-end corner (real shadcn's usual placement);
 * override [modifier]'s `.align(...)` for a different corner. [size] should match the
 * enclosing [shadcnAvatar]'s own `size` -- Awake has no composition-local/ambient-context
 * mechanism to read it implicitly (unlike the Compose reference's `LocalAvatarSize`), so it's
 * an explicit param here, consistent with every other Awake widget call being explicit rather
 * than ambient.
 */
fun BoxScope.shadcnAvatarBadge(
    modifier: UiModifier = Modifier.align(UiAlignment.BottomEnd),
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
) {
    val shadcnTheme = theme.asShadcnTheme()
    avatarFallback(
        modifier = modifier.size(size.badgeSize, size.badgeSize),
        style = Style {
            background(shadcnTheme.palette.primary)
            borderWidth(2f.dp)
            borderColor(shadcnTheme.colors.background)
        },
    ) { }
}

/**
 * A row of [shadcnAvatar]s (e.g. "who's in this conversation"). Bare [RowScope] slot, matching
 * [shadcnBreadcrumb]'s shape -- caller composes avatars with their own `Modifier.offset(x = ...)`
 * for the overlap look. See the initials-list overload below for a version that does the
 * negative-offset + ring-border overlap automatically.
 */
fun RowScope.shadcnAvatarGroup(
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = defaultArrangement(),
    content: RowScope.() -> Unit,
): UiBounds = row(modifier = modifier, horizontalArrangement = horizontalArrangement) { content() }

/** [shadcnAvatarGroup] convenience that overlaps a plain initials list automatically -- each
 * avatar after the first shifted left by [overlap] and ringed in the page background so it
 * reads as stacked over its neighbor (matches real shadcn's `-space-x-2` + ring convention).
 * Uses zero row spacing (the negative [Modifier.offset] does all the positioning) -- the bare
 * slot overload above still defaults to the ambient row gap for non-overlapping callers. */
fun RowScope.shadcnAvatarGroup(
    initials: List<String>,
    modifier: UiModifier = Modifier,
    size: ShadcnAvatarSize = ShadcnAvatarSize.Default,
    overlap: Dp = 8f.dp,
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return shadcnAvatarGroup(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0f.dp),
    ) {
        initials.forEachIndexed { index, name ->
            shadcnAvatar(
                name,
                modifier = Modifier.offset(x = overlap * (-index.toFloat())),
                size = size,
                style = Style {
                    borderWidth(2f.dp)
                    borderColor(shadcnTheme.colors.background)
                },
            )
        }
    }
}
